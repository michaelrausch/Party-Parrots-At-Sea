package seng302.utilities;

import javafx.geometry.Point2D;
import seng302.model.GeoPoint;

public class GeoUtility {

    private static double EARTH_RADIUS = 6378.137;
    private static Double MS_TO_KNOTS = 1.943844492;

    /**
     * Calculates the euclidean distance between two markers on the canvas using xy coordinates
     *
     * @param p1 first geographical position
     * @param p2 second geographical position
     * @return the distance in meter between two points in meters
     */
    public static Double getDistance(GeoPoint p1, GeoPoint p2) {

        double dLat = Math.toRadians(p2.getLat() - p1.getLat());
        double dLon = Math.toRadians(p2.getLng() - p1.getLng());

        double a = Math.pow(Math.sin(dLat / 2), 2.0)
            + Math.cos(Math.toRadians(p1.getLat())) * Math.cos(Math.toRadians(p2.getLat()))
            * Math.pow(Math.sin(dLon / 2), 2.0);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = EARTH_RADIUS * c;

        return d * 1000; // distance from km to meter
    }

    /**
     * Calculates the angle between to angular co-ordinates on a sphere.
     *
     * @param p1 the first geographical position, start point
     * @param p2 the second geographical position, end point
     * @return the initial bearing in degree from p1 to p2, value range (0 ~ 360 deg.). vertical up
     * is 0 deg. horizontal right is 90 deg.
     *
     * NOTE: The final bearing will differ from the initial bearing by varying degrees according to
     * distance and latitude (if you were to go from say 35°N,45°E (≈ Baghdad) to 35°N,135°E (≈
     * Osaka), you would start on a heading of 60° and end up on a heading of 120°
     */
    public static Double getBearing(GeoPoint p1, GeoPoint p2) {
        return (Math.toDegrees(getBearingRad(p1, p2)) + 360.0) % 360.0;
    }


    /**
     * WARNING: this function DOES NOT account for wrapping around on lats / longs etc.
     * SO BE CAREFUL IN USING THIS FUNCTION
     *
     * @param p1 GeoPoint 1
     * @param p2 GeoPoint 2
     * @return GeoPoint midPoint
     */
    public static GeoPoint getDirtyMidPoint(GeoPoint p1, GeoPoint p2) {
        return new GeoPoint((p1.getLat() + p2.getLat()) / 2, (p1.getLng() + p2.getLng()) / 2);
    }

    /**
     * Calculates the angle between to angular co-ordinates on a sphere in radians.
     *
     * @param p1 the first geographical position, start point
     * @param p2 the second geographical position, end point
     * @return the initial bearing in degree from p1 to p2, value range (0 ~ 360 deg.). vertical up
     * is 0 deg. horizontal right is 90 deg.
     *
     * NOTE: The final bearing will differ from the initial bearing by varying degrees according to
     * distance and latitude (if you were to go from say 35°N,45°E (≈ Baghdad) to 35°N,135°E (≈
     * Osaka), you would start on a heading of 60° and end up on a heading of 120°
     */
    public static Double getBearingRad(GeoPoint p1, GeoPoint p2) {
        double dLon = Math.toRadians(p2.getLng() - p1.getLng());

        double y = Math.sin(dLon) * Math.cos(Math.toRadians(p2.getLat()));
        double x = Math.cos(Math.toRadians(p1.getLat())) * Math.sin(Math.toRadians(p2.getLat()))
            - Math.sin(Math.toRadians(p1.getLat())) * Math.cos(Math.toRadians(p2.getLat())) * Math
            .cos(dLon);

        return Math.atan2(y, x);
    }

    /**
     * Given an existing point in lat/lng, distance in (in meter) and bearing (in degrees),
     * calculates the new lat/lng.
     *
     * @param origin the original position within lat / lng
     * @param bearing the bearing in degree, from original position to the new position
     * @param distance the distance in meter, from original position to the new position
     * @return the new position
     */
    public static GeoPoint getGeoCoordinate(GeoPoint origin, Double bearing, Double distance) {
        double b = Math.toRadians(bearing); // bearing to radians
        double d = distance / 1000.0; // distance to km

        double originLat = Math.toRadians(origin.getLat());
        double originLng = Math.toRadians(origin.getLng());

        double endLat = Math.asin(Math.sin(originLat) * Math.cos(d / EARTH_RADIUS)
            + Math.cos(originLat) * Math.sin(d / EARTH_RADIUS) * Math.cos(b));
        double endLng = originLng
            + Math.atan2(Math.sin(b) * Math.sin(d / EARTH_RADIUS) * Math.cos(originLat),
            Math.cos(d / EARTH_RADIUS) - Math.sin(originLat) * Math.sin(endLat));

        return new GeoPoint(Math.toDegrees(endLat), Math.toDegrees(endLng));
    }

