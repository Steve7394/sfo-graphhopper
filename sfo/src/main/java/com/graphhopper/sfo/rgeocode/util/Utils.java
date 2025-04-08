package com.graphhopper.sfo.rgeocode.util;


import com.graphhopper.jackson.Gpx;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.util.shapes.GHPoint;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<GHPoint> getPointsFromRequest(HttpServletRequest httpServletRequest, String urlStart) {
        String url = httpServletRequest.getRequestURI();
        if (!url.startsWith(urlStart)) throw new IllegalArgumentException("Incorrect URL " + url);
        url = url.substring(urlStart.length());
        String[] pointStrings = url.split(";");
        List<GHPoint> points = new ArrayList<>(pointStrings.length);
        for (String pointString : pointStrings) {
            points.add(GHPoint.fromStringLonLat(pointString));
        }
        return points;
    }

    public static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double y = Math.sin(deltaLambda) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2) -
                Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    public static double calculateOverallBearing(List<GHPoint> points) {
        if (points.size() < 2) {
            throw new IllegalArgumentException("At least two points are required to calculate a bearing.");
        }

        List<Double> bearings = new ArrayList<>();
        List<Double> weights = new ArrayList<>();

        int n = points.size();
        for (int i = 0; i < n - 1; i++) {
            GHPoint p1 = points.get(i);
            GHPoint p2 = points.get(i + 1);
            double bearing = calculateBearing(p1.getLat(), p1.getLon(), p2.getLat(), p2.getLon());

            bearings.add(bearing);
            weights.add((double) (i + 1));
        }

        double totalWeight = 0;
        double weightedSum = 0;
        for (int i = 0; i < bearings.size(); i++) {
            double weight = weights.get(i);
            weightedSum += bearings.get(i) * weight;
            totalWeight += weight;
        }

        return weightedSum / totalWeight;
    }

    public static Gpx createGpxFromPointList(List<GHPoint> points){
        Gpx gpx = new Gpx();
        Gpx.Trk trk = new Gpx.Trk();
        Gpx.Trkseg trkseg = new Gpx.Trkseg();
        for (GHPoint point : points) {
            Gpx.Trkpt trkpt = new Gpx.Trkpt();
            trkpt.lat = point.getLat();
            trkpt.lon = point.getLon();
            trkseg.trkpt.add(trkpt);
        }
        trk.trkseg.add(trkseg);
        gpx.trk.add(trk);
        return gpx;
    }

    public static List<CustomArea> findPolygons(double lat, double lon, List<CustomArea> customAreas) {
        return new AreaIndex<>(customAreas).query(lat, lon);
    }

}
