package seng302.models.parsers;

import org.junit.Before;
import org.junit.Test;
import seng302.models.mark.*;

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
		cp = new CourseParser("/config/course.xml");
	}

	@Test
	public void getGates() throws Exception {
		ArrayList<Mark> course = cp.getCourse();

		assertTrue(MarkType.OPEN_GATE == course.get(0).getMarkType());

		GateMark gateMark1 = (GateMark) course.get(0);
		assertEquals(32.293771, gateMark1.getSingleMark2().getLatitude(), 0.00000001);
		assertEquals(-64.855242, gateMark1.getSingleMark2().getLongitude(), 0.00000001);

		GateMark gateMark2 = (GateMark) course.get(5);

		assertEquals("Finish1", gateMark2.getSingleMark1().getName());
		assertEquals("Finish2", gateMark2.getSingleMark2().getName());
		assertEquals(32.317257, gateMark2.getSingleMark2().getLatitude(), 0.00000001);
		assertEquals(-64.83626, gateMark2.getSingleMark2().getLongitude(), 0.00000001);
	}

	@Test
	public void getMarks() throws Exception {
		ArrayList<Mark> course = cp.getCourse();
		assertEquals("Mid Mark", course.get(1).getName());
	}

	@Test
	public void getOrder() {
		ArrayList<Mark> course = cp.getCourse();
		assertEquals(6, course.size());
		assertEquals("Start", course.get(0).getName());
		assertEquals("Mid Mark", course.get(1).getName());
		assertEquals("Leeward Gate", course.get(2).getName());
		assertEquals("Windward Gate", course.get(3).getName());
		assertEquals("Leeward Gate", course.get(4).getName());
		assertEquals("Finish", course.get(5).getName());
	}

}