package seng302;

import org.junit.Test;
import seng302.models.Boat;
import seng302.models.Event;
import seng302.models.Leg;

import static org.junit.Assert.assertEquals;

/**
 * Test for Event class
 * Created by Haoming on 7/03/17.
 */
public class EventTest {

    @Test
    public void getTimeString() throws Exception {
        Leg leg = new Leg(35, 100, "Start");
        Boat boat = new Boat("testBoat");
        Event event = new Event(1231242, boat, leg);
        assertEquals("20:31:242", event.getTimeString());
    }

    /**
    * ensure all boats are added as they pass the marker
    */
    @Test
    public void boatOrderTest() throws Exception {
        Leg leg = new Leg(35, 100, "1");

        Boat boat1 = new Boat("testBoat");
        Boat boat2 = new Boat("testBoat2");

        Event event1 = new Event(1231242, boat1, leg);
        Event event2 = new Event(1231242, boat2, leg);

        event1.boatPassedMarker();
        event2.boatPassedMarker();

        assertEquals(event1.getLeg().getMarker().getBoats()[0].getTeamName(), "testBoat");
        assertEquals(event2.getLeg().getMarker().getBoats()[1].getTeamName(), "testBoat2");
    }
}