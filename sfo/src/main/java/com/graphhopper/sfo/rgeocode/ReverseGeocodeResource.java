package com.graphhopper.sfo.rgeocode;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.CityNameParser;
import com.graphhopper.routing.util.parsers.CityOsmIdParser;
import com.graphhopper.routing.util.parsers.ProvinceNameParser;
import com.graphhopper.routing.util.parsers.ProvinceOsmIdParser;
import com.graphhopper.search.KVStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("reverse-geocode")
public class ReverseGeocodeResource {
    private final LocationIndex locationIndex;
    private final EncodingManager encodingManager;

    @Inject
    public ReverseGeocodeResource(GraphHopper graphhopper, EncodingManager encodingManager) {
        this.locationIndex = graphhopper.getLocationIndex();
        this.encodingManager = encodingManager;
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(SnapRequest requestBody) {
        validateRequest(requestBody);

        SnapResponse response = new SnapResponse();

        enrichResponse(requestBody, response);



        // possible exceptions must be added later
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    private void validateRequest(SnapRequest snapRequest){
        if (snapRequest.getLat() == null || snapRequest.getLon() == null){
            throw new IllegalArgumentException("The latitude and longitude parameters can not be null");
        }
        // out of bounds error based on iran country geometry must be implemented
    }

    private void enrichResponse(SnapRequest snapRequest, SnapResponse snapResponse){
        // we can implement edge filter, for example do not snap on footways or steps
        EdgeIteratorState edge = locationIndex
                .findClosest(snapRequest.getLat(), snapRequest.getLon(), EdgeFilter.ALL_EDGES)
                .getClosestEdge();
        if (edge == null){
            throw new IllegalArgumentException("There is no edge near requested point: {" + snapRequest.toString() + "}");
        }
        snapResponse.setStreet(edge.getName());
        snapResponse.setStreetType(edge.get(encodingManager.getEnumEncodedValue(RoadClass.KEY, RoadClass.class)).name());
        snapResponse.setStreetWayId(edge.get(encodingManager.getIntEncodedValue(OSMWayID.KEY)));
        snapResponse.setStreetMaxSpeed(edge.get(encodingManager.getDecimalEncodedValue(MaxSpeed.KEY)));
        snapResponse.setCountry(edge.get(encodingManager.getEnumEncodedValue(Country.KEY, Country.class)).getCountryName());
        snapResponse.setProvince(edge.get(encodingManager.getStringEncodedValue(ProvinceNameParser.KEY)));
        snapResponse.setProvinceOsmId(edge.get(encodingManager.getIntEncodedValue(ProvinceOsmIdParser.KEY)));
        snapResponse.setCity(edge.get(encodingManager.getStringEncodedValue(CityNameParser.KEY)));
        snapResponse.setCityOsmId(edge.get(encodingManager.getIntEncodedValue(CityOsmIdParser.KEY)));
    }
}
