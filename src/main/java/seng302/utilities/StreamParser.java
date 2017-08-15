package seng302.utilities;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.model.stream.packets.PacketType;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.parser.*;
import seng302.model.stream.parser.PositionUpdateData.DeviceType;

/**
 * StreamParser is a utilities class for taking byte data, formatted according to the AC35 streaming
 * protocol, and parsing it into basic data types or collections.
 *
 * Created by kre39 on 23/04/17.
 */
public class StreamParser {

    /**
     * Extracts and returns the seq num used in the heartbeat packet.
     *
     * @param packet Packet parsed in to use the payload
     * @return the packet sequence number if the packet is of type HEARTBEAT, null otherwise.
     */
    public static Long extractHeartBeat(StreamPacket packet) {
        if (packet.getType() != PacketType.HEARTBEAT) {
            return null;
        }
        long heartbeat = bytesToLong(packet.getPayload());
        System.out.println("heartbeat = " + heartbeat);
        return heartbeat;
    }

    /**
     * Extracts the useful race status data from race status type packets. This method will also
     * print to the console the current state of the race (if it has started/finished or is about to
     * start), along side this it'll also display the amount of time since the race has started or
     * time till it starts
     *
     * @param packet Packet parsed in to use the payload
     * @return null if the packet type is not RACE_STATUS, otherwise an instance of RaceStatusData
     * containing the parsed packet data.
     */
    public static RaceStatusData extractRaceStatus(StreamPacket packet) {
        if (packet.getType() != PacketType.RACE_STATUS) {
            return null;
        }
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long currentTime = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 7, 11));
        int raceStatus = payload[11];
        long expectedStartTime = bytesToLong(Arrays.copyOfRange(payload, 12, 18));
        long windDir = bytesToLong(Arrays.copyOfRange(payload, 18, 20));
        long rawWindSpeed = bytesToLong(Arrays.copyOfRange(payload, 20, 22));

//        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//        currentTime = format.format((new Date(currentTime)))

        RaceStatusData data = new RaceStatusData(
            windDir, rawWindSpeed, raceStatus, currentTime, expectedStartTime
        );

//        long timeTillStart =
//            ((new Date(expectedStartTime)).getTime() - (new Date(currentTime)).getTime()) / 1000;
//
//        if (timeTillStart > 0) {
//            timeSinceStart = timeTillStart;
//        } else {
//            if (raceStatus == 4 || raceStatus == 8) {
//                raceFinished = true;
//                raceStarted = false;
//            } else if (!raceStarted) {
//                raceStarted = true;
//                raceFinished = false;
//            }
//            timeSinceStart = timeTillStart;
//        }
//

//
        int noBoats = payload[22];
        int raceType = payload[23];
        long boatID, estTimeAtNextMark, estTimeAtFinish;
        int leg, boatStatus;
        for (int i = 0; i < noBoats; i++) {
            boatID = bytesToLong(
                Arrays.copyOfRange(payload, 24 + (i * 20), 28 + (i * 20)));
            boatStatus = (int) payload[28 + (i * 20)];

//            setBoatLegPosition(boat, (int) payload[29 + (i * 20)]);
//            boat.setPenaltiesAwarded((int) payload[30 + (i * 20)]);
//            boat.setPenaltiesServed((int) payload[31 + (i * 20)]);
            estTimeAtNextMark = bytesToLong(
                Arrays.copyOfRange(payload, 32 + (i * 20), 38 + (i * 20)));
//            boat.setEstimateTimeTillNextMark(estTimeAtNextMark);
            estTimeAtFinish = bytesToLong(
                Arrays.copyOfRange(payload, 38 + (i * 20), 44 + (i * 20)));
            leg = (int) payload[29 + (i * 20)];
//            boat.setEstimateTimeAtFinish(estTimeAtFinish);
            data.addBoatData(boatID, estTimeAtNextMark, estTimeAtFinish, leg, boatStatus);
        }
        return data;
    }

