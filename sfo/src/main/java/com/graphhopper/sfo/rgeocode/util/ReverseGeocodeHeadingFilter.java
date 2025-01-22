package com.graphhopper.sfo.rgeocode.util;

import com.graphhopper.routing.util.DirectedEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;

public class ReverseGeocodeHeadingFilter implements EdgeFilter {
    private final double heading;
    private final DirectedEdgeFilter directedEdgeFilter;
    private final GHPoint pointNearHeading;

    public ReverseGeocodeHeadingFilter(double heading, GHPoint pointNearHeading) {
        this.directedEdgeFilter = (edgeState, reverse) ->  true;;
        this.heading = heading;
        this.pointNearHeading = pointNearHeading;
    }

    @Override
    public boolean accept(EdgeIteratorState edgeState) {
        final double tolerance = 80;
        final double maxDistance = 100;
        double headingOfEdge = getHeadingOfGeometryNearPoint(edgeState, pointNearHeading, maxDistance);
        if (Double.isNaN(headingOfEdge))
            return false;
        return Math.abs(headingOfEdge - heading) < tolerance && directedEdgeFilter.accept(edgeState, false);
    }

    static double getHeadingOfGeometryNearPoint(EdgeIteratorState edgeState, GHPoint point, double maxDistance) {
        final DistanceCalc calcDist = DistanceCalcEarth.DIST_EARTH;
        double closestDistance = Double.POSITIVE_INFINITY;
        PointList points = edgeState.fetchWayGeometry(FetchMode.ALL);
        int closestPoint = -1;
        for (int i = 1; i < points.size(); i++) {
            double fromLat = points.getLat(i - 1), fromLon = points.getLon(i - 1);
            double toLat = points.getLat(i), toLon = points.getLon(i);
            double distance = calcDist.validEdgeDistance(point.lat, point.lon, fromLat, fromLon, toLat, toLon)
                    ? calcDist.calcDenormalizedDist(calcDist.calcNormalizedEdgeDistance(point.lat, point.lon, fromLat, fromLon, toLat, toLon))
                    : calcDist.calcDist(fromLat, fromLon, point.lat, point.lon);
            if (i == points.size() - 1)
                distance = Math.min(distance, calcDist.calcDist(toLat, toLon, point.lat, point.lon));
            if (distance > maxDistance)
                continue;
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = i;
            }
        }
        if (closestPoint < 0)
            return Double.NaN;

        double fromLat = points.getLat(closestPoint - 1), fromLon = points.getLon(closestPoint - 1);
        double toLat = points.getLat(closestPoint), toLon = points.getLon(closestPoint);
        return AngleCalc.ANGLE_CALC.calcAzimuth(fromLat, fromLon, toLat, toLon);
    }

}
