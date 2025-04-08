package com.graphhopper.sfo.rgeocode.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.gpx.GpxConversions;
import com.graphhopper.jackson.Gpx;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.reader.osm.sfo.AdministrativeLevel;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.sfo.*;
import com.graphhopper.sfo.rgeocode.dto.MapMatchResponse;
import com.graphhopper.sfo.rgeocode.dto.SnapRequest;
import com.graphhopper.sfo.rgeocode.dto.SnapResponse;
import com.graphhopper.sfo.rgeocode.util.ReverseGeocodeHeadingFilter;
import com.graphhopper.sfo.rgeocode.util.Utils;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import com.graphhopper.util.shapes.GHPoint;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReverseGeocodeService {
    private final GraphHopper graphhopper;

    public ReverseGeocodeService(GraphHopper graphHopper) {
        this.graphhopper = graphHopper;
    }

    public SnapResponse createSnapResponse(SnapRequest request) {
        validateRequest(request);
        return createResponse(request);
    }

    public SnapResponse createSnapResponse(double lat, double lon, boolean forceEdge) {
        validateRequest(lat, lon);
        return createResponse(lat, lon, EdgeFilter.ALL_EDGES, forceEdge);
    }

    private void validateRequest(SnapRequest request) {
        validateRequest(request.getLat(), request.getLon());
    }

    private void validateRequest(double lat, double lon) {
        if (lat == 0.0 || lon == 0.0) {
            throw new IllegalArgumentException("The latitude and longitude parameters can not be null");
        }
    }

    private SnapResponse createResponse(SnapRequest request) {
        return createResponse(request.getLat(), request.getLon(), EdgeFilter.ALL_EDGES, request.isForceEdge());
    }

    private SnapResponse createResponse(double lat, double lon, EdgeFilter edgeFilter, boolean forceEdge) {
        EdgeIteratorState edge = graphhopper.getLocationIndex()
                .findClosest(lat, lon, edgeFilter)
                .getClosestEdge();
        if (edge != null) {
            return createResponse(edge);
        } else {
            if (forceEdge) {
                throw new IllegalArgumentException("There is no edge near requested point: { " + "lat: " + lat + ", lon: " + lon + " }");
            }
            return createResponse(lat, lon);
        }
    }

    private SnapResponse createResponse(double lat, double lon) {
        List<CustomArea> administrativePolygons = Utils.findPolygons(
                lat,
                lon,
                graphhopper.getAreaManager().getAdministrativePolygons()
        );
        List<CustomArea> customPolygons = Utils.findPolygons(
                lat,
                lon,
                graphhopper.getAreaManager().getCustomPolygons()
        );

        SnapResponse snapResponse = new SnapResponse();
        administrativePolygons.forEach(administrativePolygon -> {
            String name = (String) administrativePolygon.getProperties().getOrDefault("name", "");
            int osmId = (int) administrativePolygon.getProperties().getOrDefault("osm_id", 0);
            String level = (String) administrativePolygon.getProperties().getOrDefault("level", Country.KEY);

            if (level.equals(Country.KEY)) {
                snapResponse.setCountry(name);
            } else if (level.equals(AdministrativeLevel.PROVINCE.getName())) {
                snapResponse.setProvince(name);
                snapResponse.setProvinceOsmId(osmId);
            } else if (level.equals(AdministrativeLevel.COUNTY.getName())) {
                snapResponse.setCounty(name);
                snapResponse.setCountyOsmId(osmId);
            } else if (level.equals(AdministrativeLevel.DISTRICT.getName())) {
                snapResponse.setDistrict(name);
                snapResponse.setDistrictOsmId(osmId);
            } else if (level.equals(AdministrativeLevel.CITY_MUNICIPALITY.getName())) {
                snapResponse.setCity(name);
                snapResponse.setCityOsmId(osmId);
            } else if (level.equals(AdministrativeLevel.VILLAGE.getName())) {
                snapResponse.setVillage(name);
                snapResponse.setVillageOsmId(osmId);
            } else if (level.equals(AdministrativeLevel.SUBURB.getName())) {
                snapResponse.setSuburb(name);
                snapResponse.setSuburbOsmId(osmId);
            } else if (level.equals(AdministrativeLevel.SUBAREA.getName())) {
                snapResponse.setSubarea(name);
                snapResponse.setSubareaOsmId(osmId);
            } else if (level.equals(AdministrativeLevel.NEIGHBOURHOOD.getName())) {
                snapResponse.setNeighbourhood(name);
                snapResponse.setNeighbourhoodOsmId(osmId);
            }
        });

        snapResponse.setCustomPolygon(customPolygons.stream().map(c -> (long) c.getProperties().get("id")).collect(Collectors.toList()));

        return snapResponse;
    }

    private SnapResponse createResponse(EdgeIteratorState edge) {
        EncodingManager encodingManager = graphhopper.getEncodingManager();
        SnapResponse response = new SnapResponse();
        response.setStreet(edge.getName());
        response.setStreetType(edge.get(encodingManager.getEnumEncodedValue(RoadClass.KEY, RoadClass.class)).name());
        response.setStreetWayId(edge.get(encodingManager.getIntEncodedValue(OSMWayID.KEY)));
        response.setStreetMaxSpeed(edge.get(encodingManager.getDecimalEncodedValue(MaxSpeed.KEY)));

        response.setCountry(edge.get(encodingManager.getEnumEncodedValue(Country.KEY, Country.class)).getCountryName());

        response.setProvince(edge.get(encodingManager.getStringEncodedValue(ProvinceNameParser.KEY)));
        response.setProvinceOsmId(edge.get(encodingManager.getIntEncodedValue(ProvinceOsmIdParser.KEY)));

        response.setCity(edge.get(encodingManager.getStringEncodedValue(CityNameParser.KEY)));
        response.setCityOsmId(edge.get(encodingManager.getIntEncodedValue(CityOsmIdParser.KEY)));

        response.setCounty(edge.get(encodingManager.getStringEncodedValue(CountyNameParser.KEY)));
        response.setCountyOsmId(edge.get(encodingManager.getIntEncodedValue(CountyOsmIdParser.KEY)));

        response.setDistrict(edge.get(encodingManager.getStringEncodedValue(DistrictNameParser.KEY)));
        response.setDistrictOsmId(edge.get(encodingManager.getIntEncodedValue(DistrictOsmIdParser.KEY)));

        response.setVillage(edge.get(encodingManager.getStringEncodedValue(VillageNameParser.KEY)));
        response.setVillageOsmId(edge.get(encodingManager.getIntEncodedValue(VillageOsmIdParser.KEY)));

        response.setSuburb(edge.get(encodingManager.getStringEncodedValue(SuburbNameParser.KEY)));
        response.setSuburbOsmId(edge.get(encodingManager.getIntEncodedValue(SuburbOsmIdParser.KEY)));

        response.setSubarea(edge.get(encodingManager.getStringEncodedValue(SubareaNameParser.KEY)));
        response.setSubareaOsmId(edge.get(encodingManager.getIntEncodedValue(SubareaOsmIdParser.KEY)));

        response.setNeighbourhood(edge.get(encodingManager.getStringEncodedValue(NeighbourhoodNameParser.KEY)));
        response.setNeighbourhoodOsmId(edge.get(encodingManager.getIntEncodedValue(NeighbourhoodOsmIdParser.KEY)));
        List<Long> areas = Arrays.stream(edge.get(encodingManager.getStringEncodedValue(CustomPolygonIdParser.KEY)).split(",")).filter(v -> !v.equals("0")).map(Long::valueOf).toList();
        response.setCustomPolygon(
                areas
        );
        return response;
    }

    public SnapResponse createSnapResponse(List<GHPoint> points, boolean forceEdge) {
        double bearing = Utils.calculateOverallBearing(points);
        EdgeFilter headingFilter = new ReverseGeocodeHeadingFilter(bearing, points.get(points.size() - 1));
        double lat = points.get(points.size() - 1).getLat();
        double lon = points.get(points.size() - 1).getLon();
        return createResponse(lat, lon, headingFilter, forceEdge);
    }

    public MapMatchResponse createMapMatchResponse(List<GHPoint> points, double gpsError) {
        EncodingManager encodingManager = graphhopper.getEncodingManager();
        Gpx gpx = Utils.createGpxFromPointList(points);
        PMap hints = new PMap();
        hints.putObject("profile", "car");
        MapMatching mapMatching = MapMatching.fromGraphHopper(graphhopper, hints);
        mapMatching.setMeasurementErrorSigma(gpsError);
        MatchResult result = mapMatching.match(GpxConversions.getEntries(gpx.trk.get(0)));
        MapMatchResponse mapMatchResponse = new MapMatchResponse();
        double sumMaxSpeed = 0;
        int size = result.getEdgeMatches().size();
        if (size == 0) {
            throw new RuntimeException("There is no match for: " + points);
        }
        for (int i = 0; i < size; i++) {
            EdgeIteratorState edge = result.getEdgeMatches().get(i).getEdgeState();
            sumMaxSpeed += edge.get(encodingManager.getDecimalEncodedValue(MaxSpeed.KEY));
            if (i == size - 1) {
                mapMatchResponse.setSnapResponse(createResponse(edge));
            }
        }
        mapMatchResponse.setDistance(Helper.round2(result.getMatchLength()));
        mapMatchResponse.setTime(Helper.round2(result.getMatchMillis() / 1000.0));
        mapMatchResponse.setAverageMaxSpeed(Helper.round2(sumMaxSpeed / size));
        return mapMatchResponse;

    }
}
