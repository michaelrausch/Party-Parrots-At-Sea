package seng302;

import javafx.geometry.Point2D;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test Class for the GeometryUtils class
 * Created by wmu16 on 24/05/17.
 */
public class TestGeoUtils {

    //Line in x = y
    private Point2D linePoint1 = new Point2D(0, 0);
    private Point2D linePoint2 = new Point2D(1, 1);

    //Point below x = y
    private Point2D arbitraryPoint1 = new Point2D(1, 0);

    //Point above x = y
    private Point2D arbitraryPoint2 = new Point2D(0, 1);

    //Point on x = y
    private Point2D arbitraryPoint3 = new Point2D(2, 2);

    @Before
    public void setUp() throws Exception {

    }


    @Test
    public void testLineFunction() {

        Integer lineFunctionResult1 = GeometryUtils.lineFunction(linePoint1, linePoint2, arbitraryPoint1);
        Integer lineFunctionResult2 = GeometryUtils.lineFunction(linePoint1, linePoint2, arbitraryPoint2);
        Integer lineFunctionResult3 = GeometryUtils.lineFunction(linePoint1, linePoint2, arbitraryPoint3);

        //Point1 and Point2 are on opposite sides
        assertEquals(Math.abs(lineFunctionResult1), Math.abs(lineFunctionResult2));
        assertNotEquals(lineFunctionResult1, lineFunctionResult2);

        //Point3 is on the line
        assertEquals((long) lineFunctionResult3, 0L);
    }

    @Test
    public void testMakeArbitraryVectorPoint() {

        //Make a point (1,0) from point (0,0)
        Point2D newPoint = GeometryUtils.makeArbitraryVectorPoint(linePoint1, 0d, 1d);
        Point2D expected = new Point2D(1,0);

        assertEquals(expected.getX(), newPoint.getX(), 1E-6);
        assertEquals(expected.getY(), newPoint.getY(), 1E-6);

        newPoint = GeometryUtils.makeArbitraryVectorPoint(linePoint1, 90d, 1d);
        expected = new Point2D(0, 1);

        assertEquals(expected.getX(), newPoint.getX(), 1E-6);
        assertEquals(expected.getY(), newPoint.getY(), 1E-6);
    }
}
