package seng302.models.parsers;


import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

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


}
