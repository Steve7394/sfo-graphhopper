package com.graphhopper.sfo.rgeocode;

import static com.graphhopper.util.Helper.round;

public class SnapRequest {
    private Double lat;
    private Double lon;

    public SnapRequest() {
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = round(lat, 7);
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = round(lon, 7);
    }
}
