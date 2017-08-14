package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.util.ArrayList;
import org.junit.Assert;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.server.messages.BoatAction;
import seng302.model.Yacht;
import seng302.visualiser.ClientToServerThread;

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
        Thread.sleep(200); // Sleep needed to help the threads all be up to speed with each other
        Yacht yacht = (new ArrayList<>(GameState.getYachts().values())).get(0);
        Assert.assertFalse(yacht.getSailIn());
    }


    @When("^the user has pressed \"([^\"]*)\"$")
    public void the_user_has_pressed(String arg1) throws Throwable {
        startTime = System.currentTimeMillis();
        if (arg1 == "shift") {
            if (sailsIn) {
                client.sendBoatAction(BoatAction.SAILS_OUT);
            } else {
                client.sendBoatAction(BoatAction.SAILS_IN);
            }
        }
    }

    @Then("^the sails are \"([^\"]*)\"$")
    public void the_sails_are(String arg1) throws Throwable {
        Thread.sleep(200); // Sleep needed to help the threads all be up to speed with each other
        Yacht yacht = (new ArrayList<>(GameState.getYachts().values())).get(0);
        if (arg1 == "in") {
            Assert.assertTrue(yacht.getSailIn());
        } else {
            Assert.assertFalse(yacht.getSailIn());
        }
    }
}
