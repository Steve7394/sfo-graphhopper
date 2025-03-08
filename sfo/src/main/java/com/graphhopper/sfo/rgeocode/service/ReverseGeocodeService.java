package com.graphhopper.sfo.rgeocode.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.gpx.GpxConversions;
import com.graphhopper.jackson.Gpx;
import com.graphhopper.matching.MapMatching;
import com.graphhopper.matching.MatchResult;
import com.graphhopper.routing.ev.*;
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

public class ReverseGeocodeService {
    private final GraphHopper graphhopper;

    public ReverseGeocodeService(GraphHopper graphHopper) {
        this.graphhopper = graphHopper;
    }

    public SnapResponse createSnapResponse(SnapRequest request){
        validateRequest(request);
        return createResponse(request);
    }

    public SnapResponse createSnapResponse(double lat, double lon){
        validateRequest(lat, lon);
        return createResponse(lat, lon);
    }

    private void validateRequest(SnapRequest request){
        if (request.getLat() == null || request.getLon() == null){
            throw new IllegalArgumentException("The latitude and longitude parameters can not be null");
        }
    }

    private void validateRequest(double lat, double lon){
        if (lat == 0.0 || lon == 0.0){
            throw new IllegalArgumentException("The latitude and longitude parameters can not be null");
        }
    }

    private SnapResponse createResponse(SnapRequest request){
        EdgeIteratorState edge = graphhopper.getLocationIndex()
                .findClosest(request.getLat(), request.getLon(), EdgeFilter.ALL_EDGES)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: {" + request.toString() + "}");
        }
        return createResponse(edge);
    }

    private SnapResponse createResponse(double lat, double lon){
        EdgeIteratorState edge = graphhopper.getLocationIndex()
                .findClosest(lat, lon, EdgeFilter.ALL_EDGES)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: { " + "lat: " + lat + ", lon: " + lon + " }");
        }
        return createResponse(edge);
    }

    private SnapResponse createResponse(EdgeIteratorState edge ) {
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

    public SnapResponse createSnapResponse(List<GHPoint> points){
        double bearing = Utils.calculateOverallBearing(points);
        EdgeFilter headingFilter = new ReverseGeocodeHeadingFilter(bearing, points.get(points.size()-1));
        double lat = points.get(points.size()-1).getLat();
        double lon = points.get(points.size()-1).getLon();
        EdgeIteratorState edge = graphhopper.getLocationIndex()
                .findClosest(lat, lon, headingFilter)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: { " + "lat: " + lat + ", lon: " + lon + " }");
        }
        return createResponse(edge);
    }

    public MapMatchResponse createMapMatchResponse(List<GHPoint> points, double gpsError){
        EncodingManager encodingManager = graphhopper.getEncodingManager();
        Gpx gpx = Utils.createGpxFromPointList(points);
        PMap hints = new PMap();
        hints.putObject("profile", "car");
        MapMatching mapMatching = MapMatching.fromGraphHopper(graphhopper, hints);
        MatchResult result = mapMatching.match(GpxConversions.getEntries(gpx.trk.get(0)));
        MapMatchResponse mapMatchResponse = new MapMatchResponse();
        double sumMaxSpeed = 0;
        int size = result.getEdgeMatches().size();
        if (size == 0){
            throw new RuntimeException("There is no match for: " + points);
        }
        for (int i = 0; i < size; i++){
            EdgeIteratorState edge = result.getEdgeMatches().get(i).getEdgeState();
            sumMaxSpeed += edge.get(encodingManager.getDecimalEncodedValue(MaxSpeed.KEY));
            if (i == size - 1){
                mapMatchResponse.setSnapResponse(createResponse(edge));
            }
        }
        mapMatchResponse.setDistance(Helper.round2(result.getMatchLength()));
        mapMatchResponse.setTime(Helper.round2(result.getMatchMillis()/1000.0));
        mapMatchResponse.setAverageMaxSpeed(Helper.round2(sumMaxSpeed / size));
        return mapMatchResponse;

    }
}
