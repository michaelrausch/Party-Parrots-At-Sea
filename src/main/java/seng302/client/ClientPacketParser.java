package seng302.client;


import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.PriorityBlockingQueue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.models.Yacht;
import seng302.models.mark.Mark;
import seng302.models.stream.XMLParser;
import seng302.models.stream.packets.BoatPositionPacket;
import seng302.models.stream.packets.StreamPacket;

/**
 * The purpose of this class is to take in the stream of divided packets so they can be read
 * and parsed in by turning the byte arrays into useful data. There are two public static hashmaps
 * that are threadsafe so the visualiser can always access the latest speed and position available
 * Created by kre39 on 23/04/17.
 */
public class ClientPacketParser {

    public static ConcurrentHashMap<Long, PriorityBlockingQueue<BoatPositionPacket>> markLocations = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, PriorityBlockingQueue<BoatPositionPacket>> boatLocations = new ConcurrentHashMap<>();
    private String threadName;
    private Thread t;
    private static boolean newRaceXmlReceived = false;
    private static boolean raceStarted = false;
    private static XMLParser xmlObject;
    private static boolean raceFinished = false;
    private static boolean streamStatus = false;
    private static long timeSinceStart = -1;
    private static Map<Integer, Yacht> boats = new ConcurrentHashMap<>();
    private static Map<Integer, Yacht> boatsPos = new ConcurrentSkipListMap<>();
    private static double windDirection = 0;
    private static Double windSpeed = 0d;
     private static Long currentTimeLong;
    private static String currentTimeString;
    private static boolean appRunning;


     //CONVERSION CONSTANTS
    private static final Double MS_TO_KNOTS = 1.94384;

    /**
     * Used to initialise the thread name and stream parser object so a thread can be executed
     */
    public ClientPacketParser() {
    }
    /**
     * Looks at the type of the packet then sends it to the appropriate parser to extract the
     * specific data associated with that packet type
     *
     * @param packet the packet to be looked at and processed
     */
    public static void parsePacket(StreamPacket packet) {
        try {
            switch (packet.getType()) {
                case HEARTBEAT:
                    extractHeartBeat(packet);
                    break;
                case RACE_STATUS:
                    extractRaceStatus(packet);
                    break;
                case DISPLAY_TEXT_MESSAGE:
                    extractDisplayMessage(packet);
                    break;
                case XML_MESSAGE:
                    newRaceXmlReceived = true;
                    extractXmlMessage(packet);
                    break;
                case RACE_START_STATUS:
                    extractRaceStartStatus(packet);
                    break;
                case YACHT_EVENT_CODE:
                    extractYachtEventCode(packet);
                    break;
                case YACHT_ACTION_CODE:
                    extractYachtActionCode(packet);
                    break;
                case CHATTER_TEXT:
                    extractChatterText(packet);
                    break;
                case BOAT_LOCATION:
                    extractBoatLocation(packet);
                    break;
                case MARK_ROUNDING:
                    extractMarkRounding(packet);
                    break;
                case COURSE_WIND:
                    extractCourseWind(packet);
                    break;
                case AVG_WIND:
                    extractAvgWind(packet);
                    break;
            }
        } catch (NullPointerException e) {
            System.out.println("Error parsing packet");
            e.printStackTrace();
        }
    }

