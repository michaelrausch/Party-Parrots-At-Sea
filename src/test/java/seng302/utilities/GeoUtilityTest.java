package seng302.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import javafx.geometry.Point2D;
import org.junit.Test;
import seng302.model.GeoPoint;

/**
 * http://www.geoplaner.com/ For plotting geo points for visualisation
 * To test methods in GeoUtility.
 * Use this site to calculate distances
 * https://rechneronline.de/geo-coordinates/#distance
 * Created by Haoming on 28/04/17.
 */
public class GeoUtilityTest {


    //Line in x = y
    private Point2D linePoint1 = new Point2D(0, 0);
    private Point2D linePoint2 = new Point2D(1, 1);

    private Point2D arbitraryPoint1 = new Point2D(1, 0);     //Point below x = y
    private Point2D arbitraryPoint2 = new Point2D(0, 1);     //Point above x = y
    private Point2D arbitraryPoint3 = new Point2D(2, 2);     //Point on x = y

    private GeoPoint p1 = new GeoPoint(57.670333, 11.827833);
    private GeoPoint p2 = new GeoPoint(57.671524, 11.844495);
    private GeoPoint p3 = new GeoPoint(57.670822, 11.843392);
    private GeoPoint p4 = new GeoPoint(25.694829, 98.392049);
    private GeoPoint p5 = new GeoPoint(57.671829, 11.842049);

    private double toleranceRate = 0.01;


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


    @Test
    public void testGetDistance() throws Exception {
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
    public void testLineFunction() {

        Integer lineFunctionResult1 = GeoUtility
            .lineFunction(linePoint1, linePoint2, arbitraryPoint1);
        Integer lineFunctionResult2 = GeoUtility
            .lineFunction(linePoint1, linePoint2, arbitraryPoint2);
        Integer lineFunctionResult3 = GeoUtility
            .lineFunction(linePoint1, linePoint2, arbitraryPoint3);

        //Point1 and Point2 are on opposite sides
        assertEquals(Math.abs(lineFunctionResult1), Math.abs(lineFunctionResult2));
        assertNotEquals(lineFunctionResult1, lineFunctionResult2);

        //Point3 is on the line
        assertEquals((long) lineFunctionResult3, 0L);
    }

    @Test
    public void testMakeArbitraryVectorPoint() {

        //Make a point (1,0) from point (0,0)
        Point2D newPoint = GeoUtility.makeArbitraryVectorPoint(linePoint1, 0d, 1d);
        Point2D expected = new Point2D(1, 0);

        assertEquals(expected.getX(), newPoint.getX(), 1E-6);
        assertEquals(expected.getY(), newPoint.getY(), 1E-6);

        newPoint = GeoUtility.makeArbitraryVectorPoint(linePoint1, 90d, 1d);
        expected = new Point2D(0, 1);

        assertEquals(expected.getX(), newPoint.getX(), 1E-6);
        assertEquals(expected.getY(), newPoint.getY(), 1E-6);
    }

    @Test
    public void testIsPointInTriangle() {
        GeoPoint v1 = new GeoPoint(57.670333, 11.842833);
        GeoPoint v2 = new GeoPoint(57.671524, 11.844495);
        GeoPoint v3 = new GeoPoint(57.671829, 11.842049);
        GeoPoint p1 = new GeoPoint(57.670822, 11.843192); // inside triangle
        GeoPoint p2 = new GeoPoint(57.670892, 11.843642); // outside triangle

        // benchmark test. 100,000 calculations for 0.336 seconds
//        long startTime = System.nanoTime();
//        for (int i = 0; i < 100000; i++) {
//            assertTrue(GeoUtility.isPointInTriangle(v1, v2, v3, p1));
//        }
//        System.out.println((System.nanoTime() - startTime) / 1000000000.0);

        // test for different orders of vertices, which should not affect the result
        assertTrue(GeoUtility.isPointInTriangle(v2, v1, v3, p1));
        assertTrue(GeoUtility.isPointInTriangle(v3, v1, v2, p1));

        assertFalse(GeoUtility.isPointInTriangle(v1, v2, v3, p2));

    }


    @Test
    public void testCheckCrossedGate() {
        GeoPoint mark1 = new GeoPoint(37.40937, -122.62233);
        GeoPoint mark2 = new GeoPoint(37.40938, -122.62154);
        GeoPoint location1 = new GeoPoint(37.40964, -122.62196);
        GeoPoint location2 = new GeoPoint(37.40910, -122.62189);
        GeoPoint location3 = new GeoPoint(37.40949, -122.62202);
        GeoPoint location4 = new GeoPoint(37.40927, -122.62152);

        // M1 -> M3 enters from CCW side
        assertTrue(GeoUtility.checkCrossedLine(mark1, mark2, location1, location2) == 2);
        // M1 -> M3 doesn't across
        assertFalse(GeoUtility.checkCrossedLine(mark1, mark2, location1, location3) > 0);
        // M2 -> M3 enters from CW side
        assertTrue(GeoUtility.checkCrossedLine(mark1, mark2, location2, location3) == 1);
        // order changes intersect direction
        assertTrue(GeoUtility.checkCrossedLine(mark2, mark1, location2, location3) == 2);
        assertTrue(GeoUtility.checkCrossedLine(mark1, mark2, location3, location2) == 2);
    }
}