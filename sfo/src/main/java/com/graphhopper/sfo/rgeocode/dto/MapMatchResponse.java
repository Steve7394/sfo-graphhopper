package com.graphhopper.sfo.rgeocode.dto;

public class MapMatchResponse {
    private SnapResponse snapResponse;
    private Double distance;
    private Double time;
    private Double averageMaxSpeed;

    public MapMatchResponse() {
    }

    public SnapResponse getSnapResponse() {
        return snapResponse;
    }

    public void setSnapResponse(SnapResponse snapResponse) {
        this.snapResponse = snapResponse;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getAverageMaxSpeed() {
        return averageMaxSpeed;
    }

    public void setAverageMaxSpeed(double averageMaxSpeed) {
        this.averageMaxSpeed = averageMaxSpeed;
    }
}
