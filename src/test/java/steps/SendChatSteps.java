package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import javafx.scene.layout.Pane;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.GameClient;

/**
 * Created by kre39 on 7/08/17.
 */
public class SendChatSteps {

    private boolean dcSent = false;
    private ClientToServerThread client;
    private ClientToServerThread host;
    private MainServerThread mst;


//TODO Need to mock the controller pane in order to run the full game client
    @Given("^There are two games running$")
    public void the_are_two_games_running() throws Throwable {
        mst = new MainServerThread();
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


    @When("^the user has pressed sends the message \"([^\"]*)\" in a text box$")
    public void the_user_has_pressed_sends_the_message_in_a_text_box(String arg1) throws Throwable {
        client.sendChatterMessage("[time_prefix] <name_prefix> "  + arg1);
    }

    @Then("^the other client should receive the message \"([^\"]*)\"$")
    public void the_other_client_should_receive_the_message(String arg1) throws Throwable {
        System.out.println("HERE IT IS" + host.getPacketQueue().peek());
        mst.terminate();
        host.setSocketToClose();
        client.setSocketToClose();
    }

}