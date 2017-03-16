package seng302.models.parsers;

import org.junit.Before;
import org.junit.Test;
import seng302.models.GateMark;
import seng302.models.Mark;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * To test if course parser works as expected.
 * Created by Haoming on 17/03/17.
 */
public class CourseParserTest {

	private CourseParser cp;

	@Before
	public void initializeParser() throws Exception {
		cp = new CourseParser("doc/examples/course.xml");
	}

	@Test
	public void getGates() throws Exception {
		ArrayList<GateMark> gateMarks = cp.getGates();
		assertEquals(4, gateMarks.size());

		assertEquals("Start", gateMarks.get(0).getName());
		assertEquals("Leeward Gate", gateMarks.get(1).getName());
		assertEquals("Windward Gate", gateMarks.get(2).getName());
		assertEquals("Finish", gateMarks.get(3).getName());

		assertEquals("Start1", gateMarks.get(0).getMark1().getName());
		assertEquals("Start2", gateMarks.get(0).getMark2().getName());
		assertEquals(32.293834, gateMarks.get(0).getMark2().getLatitude(), 0.00000001);
		assertEquals(-64.855195, gateMarks.get(0).getMark2().getLongitude(), 0.00000001);

		assertEquals("Finish1", gateMarks.get(3).getMark1().getName());
		assertEquals("Finish2", gateMarks.get(3).getMark2().getName());
		assertEquals(32.318303, gateMarks.get(3).getMark2().getLatitude(), 0.00000001);
		assertEquals(-64.834974, gateMarks.get(3).getMark2().getLongitude(), 0.00000001);
	}

	@Test
	public void getMarks() throws Exception {
		ArrayList<Mark> marks = cp.getMarks();
		assertEquals(1, marks.size());
		assertEquals("Mid Mark", marks.get(0).getName());
	}

	@Test
	public void getOrder() throws Exception {
		ArrayList<String> order = cp.getOrder();

		assertEquals(6, order.size());
		assertEquals("Start", order.get(0));
		assertEquals("Mid Mark", order.get(1));
		assertEquals("Leeward Gate", order.get(2));
		assertEquals("Windward Gate", order.get(3));
		assertEquals("Leeward Gate", order.get(4));
		assertEquals("Finish", order.get(5));
	}

}