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

    private static boolean isWithinTag;
    public static ArrayList<Long> ids = new ArrayList<>();

    static void parseLine(byte[] bytes) {
        //TODO overhaul all of this to treat packets as appropriate
        String line = new String(bytes);
        if (line.startsWith("<")){
            isWithinTag = true;
        }
//        System.out.println("line = ---------------------------------------------\n" + line);
        if (isWithinTag) {
//            try {
//                Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(line.getBytes())).getDocumentElement();
//                if (node.getAttributes().getNamedItem("Type") != null) {
//                    System.out.println(node.getAttributes().getNamedItem("Type") );
//                    System.out.println(line);
//                }
//            } catch (Throwable e){
////                e.printStackTrace();
//            }
        }
        if (line.startsWith("</")) {
            isWithinTag = false;
        }
    }

    static void extractBoatLocation(byte[] payload){
        byte deviceType = payload[15];
        byte[] seqBytes = Arrays.copyOfRange(payload,11,15);
        byte[] latBytes = Arrays.copyOfRange(payload,16,20);
        byte[] lonBytes = Arrays.copyOfRange(payload,20,24);
        byte[] boatIdBytes = Arrays.copyOfRange(payload,8,12);
        extractTimeStamp(Arrays.copyOfRange(payload,1,7));
//        int boatSeq =  ByteBuffer.wrap(seqBytes).getInt();
        long seq = StreamReceiver.bytesToLong(seqBytes);
        long boatId = StreamReceiver.bytesToLong(boatIdBytes);
        long lat = StreamReceiver.bytesToLong(latBytes);
        long lon = StreamReceiver.bytesToLong(lonBytes);
        if (!ids.contains(boatId)) {
            ids.add(boatId);
        }
        if (boatId != 0){
            System.out.println("boatId = " + boatId);
            System.out.println("deviceType = " + (long)deviceType);
//            System.out.println("seq = " + seq);
            //needs to be validated
            System.out.println("lon = " + ((180d * (double)lon)/Math.pow(2,31)));
            System.out.println("lat = " +  ((180d *(double)lat)/Math.pow(2,31)));
        }

    }

    private static void extractTimeStamp(byte[] timeStampBytes){
        long timeStamp = 0;
        long multiplier=1;
        for(int i = 0;i < 6;i++) {
            timeStamp += timeStampBytes[i]*multiplier;
            multiplier *= 256;
        }
        System.out.println("timeStamp = " + timeStamp);
    }


}

