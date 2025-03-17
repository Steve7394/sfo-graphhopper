package com.graphhopper.reader.osm.sfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.*;

import java.util.List;

public class AdministrativeUtils {
    private final static GeometryFactory geometryFactory = new GeometryFactory();

    public static boolean isValid(Geometry geometry){
        return geometry.isValid();
    }

    public static MultiPolygon convertGeojsonNodeToMultiPolygon(ObjectNode geoJsonNode) {
        JsonNode coordinatesNode = geoJsonNode.get("geometry").get("coordinates");

        if (coordinatesNode == null || !coordinatesNode.isArray()) {
            throw new IllegalArgumentException("Invalid GeoJSON: 'coordinates' must be an array");
        }

        Polygon[] polygons = new Polygon[coordinatesNode.get(0).size()];

        for (int i = 0; i < coordinatesNode.get(0).size(); i++) {
            JsonNode polygonNode = coordinatesNode.get(0).get(i);
            polygons[i] = createPolygon(polygonNode, geometryFactory);
        }

        return geometryFactory.createMultiPolygon(polygons);
    }

    private static Polygon createPolygon(JsonNode polygonNode, GeometryFactory geometryFactory) {
        if (!polygonNode.isArray() || polygonNode.isEmpty()) {
            throw new IllegalArgumentException("Invalid Polygon data");
        }

        Coordinate[] shellCoords = parseCoordinates(polygonNode.get(0));
        LinearRing shell = geometryFactory.createLinearRing(shellCoords);

        LinearRing[] holes = new LinearRing[polygonNode.size() - 1];
        for (int i = 1; i < polygonNode.size(); i++) {
            Coordinate[] holeCoords = parseCoordinates(polygonNode.get(i));
            holes[i - 1] = geometryFactory.createLinearRing(holeCoords);
        }

        return geometryFactory.createPolygon(shell, holes);
    }

    private static Coordinate[] parseCoordinates(JsonNode arrayNode) {
        Coordinate[] coordinates = new Coordinate[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode coord = arrayNode.get(i);
            coordinates[i] = new Coordinate(coord.get(0).asDouble(), coord.get(1).asDouble());
        }
        return coordinates;
    }

    private static List<List<List<Coordinate>>> createPolygons(List<List<List<Coordinate>>> outerCoordinates, List<List<List<Coordinate>>> innerCoordinates) {
        for (List<List<Coordinate>> outerCoordinate : outerCoordinates) {
            LinearRing shell = geometryFactory.createLinearRing(outerCoordinate.toArray(new Coordinate[0]));

        }

    }
}
