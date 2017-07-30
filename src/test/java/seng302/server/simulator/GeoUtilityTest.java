package seng302.server.simulator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import seng302.model.GeoPoint;
import seng302.utilities.GeoUtility;

/**
 * To test methods in GeoUtility.
 * Created by Haoming on 28/04/17.
 */
public class GeoUtilityTest {

	private GeoPoint p1 = new GeoPoint(57.670333, 11.827833);
	private GeoPoint p2 = new GeoPoint(57.671524, 11.844495);
	private GeoPoint p3 = new GeoPoint(57.670822, 11.843392);
	private GeoPoint p4 = new GeoPoint(25.694829, 98.392049);

	private double toleranceRate = 0.01;

	@Test
	public void getDistance() throws Exception {
		double expected, actual;

		actual = GeoUtility.getDistance(p1, p2);
		expected = 1000;
		assertEquals(expected, actual, expected * toleranceRate);

		actual = GeoUtility.getDistance(p1, p3);
		expected = 927;
		assertEquals(expected, actual, expected * toleranceRate);

		actual = GeoUtility.getDistance(p2, p4);
		expected = 7430180;
		assertEquals(expected, actual, expected * toleranceRate);
	}

	@Test
	public void getBearing() throws Exception {
		double expected, actual;

		actual = GeoUtility.getBearing(p1, p2);
		expected = 82;
		assertEquals(expected, actual, expected * toleranceRate);

		actual = GeoUtility.getBearing(p1, p3);
		expected = 86;
		assertEquals(expected, actual, expected * toleranceRate);

		actual = GeoUtility.getBearing(p2, p4);
		expected = 78;
		assertEquals(expected, actual, expected * toleranceRate);
	}

	@Test
	public void getGeoCoordinate() throws Exception {
		GeoPoint expected, actual;

		actual = GeoUtility.getGeoCoordinate(p1, 82.0, 1000.0);
		expected = p2;
		assertEquals(expected.getLat(), actual.getLat(), expected.getLat() * toleranceRate);
		assertEquals(expected.getLng(), actual.getLng(), expected.getLng() * toleranceRate);

		actual = GeoUtility.getGeoCoordinate(p1, 86.0, 927.0);
		expected = p3;
		assertEquals(expected.getLat(), actual.getLat(), expected.getLat() * toleranceRate);
		assertEquals(expected.getLng(), actual.getLng(), expected.getLng() * toleranceRate);

		actual = GeoUtility.getGeoCoordinate(p2, 78.0, 7430180.0);
		expected = p4;
		assertEquals(expected.getLat(), actual.getLat(), expected.getLat() * toleranceRate);
		assertEquals(expected.getLng(), actual.getLng(), expected.getLng() * toleranceRate);
	}

}