package seng302.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import seng302.model.ClientYacht;
import seng302.model.Colors;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.model.stream.xml.generator.RaceXMLTemplate;
import seng302.model.stream.xml.generator.RegattaXMLTemplate;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.model.token.Token;
import seng302.model.token.TokenType;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;

/**
 * Utilities for parsing XML documents
 */
public class XMLParser {

    private static final int MAX_PLAYERS = 8;

    /**
     * Returns the text content of a given child element tag, assuming it exists, as an Integer.
     *
     * @param ele Document Element with child elements.
     * @param tag Tag to find in document elements child elements.
     * @return Text content from tag if found, null otherwise.
     */
    private static Integer getElementInt(Element ele, String tag) {
        NodeList tagList = ele.getElementsByTagName(tag);
        if (tagList.getLength() > 0) {
            return Integer.parseInt(tagList.item(0).getTextContent().replaceAll("\\s+",""));
        } else {
            return null;
        }
    }

    /**
     * Returns the text content of a given child element tag, assuming it exists, as an String.
     *
     * @param ele Document Element with child elements.
     * @param tag Tag to find in document elements child elements.
     * @return Text content from tag if found, null otherwise.
     */
    private static String getElementString(Element ele, String tag) {
        NodeList tagList = ele.getElementsByTagName(tag);
        if (tagList.getLength() > 0) {
            return tagList.item(0).getTextContent();
        } else {
            return null;
        }
    }

    /**
     * Returns the text content of a given child element tag, assuming it exists, as a Double.
     *
     * @param ele Document Element with child elements.
     * @param tag Tag to find in document elements child elements.
     * @return Text content from tag if found, null otherwise.
     */
    private static Double getElementDouble(Element ele, String tag) {
        NodeList tagList = ele.getElementsByTagName(tag);
        if (tagList.getLength() > 0) {
            return Double.parseDouble(tagList.item(0).getTextContent().replaceAll("\\s+",""));
        } else {
            return null;
        }
    }

    /**
     * Returns the text content of an attribute of a given Node, assuming it exists, as a String.
     *
     * @param n A node object that should have some attributes
     * @param attr The attribute you want to get from the given node.
     * @return The String representation of the text content of an attribute in the given node, else
     * returns null.
     */
    private static String getNodeAttributeString(Node n, String attr) {
        Node attrItem = n.getAttributes().getNamedItem(attr);
        if (attrItem != null) {
            return attrItem.getTextContent();
        } else {
            return null;
        }
    }

    /**
     * Returns the text content of an attribute of a given Node, assuming it exists, as an Integer.
     *
     * @param n A node object that should have some attributes
     * @param attr The attribute you want to get from the given node.
     * @return The Integer representation of the text content of an attribute in the given node,
     * else returns null.
     */
    private static Integer getNodeAttributeInt(Node n, String attr) {
        Node attrItem = n.getAttributes().getNamedItem(attr);
        if (attrItem != null) {
            return Integer.parseInt(attrItem.getTextContent());
        } else {
            return null;
        }
    }

    /**
     * Returns the text content of an attribute of a given Node, assuming it exists, as a Double.
     *
     * @param n A node object that should have some attributes
     * @param attr The attribute you want to get from the given node.
     * @return The Double representation of the text content of an attribute in the given node, else
     * returns null.
     */
    private static Double getNodeAttributeDouble(Node n, String attr) {
        Node attrItem = n.getAttributes().getNamedItem(attr);
        if (attrItem != null) {
            return Double.parseDouble(attrItem.getTextContent());
        } else {
            return null;
        }
    }

