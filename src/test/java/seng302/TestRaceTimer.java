package seng302;

import org.junit.Test;
import seng302.controllers.RaceTimerController;
import seng302.models.Race;

import static org.junit.Assert.assertTrue;


public class TestRaceTimer {
    @Test
    public void testPositiveTimeString(){
        RaceTimerController controller = new RaceTimerController(new Race());
        String result = controller.convertTimeToMinutesSeconds(61);

        assertTrue(result.equals("01:01"));
    }

    @Test
    public void testNegativeTimeString(){
        RaceTimerController controller = new RaceTimerController(new Race());
        String result = controller.convertTimeToMinutesSeconds(-61);

        assertTrue(result.equals("-01:01"));
    }
}
