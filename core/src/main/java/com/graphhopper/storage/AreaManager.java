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

import java.io.IOException;
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

    public AreaManager(List<CustomArea> customPolygons, List<CustomArea> administrativePolygons, String mainDir, String tempDir) {
        this.customPolygons = customPolygons;
        this.administrativePolygons = administrativePolygons;
        this.areaIndex = buildAreaIndex();
        this.mainFileManager = new CustomAreaFileManager(mainDir, "polygon");
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
        String id = String.valueOf(area.getProperties().get("id"));
        if (mainFileManager.write(id, area)){
            if (tempFileManager.write(id, area)){
                applyChange(baseGraph, encodingManager, area, id, false);
                this.customPolygons.add(area);
                isChanged = true;
                return;
            }else{
                mainFileManager.delete(id);
                throw new UncheckedIOException(new IOException("Can not write file on: " + tempFileManager.getDirectory()));
            }
        }
        throw new UncheckedIOException(new IOException("Can not write file on: " + mainFileManager.getDirectory()));
    }

    public void removeCustomPolygon(String id, BaseGraph baseGraph, EncodingManager encodingManager) {
        CustomArea removedCandidate = getCustomArea(id);
        if (mainFileManager.delete(id)){
            if (tempFileManager.delete(id)){
                applyChange(baseGraph, encodingManager, removedCandidate, id, true);
                this.customPolygons.remove(removedCandidate);
                isChanged = true;
                return;
            }else{
                if (tempFileManager.logicalRemove(id, removedCandidate)){
                    applyChange(baseGraph, encodingManager, removedCandidate, id, true);
                    this.customPolygons.remove(removedCandidate);
                    isChanged = true;
                    return;
                }else{
                    mainFileManager.write(id, removedCandidate);
                    throw new UncheckedIOException(new IOException("Can not remove or write file on: " + tempFileManager.getDirectory()));
                }
            }
        }
        throw new UncheckedIOException(new IOException("Can not remove file on: " + mainFileManager.getDirectory()));
    }

    private CustomArea getCustomArea(String id){
        CustomArea candidate = this.customPolygons.stream().filter(p -> String.valueOf(p.getProperties().get("id")).equals(id)).findFirst().orElse(null);
        if (candidate == null) {
            throw new RuntimeException("there is no custom polygon with id: " + id);
        }
        return candidate;
    }

    public void updateCustomPolygon(String id, CustomArea area, BaseGraph baseGraph, EncodingManager encodingManager){
        try {
            getCustomArea(id);
        } catch (RuntimeException e) {
            LOGGER.warn(e.getLocalizedMessage());
            addCustomPolygon(area, baseGraph, encodingManager);
            return;
        }
        this.removeCustomPolygon(id, baseGraph, encodingManager);
        this.addCustomPolygon(area, baseGraph, encodingManager);
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
        tempFileManager.getAllCustomAreas(false).forEach(a -> {
            applyChange(baseGraph, encodingManager, a,  String.valueOf(a.getProperties().get("id")), false);
        });
        tempFileManager.getAllCustomAreas(true).forEach(a -> {
            applyChange(baseGraph, encodingManager, a,  String.valueOf(a.getProperties().get("id")), true);
        });
    }


}
