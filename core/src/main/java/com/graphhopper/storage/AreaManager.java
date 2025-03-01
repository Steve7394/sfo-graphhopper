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
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AreaManager {
    private final List<CustomArea> customPolygons;
    private final List<CustomArea> administrativePolygons;
    private AreaIndex<CustomArea> areaIndex;
    private boolean isChanged = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(AreaManager.class);
    private final CustomAreaFileManager mainFileManager;
    private final CustomAreaFileManager tempFileManager;
    private final static String REMOVE_KEY = "SHOULD_REMOVE";

    public AreaManager(List<CustomArea> customPolygons, List<CustomArea> administrativePolygons, String mainDir, String tempDir) {
        this.customPolygons = customPolygons;
        this.administrativePolygons = administrativePolygons;
        this.areaIndex = buildAreaIndex();
        this.mainFileManager = new CustomAreaFileManager(mainDir, "geojson");
        this.tempFileManager = new CustomAreaFileManager(tempDir, "temp");
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
        String id = String.valueOf(area.getProperties().get("id"));
        applyChange(baseGraph, encodingManager, area, id, false);
        mainFileManager.write(id, area);
        tempFileManager.write(id, area);
    }

    public void removeCustomPolygon(String id, BaseGraph baseGraph, EncodingManager encodingManager) {
        CustomArea removedCandidate = getCustomArea(id);
        this.customPolygons.remove(removedCandidate);
        isChanged = true;
        applyChange(baseGraph, encodingManager, removedCandidate, id, true);
        mainFileManager.delete(id);
        try {
            tempFileManager.delete(id);
        } catch (UncheckedIOException e) {
            removedCandidate.getProperties().put(REMOVE_KEY, "");
            tempFileManager.write(id, removedCandidate);
        }
    }

    private CustomArea getCustomArea(String id){
        CustomArea candidate = this.customPolygons.stream().filter(p -> String.valueOf(p.getProperties().get("id")).equals(id)).findFirst().orElse(null);
        if (candidate == null) {
            throw new RuntimeException("there is no custom polygon with id: " + id);
        }
        return candidate;
    }

    public void updateCustomPolygon(String id, CustomArea area, BaseGraph baseGraph, EncodingManager encodingManager){
        CustomArea candidate;
        try {
            candidate = getCustomArea(id);
        } catch (RuntimeException e) {
            LOGGER.warn(e.getLocalizedMessage());
            addCustomPolygon(area, baseGraph, encodingManager);
            return;
        }
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
        mainFileManager.delete(id);
        mainFileManager.write(id, area);
        try {
            tempFileManager.delete(id);
        }catch (UncheckedIOException e) {
            candidate.getProperties().put(REMOVE_KEY, "");
            tempFileManager.write(id, candidate);
        }finally {
            tempFileManager.write(id, area);
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
        if (values.isEmpty()){
            values.add("0");
        }
        return String.join(",", values);
    }

    private AreaIndex<CustomArea> getTemporaryAreaIndex(CustomArea area) {
        return new AreaIndex<>(List.of(area));
    }

    public void applyTemp(BaseGraph baseGraph, EncodingManager encodingManager){
        tempFileManager.getAllCustomAreas().forEach(a -> {
            applyChange(baseGraph, encodingManager, a,  String.valueOf(a.getProperties().get("id")), a.getProperties().containsKey(REMOVE_KEY));
        });
    }


}
