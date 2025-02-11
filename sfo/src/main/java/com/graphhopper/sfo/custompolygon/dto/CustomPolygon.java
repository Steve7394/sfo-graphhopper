package com.graphhopper.sfo.custompolygon.dto;

import javax.validation.constraints.NotNull;

public class CustomPolygon {

    @NotNull
    private int id;

    @NotNull
    private String geometry;

    public CustomPolygon(){}

    public CustomPolygon(int id, String geometry) {
        this.id = id;
        this.geometry = geometry;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
