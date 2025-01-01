package com.graphhopper.reader.osm;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ReaderAdministrativePolygons implements IReaderAdministrativePolygons {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderAdministrativePolygons.class);
    private final Map<Long, LongArrayList> wayNodes = new HashMap<>(100);

    public ReaderAdministrativePolygons() {
    }

    @Override
    public void handleNode(ReaderNode node, int pass) {
    }

    @Override
    public void handleWay(ReaderWay way, int pass) {
        if (filterWay(way)) {
            if (pass == 1) {
                pass1HandleWay(way);
            } else if (pass == 2) {
                pass2HandleWay(way);
            }
        }
    }

    @Override
    public void handleRelation(ReaderRelation relation, int pass) {
        if (filterRelation(relation)) {
            if (pass == 1) {
                pass1HandleRelation(relation);
            } else if (pass == 2) {
                pass2HandleRelation(relation);
            }
        }
    }

    private void pass1HandleRelation(ReaderRelation relation) {
    }

    private void pass2HandleRelation(ReaderRelation way) {
    }

    private void pass1HandleWay(ReaderWay way) {
        wayNodes.put(way.getId(), way.getNodes());
    }
    private void pass2HandleWay(ReaderWay way) {
    }

    private boolean filterWay(ReaderWay way) {
        return way.hasTag("boundary", "administrative");
    }

    private boolean filterRelation(ReaderRelation relation) {
        return relation.hasTag("type", "boundary");
    }

    private AdministrativeLevel getAdministrativeLevel(ReaderElement element) {
        if (checkInteger(element, "admin_level")) {
            return AdministrativeLevel.findByLevel(Integer.parseInt(element.getTag("admin_level")));
        }
        LOGGER.warn("The " + element.getType().name() + " with id: " + element.getId() + " does not have correct admin_level" +
                "for more information see 'https://wiki.openstreetmap.org/wiki/Tag:boundary%3Dadministrative#admin_level'");
        return null;
    }

    private boolean checkInteger(ReaderElement element, String key) {
        String value = element.getTag(key);
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            LOGGER.warn("The " + element.getType().name() + ", with id: " + element.getId() + "does not have integer value of " + key.toUpperCase());
            return false;
        }
    }

}
