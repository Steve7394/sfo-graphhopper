package com.graphhopper.storage;

import com.graphhopper.routing.ev.StringEncodedValue;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.AreaIndex;
import com.graphhopper.routing.util.CustomArea;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.parsers.sfo.CustomPolygonIdParser;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        applyChange(baseGraph, encodingManager, area, String.valueOf(area.getProperties().get("id")), false);
    }

    public void removeCustomPolygon(String id, BaseGraph baseGraph, EncodingManager encodingManager) {
        CustomArea removedCandidate = getCustomArea(id);
        this.customPolygons.remove(removedCandidate);
        isChanged = true;
        applyChange(baseGraph, encodingManager, removedCandidate, id, true);
    }

    private CustomArea getCustomArea(String id){
        CustomArea candidate = this.customPolygons.stream().filter(p -> String.valueOf(p.getProperties().get("id")).equals(id)).findFirst().orElse(null);
        if (candidate == null) {
            throw new RuntimeException("there is no custom polygon with id: " + id);
        }
        return candidate;
    }

    public void updateCustomPolygon(String id, CustomArea area, BaseGraph baseGraph, EncodingManager encodingManager){
        CustomArea candidate = getCustomArea(id);
        this.customPolygons.remove(candidate);
        this.customPolygons.add(area);
        isChanged = true;
        AllEdgesIterator edge = baseGraph.getAllEdges();
        StringEncodedValue customPolygonEncoder = encodingManager.getStringEncodedValue(CustomPolygonIdParser.KEY);
        AreaIndex<CustomArea> rTempAreaIndex = getTemporaryAreaIndex(candidate);
        AreaIndex<CustomArea> aTempAreaIndex = getTemporaryAreaIndex(area);
        PointList points;
        GHPoint point;
        List<String> values;
        while (edge.next()) {
            points = edge.fetchWayGeometry(FetchMode.ALL);
            point = points.get(points.size() / 2);
            if (rTempAreaIndex.query(point.getLat(), point.getLon()).size() != 0) {
                values = decode(edge, customPolygonEncoder);
                values.remove(id);
                edge.set(customPolygonEncoder, encode(values));
            }
            if (aTempAreaIndex.query(point.getLat(), point.getLon()).size() != 0) {
                values = decode(edge, customPolygonEncoder);
                if (!values.contains(id)) {values.add(id);}
                edge.set(customPolygonEncoder, encode(values));
            }
        }
    }

    private void applyChange(BaseGraph baseGraph, EncodingManager encodingManager, CustomArea area, String value, boolean isRemove) {
        AllEdgesIterator edge = baseGraph.getAllEdges();
        AreaIndex<CustomArea> tempAreaIndex = getTemporaryAreaIndex(area);
        PointList points;
        GHPoint point;
        StringEncodedValue customPolygonEncoder = encodingManager.getStringEncodedValue(CustomPolygonIdParser.KEY);
        while (edge.next()) {
            points = edge.fetchWayGeometry(FetchMode.ALL);
            point = points.get(points.size() / 2);
            if (tempAreaIndex.query(point.getLat(), point.getLon()).size() != 0) {
                List<String> values = decode(edge, customPolygonEncoder);
                if (isRemove) values.remove(value);
                else {
                    if (!values.contains(value)) values.add(value);
                }
                edge.set(customPolygonEncoder, encode(values));
            }
        }
    }

    private List<String> decode(EdgeIterator edgeIterator, StringEncodedValue customPolygonEncoder){
        return Arrays.stream(edgeIterator.get(customPolygonEncoder).split(",")).collect(Collectors.toList());
    }

    private String encode(List<String> values) {
        if (values.size() == 0){
            values.add("0");
        }
        return String.join(",", values);
    }

    private AreaIndex<CustomArea> getTemporaryAreaIndex(CustomArea area) {
        return new AreaIndex<>(List.of(area));
    }


}
