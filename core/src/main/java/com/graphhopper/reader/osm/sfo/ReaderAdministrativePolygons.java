package com.graphhopper.reader.osm.sfo;

import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.AdministrativeRelationMemberType;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReaderAdministrativePolygons implements IReaderAdministrativePolygons {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderAdministrativePolygons.class);
    private final Map<Long, List<Double>> nodeCoordinates = new HashMap<>(100);
    private final Map<Long, LongArrayList> wayNodes = new HashMap<>(100);
    private final Map<Long, Map<AdministrativeRelationMemberType, LongArrayList>> relationWays = new HashMap<>(100);
    private final Map<Long, Tags> relationsTags = new HashMap<>(100);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, List<ObjectNode>> levelToFeatureCollection = new HashMap<>();
    private String osmAreaDirectory;
    private final DistanceCalcEarth distanceCalc = new DistanceCalcEarth();

    public ReaderAdministrativePolygons(String osmAreaDirectory) {
        for (AdministrativeLevel level : AdministrativeLevel.values()) {
            levelToFeatureCollection.put(level.getLevel(), new ArrayList<>());
        }
        this.osmAreaDirectory = osmAreaDirectory;
    }

    @Override
    public void handleNode(ReaderNode node) {
        List<Double> coordinates = nodeCoordinates.getOrDefault(node.getId(), null);
        if (coordinates == null) return;
        coordinates.add(Helper.round(node.getLon(), 7));
        coordinates.add(Helper.round(node.getLat(), 7));
    }

    @Override
    public void handleWay(ReaderWay way) {
        if (filterWay(way)) {
            wayNodes.put(way.getId(), way.getNodes());
            for (LongCursor nodeIdCursor : way.getNodes()) {
                nodeCoordinates.put(nodeIdCursor.value, new ArrayList<>(2));
            }
        }
    }

    @Override
    public void handleRelation(ReaderRelation relation) {
        if (filterRelation(relation)) {
            AdministrativeLevel level = getAdministrativeLevel(relation);
            if (level == null) return;
            String name = relation.getTag("name", level.getName());
            Map<AdministrativeRelationMemberType, LongArrayList> members = new HashMap<>();
            members.put(AdministrativeRelationMemberType.INNER, new LongArrayList());
            members.put(AdministrativeRelationMemberType.OUTER, new LongArrayList());
            relationWays.put(relation.getId(), members);
            relationsTags.put(relation.getId(), new Tags(name, level));
            for (ReaderRelation.Member m : relation.getMembers()) {
                if (m.getType().equals(ReaderElement.Type.WAY)) {
                    if (m.getRole().equals(AdministrativeRelationMemberType.OUTER.getName())) {
                        members.get(AdministrativeRelationMemberType.OUTER).add(m.getRef());
                        wayNodes.put(m.getRef(), new LongArrayList());
                    } else if (m.getRole().equals(AdministrativeRelationMemberType.INNER.getName())) {
                        members.get(AdministrativeRelationMemberType.INNER).add(m.getRef());
                        wayNodes.put(m.getRef(), new LongArrayList());
                    }
                }
            }
        }
    }

    @Override
    public void onFinish() {
        for (Long relationId : relationsTags.keySet()) {
            Map<AdministrativeRelationMemberType, LongArrayList> members = relationWays.getOrDefault(relationId, null);
            Tags tags = relationsTags.getOrDefault(relationId, null);
            if (members == null || tags == null) continue;
            LongArrayList outerMembers = members.get(AdministrativeRelationMemberType.OUTER);
            LongArrayList innerMembers = members.get(AdministrativeRelationMemberType.INNER);
            List<List<List<Double>>> outerCoordinates = new ArrayList<>();
            List<List<List<Double>>> innerCoordinates = new ArrayList<>();
            if (!outerMembers.isEmpty()) {
                outerCoordinates = fetchGeometry(outerMembers, true);
            }
            if (!innerMembers.isEmpty()) {
                innerCoordinates = fetchGeometry(innerMembers, false);
            }
            if (innerCoordinates == null && outerCoordinates == null) {
                continue;
            }
            List<ObjectNode> features = levelToFeatureCollection.getOrDefault(tags.level.getLevel(), null);
            if (features == null) continue;
            ObjectNode feature = createGeojsonFeature(
                    outerCoordinates,
                    innerCoordinates,
                    tags.level.getName(),
                    tags.name,
                    relationId
            );
            if (feature == null) continue;
            if (!AdministrativeUtils.isValid(AdministrativeUtils.convertGeojsonNodeToMultiPolygon(feature))){
                LOGGER.warn("The relation with Id " + relationId + " does not have valid geometry");
                continue;
            };
            features.add(feature);
        }
        flush();
    }


    public void flush() {
        levelToFeatureCollection.forEach((k, v) -> {
            createGeojsonFeatureCollection(v, AdministrativeLevel.findByLevel(k).getName());
        });
        this.levelToFeatureCollection.clear();
        this.wayNodes.clear();
        this.relationWays.clear();
        this.nodeCoordinates.clear();
    }

    private boolean isCounterClockwise(List<List<Double>> coordinates) {
        double sum = 0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            List<Double> p1 = coordinates.get(i);
            List<Double> p2 = coordinates.get(i + 1);
            sum += (p2.get(0) - p1.get(0)) * (p2.get(1) + p1.get(1));
        }
        return sum < 0;
    }

    private List<List<List<Double>>> fetchGeometry(LongArrayList wayIds, boolean isOuter) {
        if (wayIds.size() == 0) return null;
        List<List<List<Double>>> output = new ArrayList<>(wayIds.size());
        List<List<Double>> temp = new ArrayList<>(wayIds.size());
        LongArrayList nodes;
        boolean isCounterClockWise;
        for (LongCursor wayId : wayIds) {
            nodes = wayNodes.getOrDefault(wayId.value, null);
            if (nodes == null || nodes.size() == 0) {
                continue;
            }
            for (LongCursor nodeId : nodes) {
                temp.add(nodeCoordinates.get(nodeId.value));
            }
            isCounterClockWise = isCounterClockwise(temp);
            if ((isCounterClockWise && !isOuter) || !isCounterClockWise && isOuter) {
                Collections.reverse(temp);
            }
            if (temp.isEmpty()) continue;
            output.add(new ArrayList<>(temp));
            temp.clear();
        }
        if (output.isEmpty()) return null;
        return output;
    }

    public List<List<List<Double>>> connectWays(List<List<List<Double>>> ways) {
        List<List<List<Double>>> rings = new ArrayList<>();
        Set<Integer> usedWays = new HashSet<>();

        for (int i = 0; i < ways.size(); i++) {
            if (usedWays.contains(i)) continue;

            List<List<Double>> currentRing = new ArrayList<>(ways.get(i));
            usedWays.add(i);

            boolean connected;
            do {
                connected = false;
                for (int j = 0; j < ways.size(); j++) {
                    if (usedWays.contains(j)) continue;

                    List<List<Double>> way = ways.get(j);

                    // Connect current ring with this way
                    if (currentRing.get(currentRing.size() - 1).equals(way.get(0))) {
                        currentRing.addAll(way.subList(1, way.size()));
                        usedWays.add(j);
                        connected = true;
                    } else if (currentRing.get(currentRing.size() - 1).equals(way.get(way.size() - 1))) {
                        Collections.reverse(way);
                        currentRing.addAll(way.subList(1, way.size()));
                        usedWays.add(j);
                        connected = true;
                    }
                }
            } while (connected);

            if (!currentRing.get(0).equals(currentRing.get(currentRing.size() - 1))) {
                PointList pointList = new PointList();
                pointList.add(currentRing.get(0).get(1), currentRing.get(0).get(0));
                pointList.add(currentRing.get(currentRing.size() - 1).get(1), currentRing.get(currentRing.size() - 1).get(0));
                if (distanceCalc.calcDistance(pointList) > 10000) continue;
                currentRing.add(currentRing.get(0));
            }

            rings.add(currentRing);
        }

        return rings;
    }

    private void createGeojsonFeatureCollection(List<ObjectNode> features, String name) {
        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        ArrayNode featureArray = mapper.createArrayNode();
        for (ObjectNode feature : features) {
            featureArray.add(feature);
        }
        featureCollection.set("features", featureArray);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(osmAreaDirectory + "/" + name + "s" + ".geojson"), featureCollection);
        } catch (IOException e) {
            throw new RuntimeException("Can not create geojson file");
        }
    }

    private ObjectNode createGeojsonFeature(List<List<List<Double>>> outerCoordinates, List<List<List<Double>>> innerCoordinates, String level, String name, Long osmId) {
        List<List<List<Double>>> rings = new ArrayList<>();
        if (outerCoordinates != null) {
            rings.addAll(connectWays(outerCoordinates));
        }
        if (innerCoordinates != null) {
            rings.addAll(connectWays(innerCoordinates));
        }
        if (rings.isEmpty()) return null;
        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");
        feature.put("id", osmId);
        ObjectNode geometry = mapper.createObjectNode();
        geometry.put("type", "MultiPolygon");
        ArrayNode parentCoordinates = mapper.createArrayNode();
        ArrayNode coordinates = mapper.createArrayNode();
        for (List<List<Double>> ring : rings) {
            ArrayNode ringNode = mapper.createArrayNode();
            for (List<Double> point : ring) {
                ArrayNode pointNode = mapper.createArrayNode();
                pointNode.add(point.get(0));
                pointNode.add(point.get(1));
                ringNode.add(pointNode);
            }
            coordinates.add(ringNode);
        }
        parentCoordinates.add(coordinates);
        geometry.set("coordinates", parentCoordinates);
        feature.set("geometry", geometry);
        ObjectNode properties = mapper.createObjectNode();
        properties.put("level", level);
        properties.put("name", name);
        properties.put("osm_id", osmId);
        feature.set("properties", properties);
        return feature;
    }

    private boolean filterWay(ReaderWay way) {
        return wayNodes.containsKey(way.getId());
    }

    private boolean filterRelation(ReaderRelation relation) {
        return relation.hasTag("type", "boundary") && getAdministrativeLevel(relation) != null;
    }

    private AdministrativeLevel getAdministrativeLevel(ReaderElement element) {
        if (checkInteger(element, "admin_level")) {
            return AdministrativeLevel.findByLevel(Integer.parseInt(element.getTag("admin_level")));
        }
        LOGGER.warn("The " + element.getType().name() + " with id: " + element.getId() + " does not have correct admin_level: " + element.getTag("admin_level") +
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

    private static class Tags {
        public String name;
        public AdministrativeLevel level;

        public Tags(String name, AdministrativeLevel level) {
            this.name = name;
            this.level = level;
        }
    }

}
