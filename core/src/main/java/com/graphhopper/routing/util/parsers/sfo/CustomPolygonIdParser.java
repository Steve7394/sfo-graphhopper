package com.graphhopper.routing.util.parsers.sfo;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

import java.util.ArrayList;
import java.util.List;

public class CustomPolygonIdParser implements TagParser {
    public static final String KEY = "custom_polygon";
    private final IntEncodedValue encoder;

    public CustomPolygonIdParser(EncodedValueLookup encodedValueLookup) {
        this.encoder = encodedValueLookup.getIntEncodedValue(KEY);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        List<CustomArea> areas = way.getTag("custom_areas", new ArrayList<>());
        if (areas.size() <= 1) {
            encoder.setInt(false, edgeId, edgeIntAccess, 0);
            return;
        }
        CustomArea area = areas.stream().filter(a -> a.getProperties().containsKey("custom") && a.getProperties().containsKey("id")).findFirst().orElse(null);
        if (area == null) {
            encoder.setInt(false, edgeId, edgeIntAccess, 0);
            return;
        }
        encoder.setInt(false, edgeId, edgeIntAccess, Integer.parseInt(String.valueOf(area.getProperties().get("id"))));
    }
}
