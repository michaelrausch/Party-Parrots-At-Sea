package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.messages.BoatAction;
import seng302.model.ServerYacht;
import seng302.model.mark.MarkOrder;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 *
 * Created by kre39 on 7/08/17.
 */
public class ToggleSailSteps {

    MainServerThread mst;
    ClientToServerThread client;
    long startTime;

    @Given("^The game is running$")
    public void the_game_is_running() throws Throwable {
        mst = new MainServerThread();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        client = new ClientToServerThread("localhost", mst.getPortNumber());
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        XMLGenerator xmlGenerator = new XMLGenerator();
        xmlGenerator.setRaceTemplate(
                XMLParser.parseRaceDef(
                        "/maps/default.xml", "test", 2, null, false
                ).getValue()
        );
        GameState.setCurrentStage(GameStages.RACING);
        GameState.addYacht(1, new ServerYacht(BoatMeshType.DINGHY, 1, "0", "", "", ""));
        Thread.sleep(200); // Sleep needed to help the threads all be up to speed with each other
        ServerYacht yacht = (new ArrayList<>(GameState.getYachts().values())).get(0);
        Assert.assertFalse(yacht.getSailIn());
    }


    @When("^the user has pressed \"([^\"]*)\"$")
    public void the_user_has_pressed(String arg1) throws Throwable {
        startTime = System.currentTimeMillis();
        if (arg1 == "shift") {
            client.sendBoatAction(BoatAction.SAILS_IN);
        }
    }

    @Then("^the sails are \"([^\"]*)\"$")
    public void the_sails_are(String arg1) throws Throwable {
        Thread.sleep(200); // Sleep needed to help the threads all be up to speed with each other
        ServerYacht yacht = (new ArrayList<>(GameState.getYachts().values())).get(0);
        if (arg1 == "in") {
            Assert.assertTrue(yacht.getSailIn());
        } else {
            Assert.assertFalse(yacht.getSailIn());
        }
        mst.terminate();
        client.setSocketToClose();
    }
}
