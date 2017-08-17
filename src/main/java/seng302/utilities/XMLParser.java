package seng302.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import seng302.model.ClientYacht;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;

/**
 * Utilities for parsing XML documents
 */
public class XMLParser {

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
            return Integer.parseInt(tagList.item(0).getTextContent());
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
            return Double.parseDouble(tagList.item(0).getTextContent());
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
                ClientYacht yacht = new ClientYacht(
                    XMLParser.getNodeAttributeString(currentBoat, "Type"),
                    XMLParser.getNodeAttributeInt(currentBoat, "SourceID"),
                    XMLParser.getNodeAttributeString(currentBoat, "HullNum"),
                    XMLParser.getNodeAttributeString(currentBoat, "ShortName"),
                    XMLParser.getNodeAttributeString(currentBoat, "BoatName"),
                    XMLParser.getNodeAttributeString(currentBoat, "Country"));
                yacht.setColour(Color.web(getNodeAttributeString(currentBoat, "Color")));
                if (yacht.getBoatType().equals("Yacht")) {
                    competingBoats.put(yacht.getSourceId(), yacht);
                }
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

    /**
     * Returns an object containing the data extracted from the given xml formatted document
     *
     * @param doc XML document
     * @return object containing race data
     */
    public static RaceXMLData parseRace(Document doc) {
        Element docEle = doc.getDocumentElement();
        return new RaceXMLData(
            extractParticpantIDs(docEle),
            extractCompoundMarks(docEle),
            extractMarkOrder(docEle),
            extractCourseLimit(docEle)
        );
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
                courseLimit.add(
                    new Limit(
                        XMLParser.getNodeAttributeInt(limitNode, "SeqID"),
                        XMLParser.getNodeAttributeDouble(limitNode, "Lat"),
                        XMLParser.getNodeAttributeDouble(limitNode, "Lon")
                    )
                );
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
    private static List<Integer> extractParticpantIDs (Element docEle) {
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
                cMark = new CompoundMark(
                    XMLParser.getNodeAttributeInt(cMarkNode, "CompoundMarkID"),
                    XMLParser.getNodeAttributeString(cMarkNode, "Name"),
                    createMarks(cMarkNode)
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
                Integer sourceID = XMLParser.getNodeAttributeInt(markNode, "SourceID");
                String markName = XMLParser.getNodeAttributeString(markNode, "Name");
                Double targetLat = XMLParser.getNodeAttributeDouble(markNode, "TargetLat");
                Double targetLng = XMLParser.getNodeAttributeDouble(markNode, "TargetLng");
                Mark mark = new Mark(markName, seqID, targetLat, targetLng, sourceID);
                subMarks.add(mark);
            }
        }
        return subMarks;
    }
}