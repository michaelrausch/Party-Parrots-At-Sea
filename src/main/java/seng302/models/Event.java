package seng302.models;

import seng302.models.mark.Mark;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
* Event class containing the time of specific event, related team/boat, and
* event location such as leg.
*/
public class Event {
    private Double time; // Time the event occurs
    private Yacht boat;
    private boolean isFinishingEvent = false; // This event occurs when a boat finishes the race
    private Mark mark1; // This mark
    private Mark mark2; // Next mark
    private int markPosInRace; // the position of the current mark in the race course
    private double heading;
    private final double ORIGIN_LAT = 32.320504;
    private final double ORIGIN_LON = -64.857063;
    private final double SCALE = 16000;

    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Yacht eventBoat, Mark mark1, Mark mark2, int markPosInRace) {
        this.time = eventTime;
        this.boat = eventBoat;
        this.mark1 = mark1;
        this.mark2 = mark2;
        this.markPosInRace = markPosInRace;
        this.heading = angleFromCoordinate(mark1, mark2);

    }

    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Yacht eventBoat, Mark mark1, int markPosInRace) {
        this.time = eventTime;
        this.boat = eventBoat;
        this.mark1 = mark1;
        this.markPosInRace = markPosInRace;
        this.isFinishingEvent = true;
    }

    public double getTime() {
        return this.time;
    }

    public void setTime(double eventTime) {
        this.time = eventTime;
    }

    /**
     * Gets the time in a formatted string
     *
     * @return the string of time
     */
    public String getTimeString() {
        return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(time.longValue()));
    }

    public Yacht getBoat() {
        return this.boat;
    }

    public void setBoat(Yacht eventBoat) {
        this.boat = eventBoat;
    }

    public boolean getIsFinishingEvent() {
        return this.isFinishingEvent;
    }

    /**
     * Get a string that contains the timestamp and course information for this event
     *
     * @return A string that details what happened in this event
     */
    public String getEventString() {
        // This event is a boat finishing the race
        if (this.isFinishingEvent) {
            return (this.getTimeString() + ", " + this.getBoat().getBoatName() + " finished the race");
        }
        return (this.getTimeString() + ", " + this.getBoat().getBoatName() + " passed " + this.mark1.getName() + " going heading " + this.getBoatHeading() + "°");
    }

    /**
     * @return the distance between the two marks
     */
    public double getDistanceBetweenMarks() {
        double earth_radius = 6378.137;
        double dLat = this.mark2.getLatitude() * Math.PI / 180 - this.mark1.getLatitude() * Math.PI / 180;
        double dLon = this.mark2.getLongitude() * Math.PI / 180 - this.mark1.getLongitude() * Math.PI / 180;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(this.mark1.getLatitude() * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = earth_radius * c;

        return d * 1000;
    }

    /**
     * Calculates current boat heading direction.
     * @return the boats heading as degree. vertical upward is 0 degree, and degree goes up clockwise.
     */
    public double getBoatHeading() {
        if (mark2 == null){
            return 0.0;
        }

        double x1 = (mark1.getLongitude() - ORIGIN_LON) * SCALE;
        double y1 = (ORIGIN_LAT - mark1.getLatitude()) * SCALE;
        double x2 = (mark2.getLongitude() - ORIGIN_LON) * SCALE;
        double y2 = (ORIGIN_LAT - mark2.getLatitude()) * SCALE;

        double headingRadians = Math.atan2(y2-y1, x2-x1);

        if (headingRadians < 0){
            headingRadians += 2 * Math.PI;
        }

        // Convert back to degrees, and flip 180 degrees
//        return ((headingRadians) * 180) / Math.PI;
        return (Math.toDegrees(headingRadians) + 90) % 360;

    }

    /**
     * Calculates the angle between to angular co-ordinates on a sphere.
     *
     * @param geoPointOne first geographical location
     * @param geoPointTwo second geographical location
     * @return the angle from point one to point two
     */
    private Double angleFromCoordinate(Mark geoPointOne, Mark geoPointTwo) {
        if (geoPointTwo == null)
            return null;

        double x1 = geoPointOne.getLatitude();
        double y1 = -geoPointOne.getLongitude();
        double x2 = geoPointTwo.getLatitude();
        double y2 = -geoPointTwo.getLongitude();

        return Math.toDegrees(Math.atan2(x2-x1, y2-y1));

    }

    public double getHeading() {
        return heading;
    }

    public Mark getThisMark() {
        return this.mark1;
    }

    public int getMarkPosInRace() {
        return markPosInRace;
    }
}