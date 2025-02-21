package com.graphhopper.sfo.rgeocode.service;

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.DirectedEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.HeadingEdgeFilter;
import com.graphhopper.routing.util.parsers.sfo.*;
import com.graphhopper.sfo.rgeocode.dto.SnapRequest;
import com.graphhopper.sfo.rgeocode.dto.SnapResponse;
import com.graphhopper.sfo.rgeocode.util.ReverseGeocodeHeadingFilter;
import com.graphhopper.sfo.rgeocode.util.Utils;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.shapes.GHPoint;

import java.util.Arrays;
import java.util.List;

public class ReverseGeocodeService {
    private final EncodingManager encodingManager;
    private final LocationIndex locationIndex;

    public ReverseGeocodeService(EncodingManager encodingManager, LocationIndex locationIndex) {
        this.encodingManager = encodingManager;
        this.locationIndex = locationIndex;
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
        EdgeIteratorState edge = locationIndex
                .findClosest(request.getLat(), request.getLon(), EdgeFilter.ALL_EDGES)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: {" + request.toString() + "}");
        }
        return createResponse(edge);
    }

    private SnapResponse createResponse(double lat, double lon){
        EdgeIteratorState edge = locationIndex
                .findClosest(lat, lon, EdgeFilter.ALL_EDGES)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: { " + "lat: " + lat + ", lon: " + lon + " }");
        }
        return createResponse(edge);
    }

    private SnapResponse createResponse(EdgeIteratorState edge ) {
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
        // we must add snapped point to response for all rests
        double bearing = Utils.calculateOverallBearing(points);
        EdgeFilter headingFilter = new ReverseGeocodeHeadingFilter(bearing, points.get(points.size()-1));
        double lat = points.get(points.size()-1).getLat();
        double lon = points.get(points.size()-1).getLon();
        EdgeIteratorState edge = locationIndex
                .findClosest(lat, lon, headingFilter)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: { " + "lat: " + lat + ", lon: " + lon + " }");
        }
        return createResponse(edge);
    }
}
