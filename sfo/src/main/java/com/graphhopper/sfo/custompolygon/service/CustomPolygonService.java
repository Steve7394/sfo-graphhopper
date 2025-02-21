package com.graphhopper.sfo.custompolygon.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.sfo.custompolygon.dto.CustomPolygon;
import com.graphhopper.sfo.custompolygon.util.Polyline6Util;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomPolygonService {
    private final GraphHopper graphHopper;
    private final EncodingManager encodingManager;
    public CustomPolygonService(GraphHopper graphHopper, EncodingManager encodingManager) {
        this.graphHopper = graphHopper;
        this.encodingManager = encodingManager;
    }

    public List<CustomPolygon> getAll(){
        return graphHopper.getAreaManager().getCustomPolygons().stream().map(this::customArea2Polygon).collect(Collectors.toList());
    }

    public CustomPolygon getById(int id){
        CustomArea area = graphHopper.getAreaManager().getCustomPolygons().stream().filter(p -> Integer.parseInt(String.valueOf(p.getProperties().get("id"))) == id).findFirst().orElse(null);
        if (area == null){
            throw new RuntimeException("there is no custom area with id: " + id);
        }
        return customArea2Polygon(area);
    }

    public void save(CustomPolygon area){
        validate(area);
        graphHopper.getAreaManager().addCustomPolygon(
                customPolygon2CustomArea(area),
                graphHopper.getBaseGraph(),
                encodingManager
        );
    }

    public void update(CustomPolygon area, long id){
        validate(id);
        if (id != area.getId()){
            throw new RuntimeException("the id in path param: " + id + " do not match with area id: " + area.getId());
        }
        graphHopper.getAreaManager().updateCustomPolygon(
                String.valueOf(id),
                customPolygon2CustomArea(area),
                graphHopper.getBaseGraph(),
                encodingManager
        );
    }

    public void delete(int id){
        validate(id);
        graphHopper.getAreaManager().removeCustomPolygon(
                String.valueOf(id),
                graphHopper.getBaseGraph(),
                encodingManager
        );
    }

    private CustomArea customPolygon2CustomArea(CustomPolygon customPolygon){
        Map<String, Object> properties = new HashMap<>();
        properties.put("custom", 1);
        properties.put("id", customPolygon.getId());
        Polygon polygon = new GeometryFactory().createPolygon(Polyline6Util.decodePolyline6(customPolygon.getGeometry()).toArray(Coordinate[]::new));
        return new CustomArea(properties, List.of(polygon));
    }

    private CustomPolygon customArea2Polygon(CustomArea area){
        return new CustomPolygon(
                Integer.parseInt(String.valueOf(area.getProperties().get("id"))),
                Polyline6Util.encodePolyline6(Arrays.stream(area.getBorders().get(0).getCoordinates()).toList())
        );
    }

    private void validate(long id){
        if (id == 0){
            throw new IllegalArgumentException("The polygon id can not be zero");
        }
    }
    private void validate(CustomPolygon polygon){
        validate(polygon.getId());
    }
}
