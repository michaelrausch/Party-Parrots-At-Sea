package seng302;

import org.junit.Test;
import seng302.models.Race;
import seng302.models.Yacht;

import java.lang.reflect.Array;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the Race class.
 */
public class RaceTest {
    /**
     * Test that all boats were added to the race
     */
    @Test
    public void testAddingBoatsToRace() {
        Yacht boat1 = new Yacht("Team 1");
        Yacht boat2 = new Yacht("Team 2");

        Race race = new Race();
        race.addBoat(boat1);
        race.addBoat(boat2);

        assertEquals(Array.getLength(race.getBoats()), 2);
    }

    @Test
    public void testGetShuffledBoats(){
        Yacht boat1 = new Yacht("Team 1");
        Yacht boat2 = new Yacht("Team 2");

        Race race = new Race();
        race.addBoat(boat1);
        race.addBoat(boat2);

        assertEquals(Array.getLength(race.getShuffledBoats()), 2);
    }
}
