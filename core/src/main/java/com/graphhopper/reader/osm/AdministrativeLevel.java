package com.graphhopper.reader.osm;

import java.util.Arrays;
import java.util.Optional;

public enum AdministrativeLevel {
    PROVINCE("province", 4),
    COUNTY("county", 5),
    DISTRICT("district", 6),
    CITY_MUNICIPALITY("city_municipality", 7),
    VILLAGE("village", 8),
    SUBURB("village", 9),
    SUBAREA("subarea", 10),
    NEIGHBOURHOOD("neighbourhood", 11);

    private final String name;
    private final int level;
    AdministrativeLevel(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public String getName(){
        return this.name;
    }

    public int getLevel(){
        return this.level;
    }

    public static AdministrativeLevel findByLevel(int level){
        Optional<AdministrativeLevel> optionalAL = Arrays.stream(AdministrativeLevel.values()).filter(a -> a.level == level).findFirst();
        return optionalAL.orElse(null);
    }
}
