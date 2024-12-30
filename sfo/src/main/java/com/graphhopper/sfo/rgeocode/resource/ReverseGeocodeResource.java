package com.graphhopper.sfo.rgeocode.resource;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.sfo.rgeocode.dto.SnapRequest;
import com.graphhopper.sfo.rgeocode.dto.SnapResponse;
import com.graphhopper.sfo.rgeocode.service.ReverseGeocodeService;
import com.graphhopper.storage.index.LocationIndex;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        SnapResponse response = ReverseGeocodeService.createSnapResponse(requestBody, encodingManager, locationIndex);
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet(@QueryParam("lat") double lat, @QueryParam("lon") double lon) {
        SnapResponse response = ReverseGeocodeService.createSnapResponse(lat, lon, encodingManager, locationIndex);
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }
}