//    private static void setBoatLegPosition(Yacht updatingBoat, Integer leg){
//        Integer placing = 1;
//        if (leg != updatingBoat.getLegNumber() && (raceStarted || raceFinished)) {
//            for (Yacht boat : boats.values()) {
//                if (boat.getLegNumber() != null && leg <= boat.getLegNumber()){
//                    placing += 1;
//                }
//            }
//            updatingBoat.setPlacing(placing.toString());
//            updatingBoat.setLegNumber(leg);
//            boatsPos.putIfAbsent(placing, updatingBoat);
//            boatsPos.replace(placing, updatingBoat);
//        } else if(updatingBoat.getLegNumber() == null){
//            updatingBoat.setPlacing("1");
//            updatingBoat.setLegNumber(leg);
//        }
//    }

    /**
     * Parses and returns the text from a StreamPacket containing text data for display.
     *
     * @param packet Packet parsed in to use the payload
     * @return A list containing all display message text. Is null if the packet is not of type
     * DISPLAY_TEXT_MESSAGE.
     */
    public static List<String> extractDisplayMessage(StreamPacket packet) {
        if (packet.getType() != PacketType.DISPLAY_TEXT_MESSAGE) {
            return null;
        }
        List<String> message = new ArrayList<>();
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int numOfLines = payload[3];
        int totalLen = 0;
        for (int i = 0; i < numOfLines; i++) {
            int lineNum = payload[4 + totalLen];
            int textLength = payload[5 + totalLen];
            byte[] messageTextBytes = Arrays
                .copyOfRange(payload, 6 + totalLen, 6 + textLength + totalLen);
            message.add(new String(messageTextBytes));
            totalLen += 2 + textLength;
        }
        return message;
    }

    /**
     * Parses and returns an XMLParser containing XML data sent in the given StreamPacket. XML data
     * can be for races, boats or the regatta.
     *
     * @param packet Packet parsed in to use the payload
     * @return XMLParse containing xmldata. Returns null if the StreamPacket is not of type
     * XML_MESSAGE.
     */
    public static Document extractXmlMessage(StreamPacket packet) {
        if (packet.getType() != PacketType.RACE_XML &&
            packet.getType() != PacketType.REGATTA_XML &&
            packet.getType() != PacketType.BOAT_XML) {
            return null;
        }

        byte[] payload = packet.getPayload();
        int messageType = payload[9];
        long messageLength = bytesToLong(Arrays.copyOfRange(payload, 12, 14));
        String xmlMessage = new String(
            (Arrays.copyOfRange(payload, 14, (int) (14 + messageLength)))).trim();

        //Create XML document Object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(new StringReader(xmlMessage)));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * Extracts the race start status from the packet and returns it as a long array.
     *
     * @param packet Packet parsed in to use the payload
     * @return An array of form [raceID, raceStartTime, notificationType, timeStamp] or null if the
     * packet type is not of RACE_START_STATUS.
     */
    public static RaceStartData extractRaceStartStatus(StreamPacket packet) {
        if (packet.getType() != PacketType.RACE_START_STATUS) {
            return null;
        }
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long raceStartTime = bytesToLong(Arrays.copyOfRange(payload, 9, 15));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 15, 19));
        int notificationType = payload[19];
        return new RaceStartData(raceId, raceStartTime, notificationType, timeStamp);
    }

    /**
     * Parses the the byte array in a StreamPacket for yacht events to retrieve the necessary info
     * and returns it as YachtEventData.
     *
     * @param packet Packet parsed in to use the payload
     * @return the event data in the form of YachtEventData. Returns null if the packet is not of
     * type YACHT_EVENT_CODE.
     */
    public static YachtEventData extractYachtEventCode(StreamPacket packet) {
        if (packet.getType() != PacketType.YACHT_EVENT_CODE) {
            return null;
        }
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long ackNumber = bytesToLong(Arrays.copyOfRange(payload, 7, 9));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 9, 13));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload, 13, 17));
        long incidentId = bytesToLong(Arrays.copyOfRange(payload, 17, 21));
        int eventId = payload[21];
        return new YachtEventData(subjectId, incidentId, eventId, timeStamp);
    }

    /**
     * Parses data from a StreamPacket for yacht actions and returns it in a long array.
     *
     * @param packet Packet parsed in to use the payload
     * @return long array of packet data in the form [subjectID, incidentID, eventID, timeStamp].
     * Returns null if the packet is not of type YACHT_ACTION_CODE.
     */
    public static long[] extractYachtActionCode(StreamPacket packet) {
        if (packet.getType() != PacketType.YACHT_ACTION_CODE) {
            return null;
        }
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload, 9, 13));
        long incidentId = bytesToLong(Arrays.copyOfRange(payload, 13, 17));
        int eventId = payload[17];
        return new long[]{subjectId, incidentId, eventId, timeStamp};
    }

    /**
     * Strips the message from the chatter text type packets.
     *
     * @param packet Packet parsed in to use the payload
     * @return Chatter text message as a string. Returns null if the packet is not of type
     * CHATTER_TEXT.
     */
    public static String extractChatterText(StreamPacket packet) {
        if (packet.getType() != PacketType.CHATTER_TEXT) {
            return null;
        }
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int messageType = payload[1];
        int length = payload[2];
        return new String(Arrays.copyOfRange(payload, 3, 3 + length));
    }

    /**
     * Takes the data from a bot location stream packet and parses the id, timeValid, lat, lon,
     * heading and groundspeed into a BoatPositionPacket which is returned.
     *
     * @param packet Packet parsed in to use the payload
     * @return BoatPositionPacket containing important boat information. Returns null if the packet
     * is not of type BOAT_LOCATION.
     */
    public static PositionUpdateData extractBoatLocation(StreamPacket packet) {
        if (packet.getType() != PacketType.BOAT_LOCATION) {
            return null;
        }
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
        double heading = bytesToLong(Arrays.copyOfRange(payload, 28, 30));
        heading = 360.0 / 0xffff * heading; //Convert to degrees.
        double groundSpeed = bytesToLong(Arrays.copyOfRange(payload, 38, 40)) / 1000.0;

        DeviceType type;
        if (deviceType == 1) {
            type = DeviceType.YACHT_TYPE;
        } else {
            type = DeviceType.MARK_TYPE;
        }

        return new PositionUpdateData((int) boatId, type, lat, lon, heading, groundSpeed);
    }

    /**
     * Processes a stream packet for a mark rounding and returns the boatID, markID and timestamp.
     *
     * @param packet The packet containing the payload
     * @return an array containing longs. The values are [boatID, markID, timeStamp]. Returns null
     * if packet is not of type MARK_ROUNDING.
     */
    public static MarkRoundingData extractMarkRounding(StreamPacket packet) {
        if (packet.getType() != PacketType.MARK_ROUNDING) {
            return null;
        }
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = bytesToLong(Arrays.copyOfRange(payload, 1, 7));
        long raceId = bytesToLong(Arrays.copyOfRange(payload, 9, 13));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload, 13, 17));
        int boatStatus = payload[17];
        int roundingSide = payload[18];
        int markType = payload[19];
        int markId = payload[20];

        return new MarkRoundingData((int) subjectId, markId, roundingSide, timeStamp);
    }

    /**
     * Returns a list containing the string value of data within the given stream packet for course
     * wind.
     *
     * @param packet The packet containing the payload
     * @return the string values of the wind packet. Returns null if the packet is not of type
     * COURSE_WIND.
     */
    public static List<String> extractCourseWind(StreamPacket packet) {
        if (packet.getType() != PacketType.COURSE_WIND) {
            return null;
        }
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int selectedWindId = payload[1];
        int loopCount = payload[2];
        List<String> windInfo = new ArrayList<>();
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
        return windInfo;
    }

    /**
     * Returns the parsed data from a StreamPacket for average wind data.
     *
     * @param packet The packet containing the payload
     * @return The wind data in the form [rawPeriod, rawSamplePeriod, period2, speed2, period3,
     * speed3, period4, speed4, timestamp] or null if the packet is not of type AVG_WIND.
     */
    public static long[] extractAvgWind(StreamPacket packet) {
        if (packet.getType() != PacketType.AVG_WIND) {
            return null;
        }
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
        return new long[]{
            rawPeriod, rawSamplePeriod, period2, speed2, period3, speed3, period4, speed4, timeStamp
        };
    }


    public static void extractBoatAction(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long actionType = bytesToLong(Arrays.copyOfRange(payload, 0, 1));
        if (actionType == 1) {
            System.out.println("VMG");
        } else if (actionType == 2) {
            System.out.println("SAILS IN");
        } else if (actionType == 3) {
            System.out.println("SAILS OUT");
        } else if (actionType == 4) {
            System.out.println("TACK/GYBE");
        } else if (actionType == 5) {
            System.out.println("UPWIND");
        } else if (actionType == 6) {
            System.out.println("DOWNWIND");
        }
    }

    /**
     * takes an array of up to 7 bytes and returns a positive long constructed from the input bytes
     *
     * @param bytes the byte array to conver to Long
     * @return a positive long if there is less than 7 bytes -1 otherwise
     */
    public static long bytesToLong(byte[] bytes) {
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
}

