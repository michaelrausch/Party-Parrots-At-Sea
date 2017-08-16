package seng302.model.mark;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.stream.xml.generator.Race;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;
import java.util.*;

/**
 * Class to hold the order of the marks in the race.
 */
public class MarkOrder {
    private List<CompoundMark> raceMarkOrder;
    private Logger logger = LoggerFactory.getLogger(MarkOrder.class);
    private Set<Mark> allMarks;

    public MarkOrder(){
        loadRaceProperties();
    }

    /**
     * @return An ordered list of marks in the race
     *         OR null if the mark order could not be loaded
     */
    public List<CompoundMark> getMarkOrder() {
        if (raceMarkOrder == null){
            logger.warn("Race order accessed but not instantiated");
            return null;
        }

        return Collections.unmodifiableList(raceMarkOrder);
    }

    /**
     * @param seqID The seqID of the current mark the boat is heading to
     * @return A Boolean indicating if this coming mark is the last one (finish line)
     */
    public Boolean isLastMark(Integer seqID) {
        return seqID == raceMarkOrder.size() - 1;
    }

    /**
     * @param currentSeqID The seqID of the current mark the boat is heading to
     * @return The mark last passed
     * @throws IndexOutOfBoundsException if there is no next mark. Check seqID != 0 first
     */
    public CompoundMark getPreviousMark(Integer currentSeqID) throws IndexOutOfBoundsException {
        return raceMarkOrder.get(currentSeqID - 1);
    }

    public CompoundMark getCurrentMark(Integer currentSeqID) {
        return raceMarkOrder.get(currentSeqID);
    }

    /**
     * @param currentSeqID The seqID of the current mark the boat is heading to
     * @return The mark following the mark that the boat is heading to
     * @throws IndexOutOfBoundsException if there is no next mark. Check using {@link
     * #isLastMark(Integer)}
     */
    public CompoundMark getNextMark(Integer currentSeqID) throws IndexOutOfBoundsException {
        return raceMarkOrder.get(currentSeqID + 1);
    }

    public Set<Mark> getAllMarks(){
        return Collections.unmodifiableSet(allMarks);
    }

    /**
     * Loads the race order from an XML string
     * @param xml An AC35 RaceXML
     * @return An ordered list of marks in the race
     */
    private List<CompoundMark> loadRaceOrderFromXML(String xml) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;
        allMarks = new HashSet<>();

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
            List<CompoundMark> course = new ArrayList<>();
            for (Corner corner : corners){
                CompoundMark compoundMark = marks.get(corner.getCompoundMarkID());
                compoundMark.setRoundingSide(
                    RoundingSide.getRoundingSide(corner.getRounding())
                );
                course.add(compoundMark);
                allMarks.addAll(compoundMark.getMarks());
            }

            return course;
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