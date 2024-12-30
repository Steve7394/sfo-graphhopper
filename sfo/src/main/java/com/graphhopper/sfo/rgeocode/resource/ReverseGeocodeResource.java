package com.graphhopper.sfo.rgeocode.resource;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.CityNameParser;
import com.graphhopper.routing.util.parsers.CityOsmIdParser;
import com.graphhopper.routing.util.parsers.ProvinceNameParser;
import com.graphhopper.routing.util.parsers.ProvinceOsmIdParser;
import com.graphhopper.sfo.rgeocode.dto.SnapRequest;
import com.graphhopper.sfo.rgeocode.dto.SnapResponse;
import com.graphhopper.sfo.rgeocode.service.ReverseGeocodeService;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.util.EdgeIteratorState;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
}
