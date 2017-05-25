package seng302;

import javafx.geometry.Point2D;

/**
 * A Class for performing geometric calculations on the canvas
 * Created by wmu16 on 24/05/17.
 */
public final class GeometryUtils {


    /**
     * Performs the line function on two points of a line and a test point to test which side of the line that point is
     * on. If the return value is
     * return  1, then the point is on one side of the line,
     * return -1 then the point is on the other side of the line
     * return  0 then the point is exactly on the line.
     * @param linePoint1 One point of the line
     * @param linePoint2 Second point of the line
     * @param testPoint The point to test with this line
     * @return A return value indicating which side of the line the point is on
     */
    public static Integer lineFunction(Point2D linePoint1, Point2D linePoint2, Point2D testPoint) {

        Double x = testPoint.getX();
        Double y = testPoint.getY();
        Double x1 = linePoint1.getX();
        Double y1 = linePoint1.getY();
        Double x2 = linePoint2.getX();
        Double y2 = linePoint2.getY();

        Double result =  (x - x1)*(y2 - y1) - (y - y1)*(x2 - x1);     //Line function

        if (result > 0) {
            return 1;
        }
        else if (result < 0) {
            return -1;
        }
        else {
            return 0;
        }
    }


    /**
     * Given a point and a vector (angle and vector length) Will create a new point, that vector away from the origin
     * point
     * @param originPoint The point with which to use as the base for our vector addition
     * @param angleInDeg (DEGREES) The angle at which our new point is being created (in degrees!)
     * @param vectorLength The length out on this angle from the origin point to create the new point
     * @return a Point2D
     */
    public static Point2D makeArbitraryVectorPoint(Point2D originPoint, Double angleInDeg, Double vectorLength) {

        Double endPointX = originPoint.getX() + vectorLength * Math.cos(Math.toRadians(angleInDeg));
        Double endPointY = originPoint.getY() + vectorLength * Math.sin(Math.toRadians(angleInDeg));

        return new Point2D(endPointX, endPointY);

    }

}
