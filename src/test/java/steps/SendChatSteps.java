package steps;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.ArrayList;

import javafx.scene.layout.Pane;
import org.junit.Assert;
import org.mockito.Mock;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.messages.BoatAction;
import seng302.model.ServerYacht;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.GameClient;
import seng302.visualiser.controllers.StartScreenController;

/**
 * Created by kre39 on 7/08/17.
 */
public class SendChatSteps {

    MainServerThread mst;
    GameClient client1;
    GameClient client2;


//TODO Need to mock the controller pane in order to run the full game client
    @Given("^The are two games running$")
    public void the_are_two_games_running() throws Throwable {
//        client1 = new GameClient(new Pane());
//        client1.runAsHost("localhost", 4942);
////        client2 = new ClientToServerThread("localhost", 4942);
//        GameState.setCurrentStage(GameStages.RACING);
//        Thread.sleep(200);
    }


    @When("^the user has pressed sends the message \"([^\"]*)\" in a text box$")
    public void the_user_has_pressed_sends_the_message_in_a_text_box(String arg1) throws Throwable {
//        client1.getSocketThread().sendChatterMessage(arg1);
    }

    @Then("^the other client should recieve the message \"([^\"]*)\"$")
    public void the_other_client_should_recieve_the_message(String arg1) throws Throwable {
//        System.out.println(client2.getPacketQueue());
//        client2.setSocketToClose();
    }

}