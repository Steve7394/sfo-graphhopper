package com.graphhopper.sfo.rgeocode.dto;

import static com.graphhopper.util.Helper.round;

public class SnapRequest {
    private double lat;
    private double lon;
    private boolean forceEdge = true;

    public SnapRequest() {
    }
    public SnapRequest(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = round(lat, 7);
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = round(lon, 7);
    }

    public boolean isForceEdge() {
        return forceEdge;
    }

    public void setForceEdge(boolean forceEdge) {
        this.forceEdge = forceEdge;
    }

    @Override
    public String toString() {
        return "lat: " + this.lat + ", lon: " + this.lon;
    }
}
