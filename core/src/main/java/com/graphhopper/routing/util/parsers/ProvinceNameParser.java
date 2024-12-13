package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.StringEncodedValue;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.storage.IntsRef;

public class ProvinceNameParser extends AdministrativeParser{
    public static final String KEY = "province_name";
    private static final String ADMIN_TYPE = "province";
    private final StringEncodedValue provinceNameEn;

    public ProvinceNameParser(EncodedValueLookup encodedValueLookup) {
        this.provinceNameEn = encodedValueLookup.getStringEncodedValue(KEY);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        CustomArea provinceArea = getAdmissionArea(way, ADMIN_TYPE);
        provinceNameEn.setString(false, edgeId, edgeIntAccess, (String) provinceArea.getProperties().get(NAME_TAG));
    }
}