    /**
     * Performs the line function on two points of a line and a test point to test which side of the
     * line that point is on. If the return value is return  1, then the point is on one side of the
     * line, return -1 then the point is on the other side of the line return  0 then the point is
     * exactly on the line.
     *
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

        Double result = (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);     //Line function

        if (result > 0) {
            return 1;
        } else if (result < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Checks if the line formed by lastLocation and location doesn't intersect the line segment
     * formed by mark1 and mark2 See the wiki Mark Rounding algorithm for more info
     *
     * @param mark1 One mark of the line
     * @param mark2 The second mark of the line
     * @param lastLocation The last location of the point crossing this line
     * @param location The current location of the point crossing this line
     * @return 0 if two line segment doesn't intersect, otherwise 1 if they intersect and
     * lastLocation is on RHS of the line segment (mark1 to mark2) or 2 if lastLocation on LHS of
     * the line segment (mark1 to mark2)
     */
    public static Integer checkCrossedLine(GeoPoint mark1, GeoPoint mark2, GeoPoint lastLocation,
        GeoPoint location) {
        boolean enteredDirection = isClockwise(mark1, mark2, lastLocation);
        boolean exitedDirection = isClockwise(mark1, mark2, location);
        if (enteredDirection != exitedDirection) {
            if (!isPointInTriangle(mark1, lastLocation, location, mark2)
                && !isPointInTriangle(mark2, lastLocation, location, mark1)) {

                return enteredDirection ? 1 : 2;
            }
        }
        return 0;
    }

    /**
     * Given a point and a vector (angle and vector length) Will create a new point, that vector
     * away from the origin point
     *
     * @param originPoint The point with which to use as the base for our vector addition
     * @param angleInDeg (DEGREES) The angle at which our new point is being created (in degrees!)
     * @param vectorLength The length out on this angle from the origin point to create the new
     * point
     * @return a Point2D
     */
    public static Point2D makeArbitraryVectorPoint(Point2D originPoint, Double angleInDeg,
        Double vectorLength) {

        Double endPointX = originPoint.getX() + vectorLength * Math.cos(Math.toRadians(angleInDeg));
        Double endPointY = originPoint.getY() + vectorLength * Math.sin(Math.toRadians(angleInDeg));

        return new Point2D(endPointX, endPointY);

    }

    /**
     * Define vector v1 = p1 - p0 to v2 = p2- p0. This function returns the difference of bearing
     * from v1 to v2. For example, if bearing of v1 is 30 deg and bearing of v2 is 90 deg, then the
     * difference is 60 deg.
     *
     * @param bearing1 the bearing of v1
     * @param bearing2 the bearing of v2
     * @return the difference of bearing from v1 to v2
     */
    private static Double getBearingDiff(double bearing1, double bearing2) {
        return ((360 - bearing1) + bearing2) % 360;
    }

    /**
     * Check if a geo point ins on the right hand side of the line segment, which
     * formed by two geo points v1 to v2. (Algorithm: point is clockwise to the
     * line if the bearing difference is less than 180 deg.)
     *
     * @param v1 one end of the line segment
     * @param v2 another end of the line segment
     * @param point the point to be tested
     * @return true if the point is on the RHS of the line
     */
    public static Boolean isClockwise(GeoPoint v1, GeoPoint v2, GeoPoint point) {
        return getBearingDiff(getBearing(v1, v2), getBearing(v1, point)) < 180;
    }

    /**
     * Given three geo points to form a triangle, the method returns true if the fourth point is
     * inside the triangle
     *
     * @param v1 the vertex of the triangle
     * @param v2 the vertex of the triangle
     * @param v3 the vertex of the triangle
     * @param point the point to be tested
     * @return true if the fourth point is inside the triangle
     */
    public static Boolean isPointInTriangle(GeoPoint v1, GeoPoint v2, GeoPoint v3, GeoPoint point) {
        // true, if diff of bearing from (v1 to v2) to (v1 to p) is less than 180 deg
        boolean isCW = isClockwise(v1, v2, point);

        if (isClockwise(v2, v3, point) != isCW) {
            return false;
        }

        if (isClockwise(v3, v1, point) != isCW) {
            return false;
        }

        return true;
    }

    /**
     * @param boatSpeedInKnots Speed in knots
     * @return The Boat speed in millimeters per second
     */
    public static Double knotsToMMS(Double boatSpeedInKnots) {
        return boatSpeedInKnots / MS_TO_KNOTS * 1000;
    }

    /**
     * @param boatSpeedInMMS Speed in millimeters per second
     * @return The Boat speed in knots
     */
    public static Double mmsToKnots(Double boatSpeedInMMS) {
        return boatSpeedInMMS / 1000 * MS_TO_KNOTS;
    }
}