    /**
     * Produces a mapping of boat sourceIDS to boat objects created from the given xml document.
     * @param doc XML Document Object
     * @return Mapping of sourceIds to Boats.
     */
    public static Map<Integer, ClientYacht> parseBoats(Document doc) {
        Map<Integer, ClientYacht> competingBoats = new HashMap<>();

        Element docEle = doc.getDocumentElement();

        NodeList boatsList = docEle.getElementsByTagName("Boats").item(0).getChildNodes();
        for (int i = 0; i < boatsList.getLength(); i++) {
            Node currentBoat = boatsList.item(i);
            if (currentBoat.getNodeName().equals("Boat")) {
//                    Boat boat = new Boat(currentBoat);
                BoatMeshType boatMeshType;
                try {
                    boatMeshType = BoatMeshType.valueOf(XMLParser.getNodeAttributeString(currentBoat, "Type"));
                } catch (IllegalArgumentException e){
                    boatMeshType = BoatMeshType.DINGHY;
                }
                Color color;
                try {
                    color = Color.web(getNodeAttributeString(currentBoat, "Color"));
                } catch (NullPointerException npe) {
                    color = Colors.getColor(new Random().nextInt(8));
                }
                ClientYacht yacht = new ClientYacht(
                    boatMeshType,
                    XMLParser.getNodeAttributeInt(currentBoat, "SourceID"),
                    XMLParser.getNodeAttributeString(currentBoat, "HullNum"),
                    XMLParser.getNodeAttributeString(currentBoat, "ShortName"),
                    XMLParser.getNodeAttributeString(currentBoat, "BoatName"),
                    XMLParser.getNodeAttributeString(currentBoat, "Country"));
                yacht.setColour(color);
                competingBoats.put(yacht.getSourceId(), yacht);
            }
        }
        return competingBoats;
    }

    /**
     * Returns an object containing the data extracted from the given xml formatted document
     *
     * @param doc XML Document Object
     * @return Object containing regatta data
     */
    public static RegattaXMLData parseRegatta(Document doc) {

        Element docEle = doc.getDocumentElement();
        Integer regattaID = XMLParser.getElementInt(docEle, "RegattaID");
        String regattaName = XMLParser.getElementString(docEle, "RegattaName");
        String courseName = XMLParser.getElementString(docEle, "CourseName");
        Double centralLat = XMLParser.getElementDouble(docEle, "CentralLatitude");
        Double centralLng = XMLParser.getElementDouble(docEle, "CentralLongitude");
        Integer utcOffset = XMLParser.getElementInt(docEle, "UtcOffset");
        return new RegattaXMLData(
            regattaID, regattaName, courseName, centralLat, centralLng, utcOffset
        );
    }

