package seng302.models.parsers;


import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by kre39 on 23/04/17.
 */
public class StreamParser {

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
                System.out.println(packet.getType().toString());
        }
    }

    private static void extractHeartBeat(StreamPacket packet){
        System.out.println(bytesToLong(packet.getPayload()));
    }

    private static void extractRaceStatus(StreamPacket packet){

    }

    private static void extractDisplayMessage(StreamPacket packet){

    }

    private static void extractXmlMessage(StreamPacket packet){

    }

    private static void extractRaceStartStatus(StreamPacket packet){

    }

    private static void extractYachtEventCode(StreamPacket packet){

    }

    private static void extractYachtActionCode(StreamPacket packet){

    }

    private static void extractChatterText(StreamPacket packet){

    }


    static void extractBoatLocation(StreamPacket packet){
        byte[] payload = packet.getPayload();
        byte deviceType = payload[15];
        byte[] seqBytes = Arrays.copyOfRange(payload,11,15);
        byte[] latBytes = Arrays.copyOfRange(payload,16,20);
        byte[] lonBytes = Arrays.copyOfRange(payload,20,24);
        byte[] boatIdBytes = Arrays.copyOfRange(payload,8,12);
        extractTimeStamp(Arrays.copyOfRange(payload,1,7), 6);
//        int boatSeq =  ByteBuffer.wrap(seqBytes).getInt();
        long seq = bytesToLong(seqBytes);
        long boatId = bytesToLong(boatIdBytes);
        long lat = bytesToLong(latBytes);
        long lon = bytesToLong(lonBytes);

        if (boatId != 0){
//            System.out.println("boatId = " + boatId);
//            System.out.println("deviceType = " + (long)deviceType);
//            System.out.println("seq = " + seq);
            //needs to be validated
//            System.out.println("lon = " + ((180d * (double)lon)/Math.pow(2,31)));
//            System.out.println("lat = " +  ((180d *(double)lat)/Math.pow(2,31)));
        }

    }


    private static void extractMarkRounding(StreamPacket packet){

    }

    private static void extractCourseWind(StreamPacket packet){

    }

    private static void extractAvgWind(StreamPacket packet){

    }

    private static void extractTimeStamp(byte[] timeStampBytes, int noOfBytes){
        long timeStamp = 0;
        long multiplier=1;
        for(int i = 0;i < noOfBytes;i++) {
            timeStamp += timeStampBytes[i]*multiplier;
            multiplier *= 256;
        }
        System.out.println("timeStamp = " + timeStamp);
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

