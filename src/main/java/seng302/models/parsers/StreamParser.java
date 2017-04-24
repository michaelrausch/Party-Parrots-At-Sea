package seng302.models.parsers;


import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by kre39 on 23/04/17.
 */
public class StreamParser {

    private static boolean isWithinTag;


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
        byte[] latBytes = Arrays.copyOfRange(payload,16,20);
        byte[] lonBytes = Arrays.copyOfRange(payload,20,24);
        byte[] boatIdBytes = Arrays.copyOfRange(payload,8,12);
        int boatId = ByteBuffer.wrap(boatIdBytes).getInt();
        int lat = ByteBuffer.wrap(latBytes).getInt();
        int lon = ByteBuffer.wrap(lonBytes).getInt();
//        System.out.println("boatId = " + boatId);
//        System.out.println("lon = " + 180 * (lon/Math.pow(2,31)));
//        System.out.println("lat = " + 180 * (lat/Math.pow(2,31)));
    }

    public static int toInt(byte[] bytes, int offset) {

        int ret = 0;
        for (int i=0; i<4 && i+offset<bytes.length; i++) {
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }

}

