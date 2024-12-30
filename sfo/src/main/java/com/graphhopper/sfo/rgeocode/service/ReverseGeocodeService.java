package com.graphhopper.sfo.rgeocode.service;

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.CityNameParser;
import com.graphhopper.routing.util.parsers.CityOsmIdParser;
import com.graphhopper.routing.util.parsers.ProvinceNameParser;
import com.graphhopper.routing.util.parsers.ProvinceOsmIdParser;
import com.graphhopper.sfo.rgeocode.dto.SnapRequest;
import com.graphhopper.sfo.rgeocode.dto.SnapResponse;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;

import java.util.List;

public class ReverseGeocodeService {
    public static SnapResponse createSnapResponse(SnapRequest request, EncodingManager encodingManager, LocationIndex locationIndex){
        validateRequest(request);
        return createResponse(request, encodingManager, locationIndex);
    }

    private static void validateRequest(SnapRequest request){
        if (request.getLat() == null || request.getLon() == null){
            throw new IllegalArgumentException("The latitude and longitude parameters can not be null");
        }
    }

    private static SnapResponse createResponse(SnapRequest request, EncodingManager encodingManager, LocationIndex locationIndex){
        EdgeIteratorState edge = locationIndex
                .findClosest(request.getLat(), request.getLon(), EdgeFilter.ALL_EDGES)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: {" + request.toString() + "}");
        }
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
        return response;
    }
}
