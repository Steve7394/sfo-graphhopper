package com.graphhopper.reader.osm.sfo;

import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AdministrativeUtils {
    private final static GeometryFactory geometryFactory = new GeometryFactory();
    private static final Logger LOGGER = LoggerFactory.getLogger(AdministrativeUtils.class);


    public static boolean isValid(Geometry geometry) {
        return geometry.isValid();
    }

    private static Coordinate[] parseCoordinatesFromList(List<List<Double>> coordinateList) {
        Coordinate[] coordinates = new Coordinate[coordinateList.size()];
        for (int i = 0; i < coordinateList.size(); i++) {
            coordinates[i] = new Coordinate(coordinateList.get(i).get(0), coordinateList.get(i).get(1));
        }
        return coordinates;
    }

    public static Map<Integer, LinearRing> createRings(List<List<List<Double>>> coordinates) {
        Map<Integer, LinearRing> rings = new HashMap<>(coordinates.size());
        int counter = 0;
        for (List<List<Double>> coordinate : coordinates) {
            LinearRing shell = geometryFactory.createLinearRing(parseCoordinatesFromList(coordinate));
            rings.put(counter, shell);
            counter++;
        }
        return rings;
    }

    public static List<List<List<List<Double>>>> createMultiPolygon(List<List<List<Double>>> outerCoordinates, List<List<List<Double>>> innerCoordinates, Long featureId) {
        List<List<List<List<Double>>>> result = new ArrayList<>(outerCoordinates.size());
        Map<Integer, LinearRing> outerRings = createRings(outerCoordinates);
        Map<Integer, LinearRing> innerRings = createRings(innerCoordinates);
        Map<Integer, List<Integer>> polygonsMap = new HashMap<>(outerCoordinates.size());
        for (int outerRingKey : outerRings.keySet()) {
            polygonsMap.putIfAbsent(outerRingKey, new ArrayList<>(innerCoordinates.size()));
            for (int innerRingKey : innerRings.keySet()) {
                if (outerRings.get(outerRingKey).contains(innerRings.get(innerRingKey))) {
                    polygonsMap.get(outerRingKey).add(innerRingKey);
                }
            }
        }
        Polygon[] polygonArray = new Polygon[polygonsMap.size()];
        int polygonCounter = 0;
        for (int polygonKey : polygonsMap.keySet()) {
            LinearRing[] holes = new LinearRing[polygonsMap.get(polygonKey).size()];
            int counter = 0;
            List<List<List<Double>>> polygon = new ArrayList<>();
            polygon.add(outerCoordinates.get(polygonKey));
            for (int innerKey : polygonsMap.get(polygonKey)) {
                holes[counter] = innerRings.get(innerKey);
                counter++;
                polygon.add(innerCoordinates.get(innerKey));
            }
            Polygon tempPolygon = geometryFactory.createPolygon(outerRings.get(polygonKey), holes);
            if (!tempPolygon.isValid()) {
                LOGGER.warn("The relation with Id " + featureId + " does not have valid geometry");
                return null;
            }
            polygonArray[polygonCounter] = tempPolygon;
            polygonCounter ++;
            result.add(polygon);
        }
        MultiPolygon tempMultiPolygon = geometryFactory.createMultiPolygon(polygonArray);
        if (!tempMultiPolygon.isValid()){
            LOGGER.warn("The relation with Id " + featureId + " does not have valid geometry");
            return null;
        }
        return result;
    }
}
