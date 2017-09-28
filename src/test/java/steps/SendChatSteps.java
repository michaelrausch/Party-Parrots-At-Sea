package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import javafx.util.Pair;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.model.mark.CompoundMark;
import seng302.model.stream.packets.StreamPacket;
import seng302.utilities.StreamParser;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;
import seng302.visualiser.ClientToServerThread;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Cucumber test for sending chat messages
 * Created by kre39 on 7/08/17.
 */
public class SendChatSteps {

    private ClientToServerThread client;
    private ClientToServerThread host;
    private MainServerThread mst;
    private boolean messageReceived = false;
    private String arg = "";


    @Given("^There are two games running$")
    public void the_are_two_games_running() throws Throwable {
        mst = new MainServerThread();
        try {
            Thread.sleep(50);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        host = new ClientToServerThread("localhost", mst.getPortNumber());
        host.addStreamObserver(() -> {
            while (host.getPacketQueue().peek() != null) {
                StreamPacket packet = host.getPacketQueue().poll();
                switch (packet.getType()) {
                    case CHATTER_TEXT:
                        String message = StreamParser.extractChatterText(packet).getValue();
                        messageReceived = message.equals("[time_prefix] <name_prefix> "  + arg);
                        break;
                }
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        host.sendXML("/maps/default.xml", "test", 2, 2, false);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        client = new ClientToServerThread("localhost", mst.getPortNumber());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }    }


    @When("^the first client has sent the message \"([^\"]*)\"$")
    public void the_user_has_pressed_sends_the_message_in_a_text_box(String arg1) throws Throwable {
        GameState.setCurrentStage(GameStages.LOBBYING);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        arg = arg1;
        client.sendChatterMessage("[time_prefix] <name_prefix> "  + arg1);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    @Then("^the other client should receive the message \"([^\"]*)\"$")
    public void the_other_client_should_receive_the_message(String arg1) throws Throwable {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        Assert.assertTrue(messageReceived);
        mst.terminate();
        host.setSocketToClose();
        client.setSocketToClose();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
}