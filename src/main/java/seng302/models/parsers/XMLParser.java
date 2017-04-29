package seng302.models.parsers;

import com.sun.org.apache.xpath.internal.SourceTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class XMLParser {

    RegattaXMLObject createRegattaXML(Document doc) {
        return new RegattaXMLObject(doc);
    }

    RaceXMLObject createRaceXML(Document doc) {
        return new RaceXMLObject(doc);
    }

    BoatXMLObject createBoatXML(Document doc) {
        return new BoatXMLObject(doc);
    }

    // TODO: 29/04/17 ajm412: Data Validation here to return null if a tag somehow doesn't actually exist.
    private static Integer getElementInt(Element ele, String tag) {
        return Integer.parseInt(ele.getElementsByTagName(tag).item(0).getTextContent());
    }

    private static String getElementString(Element ele, String tag) {
        return ele.getElementsByTagName(tag).item(0).getTextContent();
    }

    private static Double getElementDouble(Element ele, String tag) {
        return Double.parseDouble(ele.getElementsByTagName(tag).item(0).getTextContent());
    }

    private static String getNodeAttributeString(Node n, String attr) {
        return n.getAttributes().getNamedItem(attr).getTextContent();
    }

    private static Integer getNodeAttributeInt(Node n, String attr) {
        return Integer.parseInt(n.getAttributes().getNamedItem(attr).getTextContent());
    }

    private static Double getNodeAttributeDouble(Node n, String attr) {
        return Double.parseDouble(n.getAttributes().getNamedItem(attr).getTextContent());
    }

    class RegattaXMLObject {

        //Regatta Info
        private Integer regattaID;
        private String regattaName;
        private String courseName;
        private Double centralLat;
        private Double centralLng;
        private Integer utcOffset;

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
        Integer raceID;
        String raceType;
        String creationTimeDate; // XML Creation Time

        //Race Start Details
        String raceStartTime;
        Boolean postponeStatus;

        //Non atomic race attributes
        ArrayList<Participant> participants;
        ArrayList<CompoundMark> course;
        ArrayList<Corner> compoundMarkSequence;
        ArrayList<Limit> courseLimit;

        RaceXMLObject(Document doc) {
            Element docEle = doc.getDocumentElement();
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

            course = new ArrayList<>();

            NodeList cMarkList = docEle.getElementsByTagName("Course").item(0).getChildNodes();
            for (int i = 0; i < cMarkList.getLength(); i++) {
                Node cMarkNode = cMarkList.item(i);
                if (cMarkNode.getNodeName().equals("CompoundMark")) {
                    CompoundMark cMark = new CompoundMark(cMarkNode);
                    course.add(cMark);
                }
            }

            compoundMarkSequence = new ArrayList<>();

            NodeList cornerList = docEle.getElementsByTagName("CompoundMarkSequence").item(0).getChildNodes();
            for (int i = 0; i < cornerList.getLength(); i++) {
                Node cornerNode = cornerList.item(i);
                if (cornerNode.getNodeName().equals("Corner")) {
                    Corner corner = new Corner(cornerNode);
                    compoundMarkSequence.add(corner);
                }
            }

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
            private String skipper;

            Boat(Node boatNode) {
                // TODO: 29/04/17 Actually build the boats.
            }

            public String getBoatType() { return boatType; }
            public Integer getSourceID() { return sourceID; }
            public String getHullID() { return hullID; }
            public String getShortName() { return shortName; }
            public String getBoatName() { return boatName; }
            public String getCountry() { return country; }
            public String getSkipper() { return skipper; }

        }

    }

}