package seng302;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for the Team class.
 */
public class BoatTest 
{

    @Test
    public void testBoatCreation()
    {
        Boat boat1 = new Boat("Team 1");
        assertEquals(boat1.getTeamName(), "Team 1");
    }

    @Test
    public void testChangeTeamName()
    {
    	Boat boat1 = new Boat("Team 1");
    	boat1.setTeamName("Team 2");
    	assertEquals(boat1.getTeamName(), "Team 2");
    }
}
