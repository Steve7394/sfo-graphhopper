package com.graphhopper.sfo.custompolygon.util;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class Polyline6Util {

    public static List<Coordinate> decodePolyline6(String encoded) {
        List<Coordinate> points = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int shift = 0;
            int result = 0;
            int b;
            do {
                if (encoded.length() - 1 < index) throw new RuntimeException("The Geometry is not valid");
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            shift = 0;
            result = 0;
            do {
                if (encoded.length() - 1 < index) throw new RuntimeException("The Geometry is not valid");
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            points.add(new Coordinate(lng / 1E6, lat / 1E6));
        }

        return points;
    }

    public static String encodePolyline6(List<Coordinate> points) {
        StringBuilder encoded = new StringBuilder();
        int prevLat = 0;
        int prevLng = 0;

        for (Coordinate point : points) {
            int lat = (int) (point.getY() * 1E6);
            int lng = (int) (point.getX() * 1E6);

            int deltaLat = lat - prevLat;
            int deltaLng = lng - prevLng;

            encodeValue(deltaLat, encoded);

            encodeValue(deltaLng, encoded);

            prevLat = lat;
            prevLng = lng;
        }

        return encoded.toString();
    }

    private static void encodeValue(int value, StringBuilder encoded) {
        value = (value < 0) ? ~(value << 1) : (value << 1);

        while (value >= 0x20) {
            encoded.append((char) ((value & 0x1f) + 63 + 32));
            value >>= 5;
        }
        encoded.append((char) (value + 63));
    }
}
