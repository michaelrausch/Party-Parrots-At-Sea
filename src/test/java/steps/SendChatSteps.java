package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import javafx.util.Pair;
import org.junit.Assert;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.model.stream.packets.StreamPacket;
import seng302.utilities.StreamParser;
import seng302.visualiser.ClientToServerThread;

/**
 * Cucumber test for sending chat messages
 * Created by kre39 on 7/08/17.
 */
public class SendChatSteps {

    private ClientToServerThread client;
    private ClientToServerThread host;
    private MainServerThread mst;


    @Given("^There are two games running$")
    public void the_are_two_games_running() throws Throwable {
        mst = new MainServerThread();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        host = new ClientToServerThread("localhost", 4942);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        client = new ClientToServerThread("localhost", 4942);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        mst.startGame();
        Thread.sleep(200);
    }


    @When("^the first client has sent the message \"([^\"]*)\"$")
    public void the_user_has_pressed_sends_the_message_in_a_text_box(String arg1) throws Throwable {
        client.sendChatterMessage("[time_prefix] <name_prefix> "  + arg1);
    }

    @Then("^the other client should receive the message \"([^\"]*)\"$")
    public void the_other_client_should_receive_the_message(String arg1) throws Throwable {
        Object[] packets = host.getPacketQueue().toArray();
        Pair<Integer, String> message = StreamParser.extractChatterText((StreamPacket) packets[packets.length - 1]);
        Assert.assertEquals("[time_prefix] <name_prefix> "  + arg1, message.getValue());
        mst.terminate();
        host.setSocketToClose();
        client.setSocketToClose();
    }

}