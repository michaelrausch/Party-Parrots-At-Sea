package seng302;

import org.junit.Test;
import seng302.visualiser.controllers.RaceViewController;

import static org.junit.Assert.assertTrue;


public class TestRaceTimer {
    @Test
    public void testPositiveTimeString(){
        RaceViewController controller = new RaceViewController();
        String result = controller.convertTimeToMinutesSeconds(61);

        assertTrue(result.equals("01:01"));
   }

   @Test
   public void testNegativeTimeString(){
		RaceViewController controller = new RaceViewController();
        String result = controller.convertTimeToMinutesSeconds(-61);

        assertTrue(result.equals("-01:01"));
    }
}
