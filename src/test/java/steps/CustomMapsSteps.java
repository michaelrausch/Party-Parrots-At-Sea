package steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import java.io.File;
import org.junit.Assert;
import seng302.visualiser.MapMaker;

/**
 * Created by cir27 on 26/09/17.
 */
public class CustomMapsSteps {

    MapMaker mapMaker;


    @Given("^that the game has multiple race xml files$")
    public void that_the_game_has_multiple_race_xml_files() throws Throwable {
        mapMaker = MapMaker.getInstance();
        String firstMap = mapMaker.getCurrentRacePath();
        int numMaps = 0;
        do {
            mapMaker.next();
            numMaps++;
        } while (!mapMaker.getCurrentRacePath().equals(firstMap));
        Assert.assertTrue(numMaps >= 2);
    }

    @Then("^all of them can be seen$")
    public void all_of_them_can_be_seen() throws Throwable {
        File[] files = new File(this.getClass().getResource("/maps/").getPath()).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                Assert.assertTrue(file.getAbsolutePath().equals(mapMaker.getCurrentRacePath()));
                mapMaker.next();
                System.out.println(file.getAbsolutePath());
            }
        }
    }

    @Given("^that I choose a race$")
    public void that_I_choose_a_race() throws Throwable {

    }

    @Then("^that race's course is received by clients$")
    public void that_race_s_course_is_received_by_clients() throws Throwable {

    }

    @Given("^that I choose a name for the server$")
    public void that_I_choose_a_name_for_the_server() throws Throwable {

    }

    @Then("^that name is sent to the client$")
    public void that_name_is_sent_to_the_client() throws Throwable {

    }

    @Given("^that the client has received a race$")
    public void that_the_client_has_received_a_race() throws Throwable {
    }

    @Then("^the name of that race shown to the host is the course name$")
    public void the_name_of_that_race_shown_to_the_host_is_the_course_name() throws Throwable {

    }
}
