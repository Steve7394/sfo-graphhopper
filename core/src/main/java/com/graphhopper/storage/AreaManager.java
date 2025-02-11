package com.graphhopper.storage;

import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.sfo.CustomPolygonIdParser;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.List;

public class AreaManager {
    private final List<CustomArea> customPolygons;
    private final List<CustomArea> administrativePolygons;
    private AreaIndex<CustomArea> areaIndex;
    private boolean isChanged = false;

    public AreaManager(List<CustomArea> customPolygons, List<CustomArea> administrativePolygons) {
        this.customPolygons = customPolygons;
        this.administrativePolygons = administrativePolygons;
        this.areaIndex = buildAreaIndex();
    }

    public List<CustomArea> getCustomPolygons(){
        return new ArrayList<>(customPolygons);
    }

    private AreaIndex<CustomArea> buildAreaIndex() {
        List<CustomArea> all = new ArrayList<>(customPolygons.size() + administrativePolygons.size());
        all.addAll(customPolygons);
        all.addAll(administrativePolygons);
        this.areaIndex = new AreaIndex<>(all);
        isChanged = false;
        return this.areaIndex;
    }

    public AreaIndex<CustomArea> getAreaIndex() {
        if (isChanged) {
            return buildAreaIndex();
        } else {
            return areaIndex;
        }
    }

    public void addCustomPolygon(CustomArea area, BaseGraph baseGraph, EncodingManager encodingManager) {
        this.customPolygons.add(area);
        isChanged = true;
        applyChange(baseGraph, encodingManager, area, Integer.parseInt(String.valueOf(area.getProperties().get("id"))));
    }

    public void removeCustomPolygon(int id, BaseGraph baseGraph, EncodingManager encodingManager) {
        CustomArea removedCandidate = getCustomArea(id);
        this.customPolygons.remove(removedCandidate);
        isChanged = true;
        applyChange(baseGraph, encodingManager, removedCandidate, 0);
    }

    private CustomArea getCustomArea(int id){
        CustomArea candidate = this.customPolygons.stream().filter(p -> Integer.parseInt(String.valueOf(p.getProperties().get("id"))) == id).findFirst().orElse(null);
        if (candidate == null) {
            throw new RuntimeException("there is no custom polygon with id: " + id);
        }
        return candidate;
    }

    public void updateCustomPolygon(int id, CustomArea area, BaseGraph baseGraph, EncodingManager encodingManager){
        CustomArea candidate = getCustomArea(id);
        this.customPolygons.remove(candidate);
        this.customPolygons.add(area);
        isChanged = true;
        AllEdgesIterator edge = baseGraph.getAllEdges();
        IntEncodedValue customPolygonEncoder = encodingManager.getIntEncodedValue(CustomPolygonIdParser.KEY);
        AreaIndex<CustomArea> rTempAreaIndex = getTemporaryAreaIndex(candidate);
        AreaIndex<CustomArea> aTempAreaIndex = getTemporaryAreaIndex(area);
        PointList points;
        GHPoint point;
        while (edge.next()) {
            points = edge.fetchWayGeometry(FetchMode.ALL);
            point = points.get(points.size() / 2);
            if (rTempAreaIndex.query(point.getLat(), point.getLon()).size() != 0) {
                edge.set(customPolygonEncoder, 0);
            }
            if (aTempAreaIndex.query(point.getLat(), point.getLon()).size() != 0) {
                edge.set(customPolygonEncoder, id);
            }
        }
    }

    private void applyChange(BaseGraph baseGraph, EncodingManager encodingManager, CustomArea area, int value) {
        AllEdgesIterator edge = baseGraph.getAllEdges();
        IntEncodedValue customPolygonEncoder = encodingManager.getIntEncodedValue(CustomPolygonIdParser.KEY);
        AreaIndex<CustomArea> tempAreaIndex = getTemporaryAreaIndex(area);
        PointList points;
        GHPoint point;
        while (edge.next()) {
            points = edge.fetchWayGeometry(FetchMode.ALL);
            point = points.get(points.size() / 2);
            if (tempAreaIndex.query(point.getLat(), point.getLon()).size() != 0) {
                edge.set(customPolygonEncoder, value);
            }
        }
    }

    private AreaIndex<CustomArea> getTemporaryAreaIndex(CustomArea area) {
        return new AreaIndex<>(List.of(area));
    }


}
