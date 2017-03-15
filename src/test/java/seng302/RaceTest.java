package seng302;

import org.junit.Test;
import seng302.models.Boat;
import seng302.models.Race;

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
        Boat boat1 = new Boat("Team 1");
        Boat boat2 = new Boat("Team 2");

        Race race = new Race();
        race.addBoat(boat1);
        race.addBoat(boat2);

        assertEquals(Array.getLength(race.getBoats()), 2);
    }

    @Test
    public void testGetShuffledBoats(){
        Boat boat1 = new Boat("Team 1");
        Boat boat2 = new Boat("Team 2");

        Race race = new Race();
        race.addBoat(boat1);
        race.addBoat(boat2);

        assertEquals(Array.getLength(race.getShuffledBoats()), 2);
    }
}
