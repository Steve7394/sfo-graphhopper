package com.graphhopper.reader.osm;

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;

public interface IReaderAdministrativePolygons {
    void handleNode(ReaderNode node, int pass);

    void handleWay(ReaderWay way, int pass);

    void handleRelation(ReaderRelation relation, int pass);

}
