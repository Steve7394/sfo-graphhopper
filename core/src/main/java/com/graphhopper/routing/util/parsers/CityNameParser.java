package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.StringEncodedValue;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.storage.IntsRef;

public class CityNameParser extends AdministrativeParser{
    public static final String KEY = "city_name";
    private static final String ADMIN_TYPE = "city";
    private final StringEncodedValue cityNameEnc;

    public CityNameParser(EncodedValueLookup encodedValueLookup) {
        this.cityNameEnc = encodedValueLookup.getStringEncodedValue(KEY);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        CustomArea cityArea = getAdmissionArea(way, ADMIN_TYPE);
        if (cityArea == null){
            cityNameEnc.setString(false, edgeId, edgeIntAccess, "missing");
            return;
        }
        cityNameEnc.setString(false, edgeId, edgeIntAccess, (String) cityArea.getProperties().get(NAME_TAG));
    }
}
