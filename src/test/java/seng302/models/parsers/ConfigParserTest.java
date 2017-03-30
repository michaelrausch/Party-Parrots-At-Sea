package seng302.models.parsers;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Haoming on 23/03/17.
 */
public class ConfigParserTest {

	private ConfigParser cp;

	@Before
	public void initializeParser() throws Exception {
		cp = new ConfigParser("/config/config.xml");
	}

	@Test
	public void getWindDirection() throws Exception {
		assertEquals(135, cp.getWindDirection(), 1e-10);
	}

	@Test
	public void getTimeScale() throws Exception {
		assertEquals(10.0, cp.getTimeScale(), 1e-10);
	}

	@Test
	public void getDoubleByTagName() throws Exception {
		assertEquals(6, cp.getDoubleByTagName("race-size", 0), 1e-10);
		assertEquals(100, cp.getDoubleByTagName("noTag", 100), 1e-10);
	}

	@Test
	public void getStringByTagName() throws Exception {
		assertEquals("AC35", cp.getStringByTagName("race-name", "11"));
		assertEquals("oops", cp.getStringByTagName("noTag", "oops"));
	}

}