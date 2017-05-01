package seng302.models.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Class to create an XML object from the XML Packet Messages.
 */
class XMLParser {

    /**
     * Creates a Regatta XML Object from the data in a Regatta XML Message
     * @param doc XML Document Object
     * @return A new RegattaXMLObject from the input Document.
     */
    RegattaXMLObject createRegattaXML(Document doc) {
        return new RegattaXMLObject(doc);
    }

    /**
     * Creates a Race XML Object from the data in a Regatta XML Message
     * @param doc XML Document Object
     * @return A new RaceXMLObject from the input Document.
     */
    RaceXMLObject createRaceXML(Document doc) {
        return new RaceXMLObject(doc);
    }

    /**
     * Creates a Boat XML Object from the data in a Regatta XML Message
     * @param doc XML Document Object
     * @return A new BoatXMLObject from the input Document.
     */
    BoatXMLObject createBoatXML(Document doc) {
        return new BoatXMLObject(doc);
    }

    /**
     * Returns the text content of a given child element tag, assuming it exists, as an Integer.
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
     * @param n A node object that should have some attributes
     * @param attr The attribute you want to get from the given node.
     * @return The String representation of the text content of an attribute in the given node, else returns null.
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
     * @param n A node object that should have some attributes
     * @param attr The attribute you want to get from the given node.
     * @return The Integer representation of the text content of an attribute in the given node, else returns null.
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
     * @param n A node object that should have some attributes
     * @param attr The attribute you want to get from the given node.
     * @return The Double representation of the text content of an attribute in the given node, else returns null.
     */
    private static Double getNodeAttributeDouble(Node n, String attr) {
        Node attrItem = n.getAttributes().getNamedItem(attr);
        if (attrItem != null) {
            return Double.parseDouble(attrItem.getTextContent());
        } else {
            return null;
        }
    }

    class RegattaXMLObject {
        //Regatta Info
        private Integer regattaID;
        private String regattaName;
        private String courseName;
        private Double centralLat;
        private Double centralLng;
        private Integer utcOffset;

        /**
         * Constructor for a RegattaXMLObject.
         * Takes the information from a Document object and creates a more usable format.
         * @param doc XML Document Object
         */
        RegattaXMLObject(Document doc) {
            Element docEle = doc.getDocumentElement();

            this.regattaID = getElementInt(docEle, "RegattaID");
            this.regattaName = getElementString(docEle, "RegattaName");
            this.courseName = getElementString(docEle, "CourseName");
            this.centralLat = getElementDouble(docEle, "CentralLatitude");
            this.centralLng = getElementDouble(docEle, "CentralLongitude");
            this.utcOffset = getElementInt(docEle, "UtcOffset");
        }

        public Integer getRegattaID() { return regattaID; }
        public String getRegattaName() { return regattaName; }
        public String getCourseName() { return courseName; }
        public Double getCentralLat() { return centralLat; }
        public Double getCentralLng() { return centralLng; }
        public Integer getUtcOffset() { return utcOffset; }

    }

    class RaceXMLObject {

        // Race Info
        private Integer raceID;
        private String raceType;
        private String creationTimeDate; // XML Creation Time

        //Race Start Details
        private String raceStartTime;
        private Boolean postponeStatus;

        //Non atomic race attributes
        private ArrayList<Participant> participants;
        private ArrayList<CompoundMark> course;
        private ArrayList<Corner> compoundMarkSequence;
        private ArrayList<Limit> courseLimit;

