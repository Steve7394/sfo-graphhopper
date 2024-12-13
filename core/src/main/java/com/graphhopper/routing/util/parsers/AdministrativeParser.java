package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.CustomArea;

import java.util.ArrayList;
import java.util.List;

public abstract class AdministrativeParser implements TagParser {
    protected String NAME_TAG = "name";
    protected String OSM_ID_TAG = "osm_id";
    private static final List<String> MAIN_PROPERTIES = List.of("name", "osm_id");


    protected CustomArea getAdmissionArea(ReaderWay way, String administrativeType) {
        List<CustomArea> areas = way.getTag("custom_areas", new ArrayList<>());
        if (areas.size() <= 1) {
            throw new RuntimeException("There is no custom area available");
        }
        CustomArea area = areas.stream().filter(a -> a.getProperties().containsKey("type") && a.getProperties().get("type").equals(administrativeType)).findFirst().orElse(null);
        if (area == null) {
            throw new RuntimeException("The " + administrativeType + " custom area must have type = " + administrativeType + " property");
        }
        validateProperties(area);
        return area;
    }

    protected void validateProperties(CustomArea customArea) {
        MAIN_PROPERTIES.forEach(
                p -> {
                    if (!customArea.getProperties().containsKey(p)) {
                        throw new RuntimeException(customArea.getProperties().get("type") + " area must have " + p + " property");
                    }
                }
        );
    }

    protected void validateAdminOsmIdLong(IntEncodedValue encoder, long osmId) {
        if (osmId > encoder.getMaxStorableInt())
            throw new IllegalArgumentException("Cannot store OSM admin ID: " + osmId + " as it is too large (> "
                    + encoder.getMaxStorableInt() + "). Check " + encoder.getName());
    }
}
