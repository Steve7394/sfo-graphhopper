package com.graphhopper.sfo.rgeocode.resource;

import com.graphhopper.GraphHopper;
import com.graphhopper.sfo.rgeocode.dto.MapMatchResponse;
import com.graphhopper.sfo.rgeocode.dto.SnapRequest;
import com.graphhopper.sfo.rgeocode.dto.SnapResponse;
import com.graphhopper.sfo.rgeocode.service.ReverseGeocodeService;
import com.graphhopper.sfo.rgeocode.util.Utils;
import com.graphhopper.util.shapes.GHPoint;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("reverse-geocode")
public class ReverseGeocodeResource {
    private final ReverseGeocodeService reverseGeocodeService;

    @Inject
    public ReverseGeocodeResource(GraphHopper graphhopper) {
        this.reverseGeocodeService = new ReverseGeocodeService(graphhopper);
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(SnapRequest requestBody) {
        SnapResponse response = reverseGeocodeService.createSnapResponse(requestBody);
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet(
            @QueryParam("lat") double lat,
            @QueryParam("lon") double lon,
            @QueryParam("forceEdge") @DefaultValue("true") boolean forceEdge
            ) {
        SnapResponse response = reverseGeocodeService.createSnapResponse(lat, lon, forceEdge);
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("{coordinatesArray : .+}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetMultiPointBySnap(
            @Context HttpServletRequest httpReq,
            @QueryParam("forceEdge") @DefaultValue("true") boolean forceEdge
            ){
        List<GHPoint> points = Utils.getPointsFromRequest(httpReq, "/reverse-geocode/");
        SnapResponse response = reverseGeocodeService.createSnapResponse(points, forceEdge);
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }

    @GET
    @Path("/match/{coordinatesArray : .+}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetMultiPointByMatch(
            @Context HttpServletRequest httpReq,
            @QueryParam("gpsError") @DefaultValue("10") double gpsError
    ){
        List<GHPoint> points = Utils.getPointsFromRequest(httpReq, "/reverse-geocode/match/");
        MapMatchResponse response = reverseGeocodeService.createMapMatchResponse(points, gpsError);
        return Response.status(Response.Status.OK)
                .entity(response)
                .build();
    }
}