        /**
         * Constructor for a RaceXMLObject.
         * Takes the information from a Document object and creates a more usable format.
         * @param doc XML Document Object
         */
        RaceXMLObject(Document doc) {
            Element docEle = doc.getDocumentElement();

            //Atomic and Semi-Atomic Elements
            this.raceID = getElementInt(docEle, "RaceID");
            this.raceType = getElementString(docEle, "RaceType");
            this.creationTimeDate = getElementString(docEle, "CreationTimeDate");

            Node raceStart = docEle.getElementsByTagName("RaceStartTime").item(0);
            this.raceStartTime = getNodeAttributeString(raceStart, "Start") ;
            this.postponeStatus = Boolean.parseBoolean(getNodeAttributeString(raceStart, "Postpone"));

            //Participants
            participants = new ArrayList<>();

            NodeList pList = docEle.getElementsByTagName("Participants").item(0).getChildNodes();
            for (int i = 0; i < pList.getLength(); i++) {
                Node pNode = pList.item(i);
                String entry;
                if (pNode.getNodeName().equals("Yacht")) {
                    Integer sourceID = getNodeAttributeInt(pNode, "SourceID");

                    if (pNode.getAttributes().getLength() == 2) {
                        entry = getNodeAttributeString(pNode, "Entry");
                    } else {
                        entry = null;
                    }

                    Participant pa = new Participant(sourceID, entry);
                    participants.add(pa);
                }
            }

            //Course
            course = new ArrayList<>();

            NodeList cMarkList = docEle.getElementsByTagName("Course").item(0).getChildNodes();
            for (int i = 0; i < cMarkList.getLength(); i++) {
                Node cMarkNode = cMarkList.item(i);
                if (cMarkNode.getNodeName().equals("CompoundMark")) {
                    CompoundMark cMark = new CompoundMark(cMarkNode);
                    course.add(cMark);
                }
            }

            //Course Mark Sequence
            compoundMarkSequence = new ArrayList<>();

            NodeList cornerList = docEle.getElementsByTagName("CompoundMarkSequence").item(0).getChildNodes();
            for (int i = 0; i < cornerList.getLength(); i++) {
                Node cornerNode = cornerList.item(i);
                if (cornerNode.getNodeName().equals("Corner")) {
                    Corner corner = new Corner(cornerNode);
                    compoundMarkSequence.add(corner);
                }
            }

            //Course Limits
            courseLimit = new ArrayList<>();

            NodeList limitList = docEle.getElementsByTagName("CourseLimit").item(0).getChildNodes();
            for (int i = 0; i < limitList.getLength(); i++) {
                Node limitNode = limitList.item(i);
                if (limitNode.getNodeName().equals("Limit")) {
                    Limit limit = new Limit(limitNode);
                    courseLimit.add(limit);
                }
            }
        }

        public Integer getRaceID() { return raceID; }
        public String getRaceType() { return raceType; }
        public String getCreationTimeDate() { return creationTimeDate; }
        public String getRaceStartTime() { return raceStartTime; }
        public Boolean getPostponeStatus() { return postponeStatus; }

        public ArrayList<Participant> getParticipants() { return participants; }
        public ArrayList<CompoundMark> getCompoundMarks() { return course; }
        public ArrayList<Corner> getCompoundMarkSequence() { return compoundMarkSequence; }
        public ArrayList<Limit> getCourseLimit() { return courseLimit; }

        class Participant {
            Integer sourceID;
            String entry;

            Participant(Integer sourceID, String entry) {
                this.sourceID = sourceID;
                this.entry = entry;
            }

            public Integer getsourceID() { return sourceID; }
            public String getEntry() { return entry; }
        }

        class CompoundMark {
            private Integer markID;
            private String cMarkName;
            private ArrayList<Mark> marks;

            CompoundMark(Node compoundMark) {
                marks = new ArrayList<>();
                this.markID = getNodeAttributeInt(compoundMark, "CompoundMarkID");
                this.cMarkName = getNodeAttributeString(compoundMark, "Name");
                NodeList childMarks = compoundMark.getChildNodes();
                for (int i = 0; i < childMarks.getLength(); i++) {
                    Node markNode = childMarks.item(i);
                    if (markNode.getNodeName().equals("Mark")) {
                        Mark mark = new Mark(markNode);
                        marks.add(mark);
                    }
                }
            }

            public Integer getMarkID() { return markID; }
            public String getcMarkName() { return cMarkName; }
            public ArrayList<Mark> getMarks() { return marks; }

            class Mark {
                private Integer seqID;
                private Integer sourceID;
                private String markName;
                private Double targetLat;
                private Double targetLng;

                Mark(Node markNode) {

                    this.seqID = getNodeAttributeInt(markNode, "SeqID");
                    this.sourceID = getNodeAttributeInt(markNode, "SourceID");
                    this.markName = getNodeAttributeString(markNode, "Name");
                    this.targetLat = getNodeAttributeDouble(markNode, "TargetLat");
                    this.targetLng = getNodeAttributeDouble(markNode, "TargetLng");

                }

                public Integer getSeqID() { return seqID; }
                public Integer getSourceID() { return sourceID; }
                public String getMarkName() { return markName; }
                public Double getTargetLat() { return targetLat; }
                public Double getTargetLng() { return targetLng; }
            }
        }

        class Corner {
            private Integer seqID;
            private Integer compoundMarkID;
            private String rounding;
            private Integer zoneSize;

