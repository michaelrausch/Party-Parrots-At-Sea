package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.server.messages.BoatActionMessage;
import seng302.model.Yacht;
import seng302.visualiser.ClientToServerThread;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by kre39 on 7/08/17.
 */
public class ToggleSailSteps {


    MainServerThread mst;
    ClientToServerThread client;
    boolean sailsIn = false;
    long startTime;
    private Yacht yacht;



    @Given("^The game is running$")
    public void the_game_is_running() throws Throwable {
        mst = new MainServerThread();
        client = new ClientToServerThread("localhost", 4942);
        GameState.setCurrentStage(GameStages.RACING);
    }

    @When("^the user has pressed \"([^\"]*)\"$")
    public void the_user_has_pressed(String arg1) throws Throwable {
        startTime = System.currentTimeMillis();
        if (arg1 == "shift") {
            if (sailsIn) {
//                client.sendBoatActionMessage(new BoatActionMessage(BoatActionType.SAILS_OUT));
            } else {
//                client.sendBoatActionMessage(new BoatActionMessage(BoatActionType.SAILS_IN));
            }
        }
    }

    @Then("^the sails are \"([^\"]*)\"$")
    public void the_sails_are(String arg1) throws Throwable {
        //Yacht yacht = (new ArrayList<>(GameState.getYachts().values())).get(0);
        //Assert.assertTrue(yacht.getSailIn());
    }
}