    public static Boolean tokensEnabled(Document doc) {
        Element docEle = doc.getDocumentElement();
        try {
            NamedNodeMap namedNodeMap = docEle.getElementsByTagName("Tokens").item(0).getAttributes();
            Node node = namedNodeMap.getNamedItem("Enabled");
            if (node != null) {
                return Boolean.parseBoolean(node.getNodeValue());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return false;
        }
        return false;
    }

    public static Integer getMaxPlayers(Document doc) {
        Element docEle = doc.getDocumentElement();
        try {
            NamedNodeMap namedNodeMap = docEle.getElementsByTagName("Participants").item(0).getAttributes();
            Node node = namedNodeMap.getNamedItem("MaxPlayers");
            if (node != null) {
                return Integer.parseInt(node.getNodeValue());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return MAX_PLAYERS;
        }
        return MAX_PLAYERS;
    }

    /**
     * Returns an object containing the data extracted from the given xml formatted document
     *
     * @param doc XML document
     * @return object containing race data
     */
    public static RaceXMLData parseRace(Document doc) {
        Element docEle = doc.getDocumentElement();
        return new RaceXMLData(
            extractParticipantIDs(docEle),
            extractTokens(docEle),
            extractCompoundMarks(docEle),
            extractMarkOrder(docEle),
            extractCourseLimit(docEle)
        );
    }

    /**
     * Extracts token data
     */
    private static List<Token> extractTokens(Element docEle) {
        List<Token> tokens = new ArrayList<>();
        try {
            NodeList tokenList = docEle.getElementsByTagName("Tokens").item(0).getChildNodes();
            for (int i = 0; i < tokenList.getLength(); i++) {
                Node tokenNode = tokenList.item(i);
                if (tokenNode.getNodeName().equals("Token")) {
                    String tokenType = getNodeAttributeString(tokenNode, "TokenType");
                    Double lat = getNodeAttributeDouble(tokenNode, "TargetLat");
                    Double lng = getNodeAttributeDouble(tokenNode, "TargetLng");
                    tokens.add(new Token(TokenType.valueOf(tokenType), lat, lng));
                }
            }
        } catch (NullPointerException npe) {
            return new ArrayList<>();
        }
        return tokens;
    }

    /**
     * Extracts course limit data
     */
    private static List<Limit> extractCourseLimit(Element docEle) {
        List<Limit> courseLimit = new ArrayList<>();
        NodeList limitList = docEle.getElementsByTagName("CourseLimit").item(0).getChildNodes();
        for (int i = 0; i < limitList.getLength(); i++) {
            Node limitNode = limitList.item(i);
            if (limitNode.getNodeName().equals("Limit")) {
                courseLimit.add(new Limit(
                    XMLParser.getNodeAttributeInt(limitNode, "SeqID"),
                    XMLParser.getNodeAttributeDouble(limitNode, "Lat"),
                    XMLParser.getNodeAttributeDouble(limitNode, "Lon")
                ));
            }
        }
        return courseLimit;
    }

    /**
     * Extracts course order data
     */
    private static List<Corner> extractMarkOrder (Element docEle) {
        List<Corner> compoundMarkSequence = new ArrayList<>();
        NodeList cornerList = docEle.getElementsByTagName("CompoundMarkSequence").item(0)
            .getChildNodes();
        for (int i = 0; i < cornerList.getLength(); i++) {
            Node cornerNode = cornerList.item(i);
            if (cornerNode.getNodeName().equals("Corner")) {
                compoundMarkSequence.add(
                    new Corner(
                        XMLParser.getNodeAttributeInt(cornerNode, "SeqID"),
                        XMLParser.getNodeAttributeInt(cornerNode, "CompoundMarkID"),
                        XMLParser.getNodeAttributeString(cornerNode, "Rounding"),
                        XMLParser.getNodeAttributeInt(cornerNode, "ZoneSize")
                    )
                );
            }
        }
        return compoundMarkSequence;
    }

    /**
     * Extracts course participants data
     */
    private static List<Integer> extractParticipantIDs(Element docEle) {
        List<Integer> boatIDs = new ArrayList<>();
        NodeList pList = docEle.getElementsByTagName("Participants").item(0).getChildNodes();
        for (int i = 0; i < pList.getLength(); i++) {
            Node pNode = pList.item(i);
            if (pNode.getNodeName().equals("Yacht")) {
                boatIDs.add(XMLParser.getNodeAttributeInt(pNode, "SourceID"));
            }
        }
        return boatIDs;
    }

    /**
     * Extracts course mark data
     */
    private static List<CompoundMark> extractCompoundMarks(Element docEle) {
        List<CompoundMark> allMarks = new ArrayList<>();
        NodeList cMarkList = docEle.getElementsByTagName("Course").item(0).getChildNodes();
        CompoundMark cMark;
        for (int i = 0; i < cMarkList.getLength(); i++) {
            Node cMarkNode = cMarkList.item(i);
            if (cMarkNode.getNodeName().equals("CompoundMark")) {
                String name = XMLParser.getNodeAttributeString(cMarkNode, "Name");
                name = (name == null || name.equals("")) ? "Mark " + i+1: name;
                cMark = new CompoundMark(
                    XMLParser.getNodeAttributeInt(cMarkNode, "CompoundMarkID"),
                    name, createMarks(cMarkNode)
                );
                allMarks.add(cMark);
            }
        }
        return allMarks;
    }

    /**
     * Creates marks objects from the given node
     */
    private static List<Mark> createMarks(Node compoundMark) {
        List<Mark> subMarks = new ArrayList<>();
        Integer compoundMarkID = XMLParser.getNodeAttributeInt(compoundMark, "CompoundMarkID");
        String cMarkName = XMLParser.getNodeAttributeString(compoundMark, "Name");

        NodeList childMarks = compoundMark.getChildNodes();
        for (int i = 0; i < childMarks.getLength(); i++) {
            Node markNode = childMarks.item(i);
            if (markNode.getNodeName().equals("Mark")) {
                Integer seqID = XMLParser.getNodeAttributeInt(markNode, "SeqID");
                seqID = (seqID == null) ? i+1 : seqID;

                Integer sourceID = XMLParser.getNodeAttributeInt(markNode, "SourceID");
                sourceID = (sourceID == null) ? i+1 : sourceID;

                String markName = XMLParser.getNodeAttributeString(markNode, "Name");
                markName = (markName == null || markName.equals("")) ? cMarkName + " " + i+1: markName;

                Double targetLat = XMLParser.getNodeAttributeDouble(markNode, "TargetLat");
                Double targetLng = XMLParser.getNodeAttributeDouble(markNode, "TargetLng");

                Mark mark = new Mark(markName, seqID, targetLat, targetLng, sourceID);
                subMarks.add(mark);
            }
        }
        return subMarks;
    }

    /**
     * This ungodly combination of existing methods and code blocks parses a race definition file.
     * Look upon it and despair.
     * @param url The input file path
     * @param serverName the name of the server
     * @param repetitions the repetitions of a segment of the race def file.
     * @param maxPlayers max number of players. uses the default race max if null or greater than the actual max.
     * @param tokensEnabled if tokens are enabled
     * @return a pair which contains regatta string, race string as key, value pair.
     */
    public static Pair<RegattaXMLTemplate, RaceXMLTemplate> parseRaceDef(
            String url, String serverName, Integer repetitions, Integer maxPlayers, Boolean tokensEnabled
    ) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(XMLParser.class.getResourceAsStream(url));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        Element docEle = doc.getDocumentElement();

        RegattaXMLTemplate regattaXMLTemplate = new RegattaXMLTemplate(
            serverName, XMLParser.getElementString(docEle, "CourseName"),
            XMLParser.getElementDouble(docEle, "CentralLat"),
            XMLParser.getElementDouble(docEle, "CentralLng")
        );

        XMLGenerator xmlGenerator = new XMLGenerator();
        xmlGenerator.setRegattaTemplate(regattaXMLTemplate);

        if (maxPlayers == null) {
            maxPlayers = XMLParser.getElementInt(docEle, "MaxPlayers");
        } else if (maxPlayers > XMLParser.getElementInt(docEle, "MaxPlayers")) {
            maxPlayers = XMLParser.getElementInt(docEle, "MaxPlayers");
        }

        RaceXMLTemplate raceXMLTemplate = new RaceXMLTemplate(
            new ArrayList<>(), new ArrayList<>(),
            XMLParser.extractMarkOrderRaceDef(docEle, repetitions),
            XMLParser.extractCourseLimitRaceDef(docEle),
            XMLParser.extractCompoundMarksRaceDef(docEle),
            maxPlayers, tokensEnabled
        );
        xmlGenerator.setRaceTemplate(raceXMLTemplate);
        return new Pair<>(regattaXMLTemplate, raceXMLTemplate);
    }

    private static List<Corner> extractMarkOrderRaceDef(Element docEle, int repitions){
        List<Corner> compoundMarkSequence = new ArrayList<>();
        NodeList cornerList = docEle.getElementsByTagName("Course").item(0).getChildNodes();

        int seqId = 1;
        final int zoneSize = 3;

        for (int i=0; i<cornerList.getLength(); i++) {
            Node segment = cornerList.item(i);
            if (segment.getNodeName().equals("OpeningSegment") ||
                segment.getNodeName().equals("ClosingSegment")) {

                seqId = parseCourseSegment(segment, seqId, compoundMarkSequence);

            } else if (segment.getNodeName().equals("RepeatingSegment")) {
                for (int k = 0; k < repitions; k++) {
                    seqId = parseCourseSegment(segment, seqId, compoundMarkSequence);
                }
            }
        }
        return compoundMarkSequence;
    }

    /**
     * Parses a segment of the course adding new Corners to the given list.
     * @param segment Segment to parse
     * @param seqID initial sequence ID
     * @param course course to add corners to
     * @return the last sequence id.
     */
    private static int parseCourseSegment(Node segment, int seqID, List<Corner> course) {
        NodeList segmentList = segment.getChildNodes();
        for (int j = 0; j < segmentList.getLength(); j++) {
            Node corner = segmentList.item(j);
            if (corner.getNodeName().equals("Corner")) {
                String rounding = XMLParser.getNodeAttributeString(corner, "Rounding");
                rounding = //Converting "P" to "Port" and "S" to "Stbd"
                    rounding.equals("P") ? "Port" :
                    rounding.equals("S") ? "Stbd" : rounding;
                course.add(new Corner(
                    seqID++, XMLParser.getNodeAttributeInt(corner, "CompoundMarkID"),
                    rounding, 3
                ));
            }
        }
        return seqID;
    }

    private static List<Limit> extractCourseLimitRaceDef(Element docEle) {
        List<Limit> courseLimit = new ArrayList<>();
        NodeList limitList = docEle.getElementsByTagName("CourseLimit").item(0).getChildNodes();
        int seqId = 1;
        for (int i = 0; i < limitList.getLength(); i++) {
            Node limitNode = limitList.item(i);
            if (limitNode.getNodeName().equals("Limit")) {
                courseLimit.add(new Limit(
                    seqId++, XMLParser.getNodeAttributeDouble(limitNode, "Lat"),
                    XMLParser.getNodeAttributeDouble(limitNode, "Lng")
                ));
            }
        }
        return courseLimit;
    }

    private static List<CompoundMark> extractCompoundMarksRaceDef(Element docEle){
        List<CompoundMark> allMarks = new ArrayList<>();
        NodeList cMarkList = docEle.getElementsByTagName("Marks").item(0).getChildNodes();
        CompoundMark cMark;
        int markCount = 200;
        for (int i = 0; i < cMarkList.getLength(); i++) {
            Node cMarkNode = cMarkList.item(i);
            if (cMarkNode.getNodeName().equals("CompoundMark")) {
                Integer id = XMLParser.getNodeAttributeInt(cMarkNode, "CompoundMarkID");
                List<Mark> subMarks = createMarksRaceDef(cMarkNode, markCount,"Mark " + id);
                markCount += subMarks.size();
                allMarks.add(new CompoundMark(id, "Mark " + id, subMarks));
            }
        }
        return allMarks;
    }

    private static List<Mark> createMarksRaceDef(Node compoundMark, int markCount, String markName) {
        List<Mark> subMarks = new ArrayList<>();
        NodeList childMarks = compoundMark.getChildNodes();
        int seqID = 1;
        for (int i = 0; i < childMarks.getLength(); i++) {
            Node markNode = childMarks.item(i);
            if (markNode.getNodeName().equals("Mark")) {
                Double targetLat = XMLParser.getNodeAttributeDouble(markNode, "Lat");
                Double targetLng = XMLParser.getNodeAttributeDouble(markNode, "Lng");
                Mark mark = new Mark(markName + " subMark " + seqID, seqID, targetLat, targetLng, markCount++);
                subMarks.add(mark);
                seqID += 1;
            }
        }
        return subMarks;
    }
}