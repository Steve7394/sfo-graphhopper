package com.graphhopper.reader.osm.sfo;

import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;

public interface IReaderAdministrativePolygons {
    void handleNode(ReaderNode node);

    void handleWay(ReaderWay way);

    void handleRelation(ReaderRelation relation);
    void onFinish();
}
