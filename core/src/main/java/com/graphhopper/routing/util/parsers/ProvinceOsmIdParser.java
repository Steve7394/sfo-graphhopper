package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.storage.IntsRef;

public class ProvinceOsmIdParser extends AdministrativeParser {
    public static final String KEY = "province_osm_id";
    private static final String ADMIN_TYPE = "province";
    private final IntEncodedValue provinceOsmIdEnc;

    public ProvinceOsmIdParser(EncodedValueLookup encodedValueLookup) {
        this.provinceOsmIdEnc = encodedValueLookup.getIntEncodedValue(KEY);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        CustomArea provinceArea = getAdmissionArea(way, ADMIN_TYPE);
        if (provinceArea == null){
            provinceOsmIdEnc.setInt(false, edgeId, edgeIntAccess, 0);
            return;
        }
        long provinceOsmId = Long.parseLong(String.valueOf(provinceArea.getProperties().get(OSM_ID_TAG)));
        validateAdminOsmIdLong(provinceOsmIdEnc, provinceOsmId);
        provinceOsmIdEnc.setInt(false, edgeId, edgeIntAccess, Math.toIntExact(provinceOsmId));
    }
}
