package seng302.model;

import static seng302.gameServer.GameState.checkCollision;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.gameServer.GameState;
import seng302.model.mark.MarkOrder;
import seng302.utilities.GeoUtility;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Test update function in Yacht.java to make sure yacht will not be collide each other within 25.0
 * meters.
 */
public class UpdateYachtTest {

    private ServerYacht yacht1 = new ServerYacht(BoatMeshType.DINGHY, 1, "1", "Yacht" + 1,
        "Yacht" + 1, "Test1");
    private ServerYacht yacht2 = new ServerYacht(BoatMeshType.DINGHY, 2, "2", "Yacht" + 2,
        "Yacht" + 2, "Test2");
    private GeoPoint geoPoint1 = new GeoPoint(50.0, 50.0);
    private GeoPoint geoPoint2 = GeoUtility.getGeoCoordinate(geoPoint1, 90.0, 50.0);

    @Before
    public void setUpRace() {
        new GameState();
        GameState.addYacht(1, yacht1);
        GameState.addYacht(2, yacht2);
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
        GameState.setRace(XMLParser.parseRace(doc));
        PolarTable.parsePolarFile(getClass().getResourceAsStream("/server_config/acc_polars.csv"));
    }

    @Test
    public void testUpdateYachtWithCollision() {
        // Yacht 1 heading towards 90 degrees heading
        yacht1.setLocation(geoPoint1);

        // Yacht 2 heading towards 270 degrees heading
        yacht2.setLocation(geoPoint1);

        // Start yacht 1 and rest yacht 2
        if (!yacht1.getSailIn()) {
            yacht1.toggleSailIn();
        }
        checkCollision(yacht1);
        double moved = GeoUtility.getDistance(yacht1.getLocation(), geoPoint1);
        Assert.assertEquals(GameState.BOUNCE_DISTANCE_YACHT, moved, 0.1);
    }

    @Test
    public void testUpdateYachtWithoutCollision() {
        // Yacht 1 heading towards 90 degrees heading
        yacht1.setLocation(geoPoint1);

        // Yacht 2 heading towards 270 degrees heading
        yacht2.setLocation(geoPoint2);

        // Start yacht 1 and rest yacht 2
        if (!yacht1.getSailIn()) {
            yacht1.toggleSailIn();
        }

        checkCollision(yacht1);

        Assert.assertTrue(
            GameState.YACHT_COLLISION_DISTANCE < GeoUtility.getDistance(geoPoint1, geoPoint2
            )
        ); //Check that yachts are actually far enough apart for no collision.
        Assert.assertEquals(geoPoint1.getLat(), yacht1.getLocation().getLat(), 1.001);
        Assert.assertEquals(geoPoint1.getLng(), yacht1.getLocation().getLng(), 1.001);
        Assert.assertEquals(geoPoint2.getLat(), yacht1.getLocation().getLat(), 1.001);
        Assert.assertEquals(geoPoint2.getLng(), yacht1.getLocation().getLng(), 1.001);
    }
}
