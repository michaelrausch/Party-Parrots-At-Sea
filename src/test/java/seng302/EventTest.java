package seng302;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for Event class
 * Created by Haoming on 7/03/17.
 */
public class EventTest {

	@Test
	public void getTimeString() throws Exception {
		Leg leg = new Leg(035, 100, "Start");
		Boat boat = new Boat("testBoat");
		Event event = new Event(1231242, boat, leg);
		assertEquals("20:31:242", event.getTimeString());
	}
}