package com.graphhopper.sfo.rgeocode.service;

import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.sfo.*;
import com.graphhopper.sfo.rgeocode.dto.SnapRequest;
import com.graphhopper.sfo.rgeocode.dto.SnapResponse;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;

public class ReverseGeocodeService {
    public static SnapResponse createSnapResponse(SnapRequest request, EncodingManager encodingManager, LocationIndex locationIndex){
        validateRequest(request);
        return createResponse(request, encodingManager, locationIndex);
    }

    public static SnapResponse createSnapResponse(double lat, double lon, EncodingManager encodingManager, LocationIndex locationIndex){
        validateRequest(lat, lon);
        return createResponse(lat, lon, encodingManager, locationIndex);
    }

    private static void validateRequest(SnapRequest request){
        if (request.getLat() == null || request.getLon() == null){
            throw new IllegalArgumentException("The latitude and longitude parameters can not be null");
        }
    }

    private static void validateRequest(double lat, double lon){
        if (lat == 0.0 || lon == 0.0){
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
        return createResponse(encodingManager, edge);
    }

    private static SnapResponse createResponse(double lat, double lon, EncodingManager encodingManager, LocationIndex locationIndex){
        EdgeIteratorState edge = locationIndex
                .findClosest(lat, lon, EdgeFilter.ALL_EDGES)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: { " + "lat: " + lat + ", lon: " + lon + " }");
        }
        return createResponse(encodingManager, edge);
    }

    private static SnapResponse createResponse(EncodingManager encodingManager, EdgeIteratorState edge ) {
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
        return response;
    }
}
