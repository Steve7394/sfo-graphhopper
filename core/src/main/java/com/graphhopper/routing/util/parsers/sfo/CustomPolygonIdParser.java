package com.graphhopper.routing.util.parsers.sfo;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.StringEncodedValue;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomPolygonIdParser implements TagParser {
    public static final String KEY = "custom_polygon";
    private final StringEncodedValue encoder;

    public CustomPolygonIdParser(EncodedValueLookup encodedValueLookup) {
        this.encoder = encodedValueLookup.getStringEncodedValue(KEY);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        List<CustomArea> areas = way.getTag("custom_areas", new ArrayList<>());
        if (areas.size() <= 1) {
            encoder.setString(false, edgeId, edgeIntAccess, "0");
            return;
        }
        List<CustomArea> custom_areas = areas.stream().filter(a -> a.getProperties().containsKey("custom") && a.getProperties().containsKey("id")).collect(Collectors.toList());
        if (custom_areas.size() == 0) {
            encoder.setString(false, edgeId, edgeIntAccess, "0");
            return;
        }
        encoder.setString(false, edgeId, edgeIntAccess, encode(custom_areas));
    }

    private String encode(List<CustomArea> areas){
        return areas.stream().map(a -> String.valueOf(a.getProperties().get("id"))).collect(Collectors.joining(","));
    }
}
