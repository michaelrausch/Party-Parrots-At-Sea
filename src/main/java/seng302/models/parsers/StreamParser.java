package seng302.models.parsers;


import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kre39 on 23/04/17.
 */
public class StreamParser extends Thread{

     public static ConcurrentHashMap<Long,Point3D> boatPositions = new ConcurrentHashMap<>();
     private static ArrayList<Long> boat_IDS = new ArrayList<>();
     private String threadName;
     private Thread t;

     StreamParser(String threadName){
        this.threadName = threadName;
     }

     public void run(){
         try {
             while (StreamReceiver.packetBuffer.size() <= 1) {
                 Thread.sleep(1);
             }
             StreamPacket packet = StreamReceiver.packetBuffer.take();
             while (packet != null){
                 parsePacket(packet);
                 Thread.sleep(10);
                 packet = StreamReceiver.packetBuffer.take();
             }
         } catch (Exception e){
             e.printStackTrace();
         }
     }

    public void start () {
        System.out.println("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }

     static void parsePacket(StreamPacket packet) {
        switch (packet.getType()){
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
            default:
                break;
                //System.out.println(packet.getType().toString());
        }
    }

    private static void extractHeartBeat(StreamPacket packet) {
        long heartbeat = bytesToLong(packet.getPayload());
//        System.out.println("Heartbeat: " + heartbeat);

    }

    private static void extractRaceStatus(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long currentTime = extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
        long raceId = bytesToLong(Arrays.copyOfRange(payload,7,11));
        int raceStatus = payload[11];
//        System.out.println("raceStatus = " + raceStatus);
        long expectedStartTime = extractTimeStamp(Arrays.copyOfRange(payload,12,18), 6);
        long windDir = bytesToLong(Arrays.copyOfRange(payload,18,20));
        long windSpeed = bytesToLong(Arrays.copyOfRange(payload,20,22));
        int noBoats = payload[22];
        int raceType = payload[23];
        ArrayList<String> boatStatuses = new ArrayList<>();
        for (int i = 0; i < noBoats; i++){
            String boatStatus = "SourceID: " + bytesToLong(Arrays.copyOfRange(payload,24 + (i * 20),28+ (i * 20)));
            boatStatus += "\nBoat Status: " + (int)payload[28 + (i * 20)];
            boatStatus += "\nLegNumber: " + (int)payload[29 + (i * 20)];
            boatStatus += "\nPenaltiesAwarded: " + (int)payload[29 + (i * 20)];
            boatStatus += "\nPenaltiesServed: " + (int)payload[30 + (i * 20)];
            boatStatus += "\nEstTimeAtNextMark: " + extractTimeStamp(Arrays.copyOfRange(payload,31 + (i * 20),37+ (i * 20)), 6);
            boatStatus += "\nEstTimeAtFinish: " + extractTimeStamp(Arrays.copyOfRange(payload,37 + (i * 20),43+ (i * 20)), 6);
            boatStatuses.add(boatStatus);
        }
    }

    private static void extractDisplayMessage(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int numOfLines = payload[3];
        int totalLen = 0;
        for (int i = 0; i < numOfLines; i++){
            int lineNum = payload[4 + totalLen];
            int textLength = payload[5 + totalLen];
            byte[] messageTextBytes = Arrays.copyOfRange(payload,6 + totalLen,6 + textLength + totalLen);
            String messageText = new String(messageTextBytes);
            totalLen += 2 + textLength;
        }
    }

    static void extractXmlMessage(StreamPacket packet){

        byte[] payload = packet.getPayload();
        String xmlMessage = "";

        ByteArrayInputStream payloadStream = new ByteArrayInputStream(payload);

        //Bunch of data we don't want (Message Version Number, AckNumber, Timestamp)
        payloadStream.skip(9);
        int xmlMessageSubType = payloadStream.read();
        payloadStream.skip(2);

        //checks the length of the xml message itself
        int xmlMessageLength = payloadStream.read() | payloadStream.read() << 8;

        //Converts XML message to string to be parsed
        int currentChar;
        while (payloadStream.available() > 0 && (currentChar = payloadStream.read()) != 0) {
        xmlMessage += (char)currentChar;
        }

        //Create XML document Object
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlMessage)));
            // TODO: 25/04/17 ajm412: Check that the object matches expected structure and return Document object.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void extractRaceStartStatus(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
        long raceStartTime = extractTimeStamp(Arrays.copyOfRange(payload,9,15), 6);
        long raceId = bytesToLong(Arrays.copyOfRange(payload,15,19));
        int notificationType = payload[19];
    }

    private static void extractYachtEventCode(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
        long raceId = bytesToLong(Arrays.copyOfRange(payload,9,13));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload,13,17));
        long incidentId = bytesToLong(Arrays.copyOfRange(payload,17,21));
        int eventId = payload[21];
    }

    private static void extractYachtActionCode(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
        long subjectId = bytesToLong(Arrays.copyOfRange(payload,9,13));
        long incidentId = bytesToLong(Arrays.copyOfRange(payload,13,17));
        int eventId = payload[17];
//        System.out.println("eventId = " + eventId);
    }

    private static void extractChatterText(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int messageType = payload[1];
        int length = payload[2];
        String message = new String(Arrays.copyOfRange(payload,3,3 + length));
    }


    static void extractBoatLocation(StreamPacket packet){
        byte[] payload = packet.getPayload();
        byte deviceType = payload[15];
        byte[] seqBytes = Arrays.copyOfRange(payload,11,15);
        byte[] latBytes = Arrays.copyOfRange(payload,16,20);
        byte[] lonBytes = Arrays.copyOfRange(payload,20,24);
        byte[] boatIdBytes = Arrays.copyOfRange(payload,7,11);
        byte[] headingBytes = Arrays.copyOfRange(payload,28,30);
        long timeStamp = extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
//        int boatSeq =  ByteBuffer.wrap(seqBytes).getInt();
        long seq = bytesToLong(seqBytes);
        long boatId = bytesToLong(boatIdBytes);
        long lat = bytesToLong(latBytes);
        long lon = bytesToLong(lonBytes);
        long heading = bytesToLong(headingBytes);

        if ((int)deviceType == 1){
//            System.out.println("boatId = " + boatId);
//            System.out.println("deviceType = " + (long)deviceType);
//            System.out.println("seq = " + seq);
            //needs to be validated
            Point3D point = new Point3D(((180d * (double)lat)/Math.pow(2,31)),((180d *(double)lon)/Math.pow(2,31)),(double)heading);
            boatPositions.putIfAbsent(boatId, point);
            boatPositions.replace(boatId, point);
//            System.out.println("lon = " + ((180d * (double)lon)/Math.pow(2,31)));
//            System.out.println("lat = " +  ((180d *(double)lat)/Math.pow(2,31)));
        }
    }


    private static void extractMarkRounding(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
        long raceId = bytesToLong(Arrays.copyOfRange(payload,9,13));
        long subjectId = bytesToLong(Arrays.copyOfRange(payload,13,17));
        int boatStatus = payload[17];
        int roundingSide = payload[18];
        int markType = payload[19];
        int markId = payload[20];
    }

    private static void extractCourseWind(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        int selectedWindId = payload[1];
        int loopCount = payload[2];
        ArrayList<String> windInfo = new ArrayList<>();
        for (int i = 0; i < loopCount; i++){
            String wind = "WindId: " + payload[3 + (20 * i)];
            wind += "\nTime: " + extractTimeStamp(Arrays.copyOfRange(payload,4 + (20 * i),10 + (20 * i)), 6);
            wind += "\nRaceId: " + bytesToLong(Arrays.copyOfRange(payload,10 + (20 * i),14 + (20 * i)));
            wind += "\nWindDirection: " + bytesToLong(Arrays.copyOfRange(payload,14 + (20 * i),16 + (20 * i)));
            wind += "\nWindSpeed: " + bytesToLong(Arrays.copyOfRange(payload,16 + (20 * i),18 + (20 * i)));
            wind += "\nBestUpWindAngle: " + bytesToLong(Arrays.copyOfRange(payload,18 + (20 * i),20 + (20 * i)));
            wind += "\nBestDownWindAngle: " + bytesToLong(Arrays.copyOfRange(payload,20 + (20 * i),22 + (20 * i)));
            wind += "\nFlags: " + String.format("%8s", Integer.toBinaryString(payload[22 + (20 * i)] & 0xFF)).replace(' ', '0');
            windInfo.add(wind);
        }
    }

    private static void extractAvgWind(StreamPacket packet){
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long timeStamp = extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
        long rawPeriod = bytesToLong(Arrays.copyOfRange(payload,7,9));
        long rawSamplePeriod = bytesToLong(Arrays.copyOfRange(payload,9,11));
        long period2 = bytesToLong(Arrays.copyOfRange(payload,11,13));
        long speed2 = bytesToLong(Arrays.copyOfRange(payload,13,15));
        long period3 = bytesToLong(Arrays.copyOfRange(payload,15,17));
        long speed3 = bytesToLong(Arrays.copyOfRange(payload,17,19));
        long period4 = bytesToLong(Arrays.copyOfRange(payload,19,21));
        long speed4 = bytesToLong(Arrays.copyOfRange(payload,21,23));
    }

    private static long extractTimeStamp(byte[] timeStampBytes, int noOfBytes){
        long timeStamp = 0;
        long multiplier=1;
        for(int i = 0;i < noOfBytes;i++) {
            timeStamp += timeStampBytes[i]*multiplier;
            multiplier *= 256;
        }
        return timeStamp;
    }

    /**
     * takes an array of up to 7 bytes and returns a positive
     * long constructed from the input bytes
     *
     * @return a positive long if there is less than 7 bytes -1 otherwise
     */
    private static long bytesToLong(byte[] bytes){
        long partialLong = 0;
        int index = 0;
        for (byte b: bytes){
            if (index > 6){
                return -1;
            }
            partialLong = partialLong | (b & 0xFFL) << (index * 8);
            index++;
        }
        return partialLong;
    }
}

