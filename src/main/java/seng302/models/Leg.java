package seng302.models;


import seng302.models.mark.Mark;

/**
 * Class for defining the leg of a race between two markers
 * Created by cir27 on 3/03/17.
 */
public class Leg {

    private final double ORIGIN_LAT = 32.320504;
    private final double ORIGIN_LON = -64.857063;
    private final double SCALE = 16000;

    private Double distance;
    private Double heading;
    private Mark end;
    private Mark start;

    Leg(Mark start, Mark end) {
        this.distance     = calculateMarkerDistance(start, end);
        this.heading      = angleFromCoordinate(start, end);
        this.start        = start;
        this.end          = end;
    }

    /**
     * Calculates the euclidian distance between two markers on the canvas using xy coordinates
     *
     * @param geoPointOne first geographical point
     * @param geoPointTwo second geographical point
     * @return the distance between two points in meters
     */
    private Double calculateMarkerDistance(Mark geoPointOne, Mark geoPointTwo) {

            double earth_radius = 6378.137;
            double dLat = geoPointTwo.getLatitude() * Math.PI / 180 - geoPointOne.getLatitude() * Math.PI / 180;
            double dLon = geoPointTwo.getLongitude() * Math.PI / 180 - geoPointOne.getLongitude() * Math.PI / 180;

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(geoPointOne.getLatitude() * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = earth_radius * c;

            return d * 1000;
    }

    /**
     * Calculates the angle between to angular co-ordinates on a sphere.
     *
     * @param geoPointOne first geographical location
     * @param geoPointTwo second geographical location
     * @return the angle from point one to point two
     */
    private Double angleFromCoordinate(Mark geoPointOne, Mark geoPointTwo) {

            if (geoPointTwo == null){
                return 0.0;
            }

            double x1 = (geoPointOne.getLongitude() - ORIGIN_LON) * SCALE;
            double y1 = (ORIGIN_LAT - geoPointOne.getLatitude()) * SCALE;
            double x2 = (geoPointTwo.getLongitude() - ORIGIN_LON) * SCALE;
            double y2 = (ORIGIN_LAT - geoPointTwo.getLatitude()) * SCALE;

            double headingRadians = Math.atan2(y2-y1, x2-x1);

            if (headingRadians < 0){
                headingRadians += 2 * Math.PI;
            }

            // Convert back to degrees, and flip 180 degrees
//        return ((headingRadians) * 180) / Math.PI;
            return (Math.toDegrees(headingRadians) + 90) % 360;

    }

    Double getDistance()
    {
        return this.distance;
    }

    Mark getEnd()
    {
        return this.end;
    }

    public Mark getStart() {
        return start;
    }

    public Double getHeading()
    {
        return this.heading;
    }
}
