package seng302;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import java.lang.reflect.Array;

/**
 * Unit test for the Race class.
 */
public class RaceTest 
{
    /*
        Test that all boats that were added to the race also finish the race
    */ 
    @Test
    public void testFinishingBoats()
    {
        Boat boat1 = new Boat("Team 1");
        Boat boat2 = new Boat("Team 2");
        Boat boat3 = new Boat("Team 3");

        Race race = new Race();
        race.addBoat(boat1);
        race.addBoat(boat2);
        race.addBoat(boat3);

        assertEquals(Array.getLength(race.getFinishedBoats()), 3);
    }

    /*
        Test that all boats were added to the race
    */    
    @Test
    public void testAddingBoatsToRace(){
        Boat boat1 = new Boat("Team 1");
        Boat boat2 = new Boat("Team 2");

        Race race = new Race();
        race.addBoat(boat1);
        race.addBoat(boat2);

        assertEquals(Array.getLength(race.getBoats()), 2);
    }
}
