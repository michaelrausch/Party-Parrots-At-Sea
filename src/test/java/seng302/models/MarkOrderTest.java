package seng302.models;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.MarkOrder;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;

public class MarkOrderTest {
    private static MarkOrder markOrder;
    private static Integer currentSeqID;

    @BeforeClass
    public static void setup(){
        XMLGenerator xmlGenerator = new XMLGenerator();
        xmlGenerator.setRaceTemplate(
            XMLParser.parseRaceDef(
                "/maps/default.xml", "test", 2, null, false
            ).getValue()
        );
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(new StringReader(xmlGenerator.getRaceAsXml())));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        markOrder = new MarkOrder(XMLParser.parseRace(doc));
        currentSeqID = 0;
    }

    /**
     * Test to ensure marks are loaded from XML
     */
    @Test
    public void testMarkOrderLoadedFromXML(){
        assertTrue(markOrder != null);
    }


    @Test
    public void testIsLastMark() {
        currentSeqID = 0;
        assertFalse(markOrder.isLastMark(currentSeqID));

        currentSeqID = markOrder.getMarkOrder().size() - 1;
        assertTrue(markOrder.isLastMark(currentSeqID));
    }

    @Test
    public void testGetNextMark() {
        currentSeqID = 4;
        CompoundMark nextMark = markOrder.getMarkOrder().get(4 + 1);
        assertEquals(nextMark, markOrder.getNextMark(currentSeqID));

        currentSeqID = 3;
        nextMark = markOrder.getMarkOrder().get(3 + 1);
        assertEquals(nextMark, markOrder.getNextMark(currentSeqID));
    }

    @Test
    public void testGetCurrentMark() {
        currentSeqID = 0;
        CompoundMark currentMark = markOrder.getMarkOrder().get(0);
        assertEquals(currentMark, markOrder.getCurrentMark(0));
    }

    @Test
    public void testGetPreviousMark() {
        currentSeqID = 1;
        CompoundMark prevMark = markOrder.getMarkOrder().get(0);
        assertEquals(prevMark, markOrder.getPreviousMark(currentSeqID));
    }

    @AfterClass
    public static void tearDown(){
        markOrder = null;
    }
}