            Corner(Node cornerNode) {
                this.seqID = getNodeAttributeInt(cornerNode, "SeqID");
                this.compoundMarkID = getNodeAttributeInt(cornerNode, "CompoundMarkID");
                this.rounding = getNodeAttributeString(cornerNode, "Rounding");
                this.zoneSize = getNodeAttributeInt(cornerNode, "ZoneSize");
            }

            public Integer getSeqID() { return seqID; }
            public Integer getCompoundMarkID() { return compoundMarkID; }
            public String getRounding() { return rounding; }
            public Integer getZoneSize() { return zoneSize; }
        }

        class Limit {
            private Integer seqID;
            private Double lat;
            private Double lng;

            Limit(Node limitNode) {
                this.seqID = getNodeAttributeInt(limitNode, "SeqID");
                this.lat = getNodeAttributeDouble(limitNode, "Lat");
                this.lng = getNodeAttributeDouble(limitNode, "Lon");
            }

            public Integer getSeqID() { return seqID; }
            public Double getLat() { return lat; }
            public Double getLng() { return lng; }
        }

    }

    class BoatXMLObject {

        private String lastModified;
        private Integer version;

        //Settings for the boat type in the race. This may end up having to be reworked if multiple boat types compete.
        private String boatType;
        private Double boatLength;
        private Double hullLength;
        private Double markZoneSize;
        private Double courseZoneSize;
        private ArrayList<Double> zoneLimits;// will only contain 5 elements. Limits 1-5

        //Boats
        ArrayList<Boat> boats;

        /**
         * Constructor for a BoatXMLObject.
         * Takes the information from a Document object and creates a more usable format.
         * @param doc XML Document Object
         */
        BoatXMLObject(Document doc) {

            Element docEle = doc.getDocumentElement();

            this.lastModified = getElementString(docEle, "Modified");
            this.version = getElementInt(docEle, "Version");

            NodeList settingsList = docEle.getElementsByTagName("Settings").item(0).getChildNodes();
            this.boatType = getNodeAttributeString(settingsList.item(1), "Type");
            this.boatLength = getNodeAttributeDouble(settingsList.item(3), "BoatLength");
            this.hullLength = getNodeAttributeDouble(settingsList.item(3), "HullLength");
            this.markZoneSize = getNodeAttributeDouble(settingsList.item(5), "MarkZoneSize");
            this.courseZoneSize = getNodeAttributeDouble(settingsList.item(5), "CourseZoneSize");

            Node zoneLimitsList = settingsList.item(7);
            this.zoneLimits = new ArrayList<>();
            for (int i = 0; i < zoneLimitsList.getAttributes().getLength(); i++) {
                String tag = String.format("Limit%d", i+1);
                this.zoneLimits.add(getNodeAttributeDouble(zoneLimitsList, tag));
            }

            this.boats = new ArrayList<>();
            NodeList boatsList = docEle.getElementsByTagName("Boats").item(0).getChildNodes();
            for (int i = 0; i < boatsList.getLength(); i++) {
                Node currentBoat = boatsList.item(i);
                if (currentBoat.getNodeName().equals("Boat")) {
                    Boat boat = new Boat(currentBoat);
                    this.boats.add(boat);
                }
                //System.out.println(this.getBoats());
            }

        }

        public String getLastModified() { return lastModified; }
        public Integer getVersion() { return version; }
        public String getBoatType() { return boatType; }
        public Double getBoatLength() { return boatLength; }
        public Double getHullLength() { return hullLength; }
        public Double getMarkZoneSize() { return markZoneSize; }
        public Double getCourseZoneSize() { return courseZoneSize; }
        public ArrayList<Double> getZoneLimits() { return zoneLimits; }
        public ArrayList<Boat> getBoats() { return boats; }

        class Boat {

            private String boatType;
            private Integer sourceID;
            private String hullID; //matches HullNum in the XML spec.
            private String shortName;
            private String boatName;
            private String country;

            Boat(Node boatNode) {
                this.boatType = getNodeAttributeString(boatNode, "Type");
                this.sourceID = getNodeAttributeInt(boatNode, "SourceID");
                this.hullID = getNodeAttributeString(boatNode, "HullNum");
                this.shortName = getNodeAttributeString(boatNode, "ShortName");
                this.boatName = getNodeAttributeString(boatNode, "BoatName");
                this.country = getNodeAttributeString(boatNode, "Country");
            }

            public String getBoatType() { return boatType; }
            public Integer getSourceID() { return sourceID; }
            public String getHullID() { return hullID; }
            public String getShortName() { return shortName; }
            public String getBoatName() { return boatName; }
            public String getCountry() { return country; }

        }

    }

}