    /**
     * Extracts the seq num used in the heartbeat packet
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractHeartBeat(StreamPacket packet) {
        long heartbeat = bytesToLong(packet.getPayload());
        System.out.println("heartbeat = " + heartbeat);
    }

    private static String getTimeZoneString() {

        Integer offset = xmlObject.getRegattaXML().getUtcOffset();
        StringBuilder utcOffset = new StringBuilder();
        utcOffset.append("GMT");
        if (offset > 0) {
            utcOffset.append("+");
            utcOffset.append(offset);
        } else if (offset < 0) {
            utcOffset.append("-");
            utcOffset.append(offset);
        }
        return utcOffset.toString();

    }

    /**
     * Extracts the useful race status data from race status type packets. This method will also
     * print to the console the current state of the race (if it has started/finished or is about to
     * start), along side this it'll also display the amount of time since the race has started or
     * time till it starts
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractRaceStatus(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long currentTime = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 7, 11));
        int raceStatus = payload[11];
        long expectedStartTime = bytesToLong(Arrays.copyOfRange(payload,12,18));
        long windDir = bytesToLong(Arrays.copyOfRange(payload,18,20));
        long rawWindSpeed = bytesToLong(Arrays.copyOfRange(payload,20,22));

        currentTimeLong = currentTime;
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        if (xmlObject.getRegattaXML() != null) {
            format.setTimeZone(TimeZone.getTimeZone(getTimeZoneString()));
            currentTimeString = format.format((new Date(currentTime)).getTime());
        }
        long timeTillStart =
            ((new Date(expectedStartTime)).getTime() - (new Date(currentTime)).getTime()) / 1000;

        if (timeTillStart > 0) {
            timeSinceStart = timeTillStart;
        } else {
            if (raceStatus == 4 || raceStatus == 8) {
                raceFinished = true;
                raceStarted = false;
            } else if (!raceStarted) {
                raceStarted = true;
                raceFinished = false;
            }
            timeSinceStart = timeTillStart;
        }

        double windDirFactor = 0x4000 / 90;   //0x4000 is 90 degrees, 0x8000 is 180 degrees, etc...
        windDirection = windDir / windDirFactor;
        windSpeed = rawWindSpeed / 1000 * MS_TO_KNOTS;

        int noBoats = payload[22];
        int raceType = payload[23];
        for (int i = 0; i < noBoats; i++) {
            long boatStatusSourceID = bytesToLong(
                Arrays.copyOfRange(payload, 24 + (i * 20), 28 + (i * 20)));
            Yacht boat = boats.get((int) boatStatusSourceID);
            boat.setBoatStatus((int) payload[28 + (i * 20)]);
            setBoatLegPosition(boat, (int) payload[29 + (i * 20)]);
            boat.setPenaltiesAwarded((int) payload[30 + (i * 20)]);
            boat.setPenaltiesServed((int) payload[31 + (i * 20)]);
            Long estTimeAtNextMark = bytesToLong(
                Arrays.copyOfRange(payload, 32 + (i * 20), 38 + (i * 20)));
            boat.setEstimateTimeAtNextMark(estTimeAtNextMark);
            Long estTimeAtFinish = bytesToLong(
                Arrays.copyOfRange(payload, 38 + (i * 20), 44 + (i * 20)));
            boat.setEstimateTimeAtFinish(estTimeAtFinish);
//            boatsPos.put(estTimeAtFinish, boat);
//            String boatStatus = "SourceID: " + boatStatusSourceID;
//            boatStatus += "\nBoat Status: " + (int)payload[28 + (i * 20)];
//            boatStatus += "\nLegNumber: " + (int)payload[29 + (i * 20)];
//            boatStatus += "\nPenaltiesAwarded: " + (int)payload[29 + (i * 20)];
//            boatStatus += "\nPenaltiesServed: " + (int)payload[30 + (i * 20)];
//            boatStatus += "\nEstTimeAtNextMark: " + bytesToLong(Arrays.copyOfRange(payload,31 + (i * 20),37+ (i * 20)));
//            boatStatus += "\nEstTimeAtFinish: " + bytesToLong(Arrays.copyOfRange(payload,37 + (i * 20),43+ (i * 20)));
//            boatStatuses.add(boatStatus);
        }
//        if (isRaceStarted()) {
//            int pos = 1;
//            for (Yacht yacht : boatsPos.values()) {
//                yacht.setPosition(String.valueOf(pos));
//                pos++;
//            }
//        } else {
//            for (Yacht yacht : boatsPos.values()) {
//                yacht.setPosition("-");
//            }
//        }
    }

    private static void setBoatLegPosition(Yacht updatingBoat, Integer leg){
        Integer placing = 1;
        if (leg != updatingBoat.getLegNumber() && (raceStarted || raceFinished)) {
            for (Yacht boat : boats.values()) {
                if (boat.getLegNumber() != null && leg <= boat.getLegNumber()){
                    placing += 1;
                }
            }
            updatingBoat.setPosition(placing.toString());
            updatingBoat.setLegNumber(leg);
            boatsPos.putIfAbsent(placing, updatingBoat);
            boatsPos.replace(placing, updatingBoat);
        } else if(updatingBoat.getLegNumber() == null){
            updatingBoat.setPosition("1");
            updatingBoat.setLegNumber(leg);
        }
    }

    /**
     * Used to extract the messages passed through with the display message packet
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractDisplayMessage(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int numOfLines = payload[3];
        int totalLen = 0;
        for (int i = 0; i < numOfLines; i++) {
            int lineNum = payload[4 + totalLen];
            int textLength = payload[5 + totalLen];
            byte[] messageTextBytes = Arrays
                .copyOfRange(payload, 6 + totalLen, 6 + textLength + totalLen);
            String messageText = new String(messageTextBytes);
            totalLen += 2 + textLength;
        }
    }

    /**
     * Used to read in the xml data. Will call the specific methods to create the course and boats
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractXmlMessage(StreamPacket packet) {
        xmlObject = new XMLParser();
        byte[] payload = packet.getPayload();
        int messageType = payload[9];
        long messageLength = bytesToLong(Arrays.copyOfRange(payload, 12, 14));
        String xmlMessage = new String(
            (Arrays.copyOfRange(payload, 14, (int) (14 + messageLength)))).trim();
        System.out.println(xmlMessage);
        //Create XML document Object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(new StringReader(xmlMessage)));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        xmlObject.constructXML(doc, messageType);
        if (messageType == 7) {   //7 is the boat XML
            boats = xmlObject.getBoatXML().getCompetingBoats();
        }
        if (messageType == 6) { //6 is race info xml

            newRaceXmlReceived = true;
        }
    }

    /**
     * Extracts the race start status from the packet, currently is unused within the app but
     * is here for potential future use
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractRaceStartStatus(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long raceStartTime = bytesToLong(Arrays.copyOfRange(payload, 9, 15));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 15, 19));
        int notificationType = payload[19];
    }

    /**
     * When a yacht event occurs this will parse the byte array to retrieve the necessary info,
     * currently unused
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractYachtEventCode(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 9, 13));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload, 13, 17));
        long incidentId = bytesToLong(Arrays.copyOfRange(payload, 17, 21));
        int eventId = payload[21];
    }

    /**
     * When a yacht action occurs this will parse the parse the byte array to retrieve the necessary
     * info, currently unused
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractYachtActionCode(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload, 9, 13));
        long incidentId = bytesToLong(Arrays.copyOfRange(payload, 13, 17));
        int eventId = payload[17];
    }

    /**
     * Strips the message from the chatter text type packets, currently the message is unused
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractChatterText(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int messageType = payload[1];
        int length = payload[2];
        String message = new String(Arrays.copyOfRange(payload, 3, 3 + length));
        System.out.println(message);
    }

    /**
     * Used to breakdown the boatlocation packets so the boat coordinates, id and groundspeed are
     * all used All the other extra data is still being read and translated however is unused.
     *
     * @param packet Packet parsed in to use the payload
     */
    private static void extractBoatLocation(StreamPacket packet) {
        byte[] payload = packet.getPayload();

        int deviceType = (int) payload[15];
        long timeValid = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long seq = bytesToLong(Arrays.copyOfRange(payload, 11, 15));
        long boatId = bytesToLong(Arrays.copyOfRange(payload, 7, 11));
        long rawLat = bytesToLong(Arrays.copyOfRange(payload, 16, 20));
        long rawLon = bytesToLong(Arrays.copyOfRange(payload, 20, 24));
        //Converts the double to a usable lat/lon
        double lat = ((180d * (double) rawLat) / Math.pow(2, 31));
        double lon = ((180d * (double) rawLon) / Math.pow(2, 31));
        long heading = bytesToLong(Arrays.copyOfRange(payload, 28, 30));
        double groundSpeed = bytesToLong(Arrays.copyOfRange(payload, 38, 40)) / 1000.0;
        System.out.println("boats = " + boats.values());
        System.out.println("boats = " + boats.keySet());
        //type 1 is a racing yacht and type 3 is a mark, needed for updating positions of the mark and boat
        if (deviceType == 1){
            Yacht boat = boats.get((int) boatId);
//            boat.setVelocity(groundSpeed);
            BoatPositionPacket boatPacket = new BoatPositionPacket(boatId, timeValid, lat, lon, heading, groundSpeed);

            //add a new priority que to the boatLocations HashMap
            if (!boatLocations.containsKey(boatId)) {
                boatLocations.put(boatId,
                    new PriorityBlockingQueue<>(256, new Comparator<BoatPositionPacket>() {
                        @Override
                        public int compare(BoatPositionPacket p1, BoatPositionPacket p2) {
                            return (int) (p1.getTimeValid() - p2.getTimeValid());
                        }
                    }));
            }
            boatLocations.get(boatId).put(boatPacket);
        } else if (deviceType == 3) {
            BoatPositionPacket markPacket = new BoatPositionPacket(boatId, timeValid, lat, lon,
                heading, groundSpeed);

            //add a new priority que to the boatLocations HashMap
            if (!markLocations.containsKey(boatId)) {
                markLocations.put(boatId,
                    new PriorityBlockingQueue<>(256, new Comparator<BoatPositionPacket>() {
                        @Override
                        public int compare(BoatPositionPacket p1, BoatPositionPacket p2) {
                            return (int) (p1.getTimeValid() - p2.getTimeValid());
                        }
                    }));
            }
            markLocations.get(boatId).put(markPacket);
        }
    }

