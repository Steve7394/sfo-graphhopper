package com.graphhopper.routing.util.parsers.sfo;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.sfo.AdministrativeLevel;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.StringEncodedValue;
import com.graphhopper.storage.IntsRef;

public class CityNameParser extends AdministrativeParser {
    public static final String KEY = AdministrativeLevel.CITY_MUNICIPALITY.getName() + "_name";
    private static final String ADMIN_TYPE = AdministrativeLevel.CITY_MUNICIPALITY.getName();
    private final StringEncodedValue encoder;

    public CityNameParser(EncodedValueLookup encodedValueLookup) {
        this.encoder = encodedValueLookup.getStringEncodedValue(KEY);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        handleName(edgeId, edgeIntAccess, way, ADMIN_TYPE, encoder);
    }
}
