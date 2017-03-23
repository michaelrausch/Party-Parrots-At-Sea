package seng302.models.parsers;

import org.junit.Before;
import org.junit.Test;
import seng302.models.Boat;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Haoming on 18/03/17.
 */
public class TeamsParserTest {

	private TeamsParser tp;
	@Before
	public void readFile() {
		tp = new TeamsParser("doc/examples/teams.xml");
	}

	@Test
	public void getBoats() throws Exception {
		ArrayList<Boat> boats = tp.getBoats();

		assertEquals(6, boats.size(), 1e-10);

		assertEquals("Oracle Team USA", boats.get(0).getTeamName());
		assertEquals(10, boats.get(0).getVelocity(), 1e-10);

		assertEquals("Groupama Team France", boats.get(5).getTeamName());
		assertEquals(10, boats.get(5).getVelocity(), 1e-10);
	}

}