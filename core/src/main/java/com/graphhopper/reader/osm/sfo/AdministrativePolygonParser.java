package com.graphhopper.reader.osm.sfo;

import com.graphhopper.reader.ReaderElement;
import com.graphhopper.reader.ReaderNode;
import com.graphhopper.reader.ReaderRelation;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.reader.osm.*;
import com.graphhopper.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;


public class AdministrativePolygonParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaySegmentParser.class);
    private int workerThreads = 2;
    private final IReaderAdministrativePolygons adminReader;

    public AdministrativePolygonParser(IReaderAdministrativePolygons adminReader) {
        this.adminReader = adminReader;
    }


    public void readOSM(File osmFile) {
        LOGGER.info("Start reading administrative polygons from OSM file: '" + osmFile + "'");
        LOGGER.info("pass A1 - start");
        StopWatch sw1 = StopWatch.started();
        readOSM(osmFile, new Pass1Handler(), new SkipOptions(true, true, false));
        LOGGER.info("pass A1 - finished, took: {}", sw1.stop().getTimeString());

        LOGGER.info("pass A2 - start");
        StopWatch sw2 = new StopWatch().start();
        readOSM(osmFile, new Pass2Handler(), new SkipOptions(true, false, true));
        LOGGER.info("pass A2 - finished, took: {}", sw2.stop().getTimeString());

        LOGGER.info("pass A3 - start");
        StopWatch sw3 = new StopWatch().start();
        readOSM(osmFile, new Pass3Handler(), new SkipOptions(false, true, true));
        LOGGER.info("pass A3 - finished, took: {}", sw3.stop().getTimeString());


        LOGGER.info("Finished reading administrative polygons from OSM file." +
                " pass A1: " + (int) sw1.getSeconds() + "s, " +
                " pass A2: " + (int) sw2.getSeconds() + "s, " +
                " total: " + (int) (sw1.getSeconds() + sw2.getSeconds()) + "s");
    }

    private void readOSM(File file, ReaderElementHandler handler, SkipOptions skipOptions) {
        try (OSMInput osmInput = openOsmInputFile(file, skipOptions)) {
            ReaderElement elem;
            while ((elem = osmInput.getNext()) != null)
                handler.handleElement(elem);
            handler.onFinish();
            if (osmInput.getUnprocessedElements() > 0)
                throw new IllegalStateException("There were some remaining elements in the reader queue " + osmInput.getUnprocessedElements());
        } catch (Exception e) {
            throw new RuntimeException("Could not parse OSM file: " + file.getAbsolutePath(), e);
        }
    }

    protected OSMInput openOsmInputFile(File osmFile, SkipOptions skipOptions) throws XMLStreamException, IOException {
        return new OSMInputFile(osmFile).setWorkerThreads(workerThreads).setSkipOptions(skipOptions).open();
    }

    private interface ReaderElementHandler {
        default void handleElement(ReaderElement elem) throws ParseException {
            switch (elem.getType()) {
                case NODE:
                    handleNode((ReaderNode) elem);
                    break;
                case WAY:
                    handleWay((ReaderWay) elem);
                    break;
                case RELATION:
                    handleRelation((ReaderRelation) elem);
                    break;
                case FILEHEADER:
                    handleFileHeader((OSMFileHeader) elem);
                    break;
                default:
                    throw new IllegalStateException("Unknown reader element type: " + elem.getType());
            }
        }

        default void handleNode(ReaderNode node) {
        }

        default void handleWay(ReaderWay way) {
        }

        default void handleRelation(ReaderRelation relation) {
        }

        default void handleFileHeader(OSMFileHeader fileHeader) throws ParseException {
        }

        default void onFinish() {
        }
    }

    private class Pass1Handler implements ReaderElementHandler {
        private boolean handledRelations;

        @Override
        public void handleRelation(ReaderRelation relation) {
            if (!handledRelations) {
                LOGGER.info("pass A1 - start reading OSM relations");
                handledRelations = true;
            }

            adminReader.handleRelation(relation);
        }

        @Override
        public void onFinish() {
            LOGGER.info("pass A1 - finished");
        }
    }

    private class Pass2Handler implements ReaderElementHandler {
        private boolean handledWays;

        @Override
        public void handleWay(ReaderWay way) {
            if (!handledWays) {
                LOGGER.info("pass A2 - start reading OSM ways");
                handledWays = true;
            }

            adminReader.handleWay(way);
        }

        @Override
        public void onFinish() {
            LOGGER.info("pass A2 - finished");
        }
    }
    private class Pass3Handler implements ReaderElementHandler {
        private boolean handledNodes;
        @Override
        public void handleNode(ReaderNode node) {
            if (!handledNodes) {
                LOGGER.info("pass A3 - start reading OSM nodes");
                handledNodes = true;
            }

            adminReader.handleNode(node);
        }

        @Override
        public void onFinish() {
            adminReader.onFinish();
            LOGGER.info("pass A3 - finished");
        }
    }

}
