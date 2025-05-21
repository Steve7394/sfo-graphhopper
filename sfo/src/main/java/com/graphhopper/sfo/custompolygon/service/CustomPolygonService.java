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

    public CustomPolygon getById(long id){
        CustomArea area = graphHopper.getAreaManager().getCustomPolygons().stream().filter(p -> Long.parseLong(String.valueOf(p.getProperties().get("id"))) == id).findFirst().orElse(null);
        if (area == null){
            throw new RuntimeException("there is no custom area with id: " + id);
        }
        return customArea2Polygon(area);
    }

    public void save(CustomPolygon area){
        validate(area);
        if (graphHopper.getAreaManager().getCustomArea(area.getId()) != null){
            throw new IllegalArgumentException("custom area already exists: " + area.getId());
        }
        graphHopper.getAreaManager().addCustomPolygon(
                customPolygon2CustomArea(area),
                graphHopper.getBaseGraph(),
                encodingManager
        );
    }

    public void update(CustomPolygon area, Long id){
        validateId(id);
        validateGeometryString(area.getGeometry());
        area.setId(id);
        graphHopper.getAreaManager().updateCustomPolygon(
                id,
                customPolygon2CustomArea(area),
                graphHopper.getBaseGraph(),
                encodingManager
        );
    }

    public void delete(Long id){
        validateId(id);
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
                Long.parseLong(String.valueOf(area.getProperties().get("id"))),
                Polyline6Util.encodePolyline6(Arrays.stream(area.getBorders().get(0).getCoordinates()).toList())
        );
    }

    private void validateId(Long id){
        if (id == null){
            throw new IllegalArgumentException("The polygon id can not be null");
        }
    }

    private void validateGeometryString(String geometryString){
        if (geometryString == null || geometryString.isEmpty()){
            throw new IllegalArgumentException("The polygon geometry can not be empty or null");
        }
    }
    private void validate(CustomPolygon polygon){
        validateId(polygon.getId());
        validateGeometryString(polygon.getGeometry());
    }
}
