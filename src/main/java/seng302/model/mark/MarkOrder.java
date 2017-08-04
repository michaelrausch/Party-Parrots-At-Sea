package seng302.model.mark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.model.stream.xml.generator.Race;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class to hold the order of the marks in the race.
 */
public class MarkOrder {
    private List<Mark> raceMarkOrder;
    private Logger logger = LoggerFactory.getLogger(MarkOrder.class);

    public MarkOrder(){
        loadRaceProperties();
    }

    /**
     * @return An ordered list of marks in the race
     *         OR null if the mark order could not be loaded
     */
    public List<Mark> getMarkOrder(){
        if (raceMarkOrder == null){
            logger.warn("Race order accessed but not instantiated");
            return null;
        }

        return Collections.unmodifiableList(raceMarkOrder);
    }

    /**
     * Returns the mark in the race after the previous mark
     * @param position The current race position
     * @return the next race position
     *         OR null if there is no position
     */
    public RacePosition getNextPosition(RacePosition position){
        Mark previousMark = position.getNextMark();
        Mark nextMark;

        if (position.getPositionIndex() + 1 >= raceMarkOrder.size() - 1){
            RacePosition nextRacePosition = new RacePosition(raceMarkOrder.size() - 1, null, previousMark);
            nextRacePosition.setFinishingLeg();

            return nextRacePosition;
        }

        Integer nextPositionIndex = position.getPositionIndex() + 1;
        RacePosition nextRacePosition = new RacePosition(nextPositionIndex, raceMarkOrder.get(nextPositionIndex), previousMark);

        return nextRacePosition;
    }

    /**
     * Loads the race order from an XML string
     * @param xml An AC35 RaceXML
     * @return An ordered list of marks in the race
     */
    private List<Mark> loadRaceOrderFromXML(String xml){

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;

        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.error("Failed to read generated race XML");
            return null;
        }
        
        RaceXMLData data = XMLParser.parseRace(doc);

        if (data != null){
            logger.debug("Loaded RaceXML for mark order");
            List<Corner> corners = data.getMarkSequence();
            Map<Integer, CompoundMark> marks = data.getCompoundMarks();
            List<Mark> course = new ArrayList<>();

            for (Corner corner : corners){
                CompoundMark compoundMark = marks.get(corner.getCompoundMarkID());
                course.add(compoundMark.getMarks().get(0));
            }

            return course;
        }

        return null;
    }

    /**
     * @return The first position in the race
     */
    public RacePosition getFirstPosition(){
        if (raceMarkOrder.size() > 0){
            return new RacePosition(-1, raceMarkOrder.get(0), null);
        }

        return null;
    }

    /**
     * Load the raceXML and mark order
     */
    private void loadRaceProperties(){
        XMLGenerator generator = new XMLGenerator();

        generator.setRace(new Race());

        String raceXML = generator.getRaceAsXml();

        if (raceXML == null){
            logger.error("Failed to generate raceXML (for race properties)");
            return;
        }
        raceMarkOrder = loadRaceOrderFromXML(raceXML);
    }
}
