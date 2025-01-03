package com.graphhopper.reader.osm;

import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReaderAdministrativePolygons implements IReaderAdministrativePolygons {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderAdministrativePolygons.class);
    private final Map<Long, List<Double>> nodeCoordinates = new HashMap<>(100);
    private final Map<Long, LongArrayList> wayNodes = new HashMap<>(100);
    private final Map<Long, Map<AdministrativeRelationMemberType, LongArrayList>> relationWays = new HashMap<>(100);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, List<ObjectNode>> levelToFeatureCollection = new HashMap<>();

    public ReaderAdministrativePolygons() {
       for (AdministrativeLevel level : AdministrativeLevel.values()){
           levelToFeatureCollection.put(level.getLevel(), new ArrayList<>());
       }
    }

    @Override
    public void handleNode(ReaderNode node, int pass) {
        if (pass == 1) {
            pass1HandeNode(node);
        } else if (pass == 2) {
            pass2HandeNode(node);
        }
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

    @Override
    public void flush() {
        levelToFeatureCollection.forEach((k, v) -> {
            createGeojsonFeatureCollection(v, AdministrativeLevel.findByLevel(k).getName());
        });
        this.levelToFeatureCollection.clear();
        this.wayNodes.clear();
        this.relationWays.clear();
        this.nodeCoordinates.clear();
    }

    private void pass1HandeNode(ReaderNode node) {
    }

    private void pass2HandeNode(ReaderNode node) {
        List<Double> coordinates = nodeCoordinates.getOrDefault(node.getId(), null);
        if (coordinates == null) return;
        coordinates.add(node.getLon());
        coordinates.add(node.getLat());
    }

    private void pass1HandleWay(ReaderWay way) {
        wayNodes.put(way.getId(), way.getNodes());
        for (LongCursor nodeIdCursor : way.getNodes()) {
            nodeCoordinates.put(nodeIdCursor.value, new ArrayList<>(2));
        }
    }

    private void pass2HandleWay(ReaderWay way) {
    }

    private void pass1HandleRelation(ReaderRelation relation) {
        Map<AdministrativeRelationMemberType, LongArrayList> members = new HashMap<AdministrativeRelationMemberType, LongArrayList>();
        members.put(AdministrativeRelationMemberType.INNER, new LongArrayList());
        members.put(AdministrativeRelationMemberType.OUTER, new LongArrayList());
        relationWays.put(relation.getId(), members);
        relation.getMembers().forEach(m -> {
            if (m.getType().equals(ReaderElement.Type.WAY)) {
                if (m.getRole().equals(AdministrativeRelationMemberType.OUTER.getName())) {
                    members.get(AdministrativeRelationMemberType.OUTER).add(m.getRef());
                } else if (m.getRole().equals(AdministrativeRelationMemberType.INNER.getName())) {
                    members.get(AdministrativeRelationMemberType.INNER).add(m.getRef());
                }
            }
        });
    }

    private void pass2HandleRelation(ReaderRelation relation) {
        Map<AdministrativeRelationMemberType, LongArrayList> members = relationWays.getOrDefault(relation.getId(), null);
        LongArrayList outerMembers = members.get(AdministrativeRelationMemberType.OUTER);
        LongArrayList innerMembers = members.get(AdministrativeRelationMemberType.INNER);
        AdministrativeLevel level = getAdministrativeLevel(relation);
        if (level == null) return;
        List<List<List<Double>>> coordinates = new ArrayList<>();
        if (outerMembers.size() != 0) {
            coordinates.add(fetchGeometry(outerMembers));
        }
        if (innerMembers.size() != 0) {
            coordinates.add(fetchGeometry(innerMembers));
        }
        if (coordinates.size() == 0) return;
        List<ObjectNode> features = levelToFeatureCollection.getOrDefault(level.getLevel(), null);
        if (features == null) return;
        features.add(
                createGeojsonFeature(
                        coordinates,
                        level.getName(),
                        relation.getTag("name", level.getName()),
                        relation.getId()
                )
        );
    }

    private List<List<Double>> fetchGeometry(LongArrayList wayIds) {
        if (wayIds.size() == 0) return null;
        List<List<Double>> output = new ArrayList<>(wayIds.size());
        LongArrayList nodes;
        for (LongCursor wayId : wayIds) {
            nodes = wayNodes.getOrDefault(wayId.value, null);
            if (nodes == null) return output;
            for (LongCursor nodeId : wayNodes.get(wayId.value)) {
                output.add(nodeCoordinates.get(nodeId.value));
            }
        }
        return output;
    }

    private ObjectNode createGeojsonFeatureCollection(List<ObjectNode> features, String name) {
        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        ArrayNode featureArray = mapper.createArrayNode();
        for (ObjectNode feature : features) {
            featureArray.add(feature);
        }
        featureCollection.set("features", featureArray);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(name + "s" + ".geojson"), featureCollection);
        } catch (IOException e) {
            throw new RuntimeException("Can not create geojson file");
        }
        return featureCollection;
    }

    private ObjectNode createGeojsonFeature(List<List<List<Double>>> coordinatesIn, String level, String name, Long osmId) {
        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");

        ObjectNode geometry = mapper.createObjectNode();
        geometry.put("type", "Polygon");
        ArrayNode coordinates = mapper.createArrayNode();
        for (List<List<Double>> ring : coordinatesIn) {
            ArrayNode ringNode = mapper.createArrayNode();
            for (List<Double> point : ring) {
                ArrayNode pointNode = mapper.createArrayNode();
                pointNode.add(point.get(0));
                pointNode.add(point.get(1));
                ringNode.add(pointNode);
            }
            coordinates.add(ringNode);
        }
        geometry.set("coordinates", coordinates);
        feature.set("geometry", geometry);
        ObjectNode properties = mapper.createObjectNode();
        properties.put("level", level);
        properties.put("name", name);
        properties.put("osm_id", osmId);
        feature.set("properties", properties);
        return feature;
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
        LOGGER.warn("The " + element.getType().name() + " with id: " + element.getId() + " does not have correct admin_level: " + element.getTag("admin_level")  +
                " for more information see 'https://wiki.openstreetmap.org/wiki/Tag:boundary%3Dadministrative#admin_level'");
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
