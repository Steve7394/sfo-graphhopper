package com.graphhopper.reader.osm;

public enum AdministrativeRelationMemberType {
    OUTER("outer"),
    INNER("inner");

    private final String name;

    AdministrativeRelationMemberType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
