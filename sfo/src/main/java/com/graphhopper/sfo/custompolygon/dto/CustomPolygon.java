package com.graphhopper.sfo.custompolygon.dto;

import javax.validation.constraints.NotNull;

public class CustomPolygon {

    @NotNull
    private Long id;

    @NotNull
    private String geometry;

    public CustomPolygon(){}

    public CustomPolygon(long id, String geometry) {
        this.id = id;
        this.geometry = geometry;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