    /**
     * This packet type is received when a mark or gate is rounded by a boat
     *
     * @param packet The packet containing the payload
     */
    private static void extractMarkRounding(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 9, 13));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload, 13, 17));
        int boatStatus = payload[17];
        int roundingSide = payload[18];
        int markType = payload[19];
        int markId = payload[20];

        // assign mark rounding time to boat
        boats.get((int)subjectId).setMarkRoundingTime(timeStamp);

        for (Mark mark : xmlObject.getRaceXML().getAllCompoundMarks()) {
            if (mark.getCompoundMarkID() == markId) {
                boats.get((int)subjectId).setLastMarkRounded(mark);
            }
        }
    }

    /**
     * This packet type contains periodic data on the state of the wind
     *
     * @param packet The packet containing the payload
     */
    private static void extractCourseWind(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int selectedWindId = payload[1];
        int loopCount = payload[2];
        ArrayList<String> windInfo = new ArrayList<>();
        for (int i = 0; i < loopCount; i++) {
            String wind = "WindId: " + payload[3 + (20 * i)];
            wind +=
                "\nTime: " + bytesToLong(Arrays.copyOfRange(payload, 4 + (20 * i), 10 + (20 * i)));
            wind += "\nRaceId: " + bytesToLong(
                Arrays.copyOfRange(payload, 10 + (20 * i), 14 + (20 * i)));
            wind += "\nWindDirection: " + bytesToLong(
                Arrays.copyOfRange(payload, 14 + (20 * i), 16 + (20 * i)));
            wind += "\nWindSpeed: " + bytesToLong(
                Arrays.copyOfRange(payload, 16 + (20 * i), 18 + (20 * i)));
            wind += "\nBestUpWindAngle: " + bytesToLong(
                Arrays.copyOfRange(payload, 18 + (20 * i), 20 + (20 * i)));
            wind += "\nBestDownWindAngle: " + bytesToLong(
                Arrays.copyOfRange(payload, 20 + (20 * i), 22 + (20 * i)));
            wind += "\nFlags: " + String
                .format("%8s", Integer.toBinaryString(payload[22 + (20 * i)] & 0xFF))
                .replace(' ', '0');
            windInfo.add(wind);
        }
    }

    /**
     * This packet conatins the average wind to ground speed
     *
     * @param packet The packet containing the paylaod
     */
    private static void extractAvgWind(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long rawPeriod = bytesToLong(Arrays.copyOfRange(payload, 7, 9));
        long rawSamplePeriod = bytesToLong(Arrays.copyOfRange(payload, 9, 11));
        long period2 = bytesToLong(Arrays.copyOfRange(payload, 11, 13));
        long speed2 = bytesToLong(Arrays.copyOfRange(payload, 13, 15));
        long period3 = bytesToLong(Arrays.copyOfRange(payload, 15, 17));
        long speed3 = bytesToLong(Arrays.copyOfRange(payload, 17, 19));
        long period4 = bytesToLong(Arrays.copyOfRange(payload, 19, 21));
        long speed4 = bytesToLong(Arrays.copyOfRange(payload, 21, 23));
    }

    /**
     * takes an array of up to 7 bytes and returns a positive
     * long constructed from the input bytes
     *
     * @return a positive long if there is less than 7 bytes -1 otherwise
     */
    private static long bytesToLong(byte[] bytes) {
        long partialLong = 0;
        int index = 0;
        for (byte b : bytes) {
            if (index > 6) {
                return -1;
            }
            partialLong = partialLong | (b & 0xFFL) << (index * 8);
            index++;
        }
        return partialLong;
    }

    /**
     * returns false if race not started, true otherwise
     *
     * @return race started status
     */
    public static boolean isRaceStarted() {
        return raceStarted;
    }

    /**
     * returns false if stream not connected, true otherwise
     *
     * @return stream started status
     */
    public static boolean isStreamStatus() {
        return streamStatus;
    }

    /**
     * returns race timer
     *
     * @return race timer in long
     */
    public static long getTimeSinceStart() {
        return timeSinceStart;
    }

    /**
     * return false if race not finished, true otherwise
     *
     * @return race finished status
     */
    public static boolean isRaceFinished() {
        return raceFinished;
    }

    /**
     * return a map of boats with sourceID and the boat
     *
     * @return map of boats
     */
    public static Map<Integer, Yacht> getBoats() {
        return boats;
    }


    /**
     * returns the latest updated object from xml parser
     *
     * @return the latest xml object
     */
    public static XMLParser getXmlObject() {
        return xmlObject;
    }

    /**
     * returns the wind direction in degrees
     *
     * @return a double wind direction value
     */
    public static double getWindDirection() {
        return windDirection;
    }


    /**
     * Returns the wind speed in knots
     * @return A double indicating the wind speed in knots
     */
    public static Double getWindSpeed() {
        return windSpeed;
    }

    /**
     * returns stream time in formatted string format
     *
     * @return String of stream time
     */
    public static String getCurrentTimeString() {
        return currentTimeString;
    }

    /**
     * used in boat position since tree map can sort position efficiently.
     *
     * @return a map of time to finish and boat.
     */
    public static Map<Integer, Yacht> getBoatsPos() {

        return boatsPos;
    }

    /**
     * returns current time in stream in long
     *
     * @return a long value of current time
     */
    public static Long getCurrentTimeLong() {
        return currentTimeLong;
    }

    public static void appClose() {
        appRunning = false;
    }

    /**
     * Used to check if a new un-processed xml has been found, if so will return true before
     * toggling off so that the next check will return false.
     *
     * @return the status of if new xml has been received
     */
    public static boolean isNewRaceXmlReceived() {
        if (newRaceXmlReceived) {
            newRaceXmlReceived = false;
            return true;
        } else {
            return false;
        }
    }
}

