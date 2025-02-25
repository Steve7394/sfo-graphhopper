package com.graphhopper.sfo.custompolygon.resource;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.sfo.custompolygon.dto.CustomPolygon;
import com.graphhopper.sfo.custompolygon.service.CustomPolygonService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("custom-polygon")
public class CustomPolygonResource {
    private final CustomPolygonService customPolygonService;

    @Inject
    public CustomPolygonResource(GraphHopper graphHopper, EncodingManager encodingManager) {
        this.customPolygonService = new CustomPolygonService(graphHopper, encodingManager);
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(){
        return Response.status(Response.Status.OK).entity(customPolygonService.getAll()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") long id)
    {
        return Response.status(Response.Status.OK).entity(customPolygonService.getById(id)).build();
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response save(CustomPolygon polygon)
    {
        customPolygonService.save(polygon);
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") long id, CustomPolygon polygon)
    {
        customPolygonService.update(polygon, id);
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") long id)
    {
        customPolygonService.delete(id);
        return Response.status(Response.Status.OK).build();
    }
}
