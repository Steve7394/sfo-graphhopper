package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.storage.IntsRef;

public class CityOsmIdParser extends AdministrativeParser{
    public static final String KEY = "city_osm_id";
    private static final String ADMIN_TYPE = "city";
    private final IntEncodedValue cityOsmIdEnc;

    public CityOsmIdParser(EncodedValueLookup encodedValueLookup) {
        this.cityOsmIdEnc = encodedValueLookup.getIntEncodedValue(KEY);
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        CustomArea cityArea = getAdmissionArea(way, ADMIN_TYPE);
        long cityOsmId = Long.parseLong(String.valueOf(cityArea.getProperties().get(OSM_ID_TAG)));
        validateAdminOsmIdLong(cityOsmIdEnc, cityOsmId);
        cityOsmIdEnc.setInt(false, edgeId, edgeIntAccess, Math.toIntExact(cityOsmId));
    }
}
