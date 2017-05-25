package seng302.models.mark;

/**
 * An abstract class to represent general marks
 * Created by Haoming Yin (hyi25) on 17/3/17.
 */
public abstract class Mark {

    private String name;
    private MarkType markType;
    private double latitude;
    private double longitude;
    private long id;

    /**
     * Create a mark instance by passing its name and type
     *
     * @param name the name of the mark
     * @param markType the type of mark. either GATE_MARK or SINGLE_MARK.
     */
    public Mark(String name, MarkType markType, int id) {
        this.name = name;
        this.markType = markType;
        this.id = id;
    }

    public Mark(String name, MarkType markType, double latitude, double longitude) {
        this.name = name;
        this.markType = markType;
        this.latitude = latitude;
        this.longitude = longitude;
        id = 0;
    }

    /**
     * Calculated the heading in radians from first Mark to the second Mark.
     *
     * @param pointOne First Mark
     * @param pointTwo Second Mark
     * @return Heading in radians
     */
    public static Double calculateHeadingRad(Mark pointOne, Mark pointTwo) {
        Double longitude1 = pointOne.getLongitude();
        Double longitude2 = pointTwo.getLongitude();
        Double latitude1 = pointOne.getLatitude();
        Double latitude2 = pointTwo.getLatitude();
        return calculateHeadingRad(latitude1, longitude1, latitude2, longitude2);
    }

    /**
     * Calculate the heading in radians from geographical location with latitude1, longitude 1 to
     * geographical latitude2, longitude 2
     *
     * @param longitude1 Longitude of first point in degrees
     * @param longitude2 Longitude of second point in degrees
     * @param latitude1 Latitude of first point in degrees
     * @param latitude2 Latitude of first  point in degrees
     * @return Heading in radians
     */
    public static double calculateHeadingRad(Double latitude1, Double longitude1, Double latitude2,
        Double longitude2) {
        latitude1 = Math.toRadians(latitude1);
        latitude2 = Math.toRadians(latitude2);
        Double longDiff = Math.toRadians(longitude2 - longitude1);
        Double y = Math.sin(longDiff) * Math.cos(latitude2);
        Double x =
            Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2)
                * Math.cos(longDiff);
        return Math.atan2(y, x);
    }

    /**
     * Calculates the distance in meters from the first Mark to a second Mark
     *
     * @param pointOne First Mark
     * @param pointTwo Second Mark
     * @return Distance in meters
     */
    public static Double calculateDistance(Mark pointOne, Mark pointTwo) {
        Double longitude1 = pointOne.getLongitude();
        Double longitude2 = pointTwo.getLongitude();
        Double latitude1 = pointOne.getLatitude();
        Double latitude2 = pointTwo.getLatitude();
        return calculateDistance(latitude1, longitude1, latitude2, longitude2);
    }

    /**
     * Calculate the distance in meters from geographical location with latitude1, longitude 1 to
     * geographical latitude2, longitude 2
     *
     * @param longitude1 Longitude of first point in degrees
     * @param longitude2 Longitude of second point in degrees
     * @param latitude1 Latitude of first point in degrees
     * @param latitude2 Latitude of first  point in degrees
     * @return Distance in meters
     */
    public static Double calculateDistance(Double latitude1, Double longitude1, Double latitude2,
        Double longitude2) {
        Double theta = longitude1 - longitude2;
        Double dist = Math.sin(Math.toRadians(latitude1)) * Math.sin(Math.toRadians(latitude2)) +
            Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) *
                Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60
            * 1.1508; //nautical mile (distance between two degrees) * (degrees in a minute)
        dist = dist * 1609.344;    //ratio of miles to metres
        return dist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MarkType getMarkType() {
        return markType;
    }

    public void setMarkType(MarkType markType) {
        this.markType = markType;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
