package seng302.models.parsers;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import seng302.models.Boat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by ryan_ on 30/04/2017.
 */
public class BoatsParser extends FileParser {
    private Document doc;

    public BoatsParser(String xmlString) {
        this.doc = this.parseFile(xmlString);
    }

    /**
     * Create a boat instance from a given node if 'Type' is 'Yacht'
     *
     * @param node a boat node
     * @return an instance of Boat
     */
    private Boat parseBoat(Node node) {
        try {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getAttribute("Type").equals("Yacht")) {
                    String sourceID = element.getAttribute("SourceID");
                    String boatName = element.getAttribute("BoatName");
                    String shortName = element.getAttribute("ShortName");
                    String stoweName = element.getAttribute("StoweName");
                    String country = element.getAttribute("Country");
                    Boat boat = new Boat(Integer.parseInt(sourceID), boatName, shortName, country);
                    return boat;
                }
            } else {
                throw new NoSuchElementException("Cannot generate a boat by given node");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a list of boats from the xml.
     *
     * @return a list of boats
     */
    public List<Boat> getBoats() {
        ArrayList<Boat> boats = new ArrayList<>();

        try {
            NodeList nodes = this.doc.getElementsByTagName("Boat");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                Boat boat = parseBoat(node);
                if (!(boat == null)) {
                    boats.add(boat);
                }
            }
            return boats;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
