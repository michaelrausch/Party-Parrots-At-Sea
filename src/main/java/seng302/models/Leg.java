package seng302.models;

import seng302.models.mark.SingleMark;

/**
* Represents the leg of a race.
*/
public class Leg {
    private int heading;
    private int distance;
    private boolean isFinishingLeg;
    private SingleMark startingSingleMark;

    /**
     * Create a new leg
     *
     * @param heading,  the magnetic heading of this leg
     * @param distance, the total distance of this leg in meters
     * @param singleMark,   the singleMark this leg starts on
     */
    public Leg(int heading, int distance, SingleMark singleMark) {
        this.heading = heading;
        this.distance = distance;
        this.startingSingleMark = singleMark;
        this.isFinishingLeg = false;
    }

    /**
     * Create a new leg
     *
     * @param heading,    the magnetic heading of this leg
     * @param distance,   the total distance of this leg in meters
     * @param markerName, the name of the marker this leg starts on
     */
    public Leg(int heading, int distance, String markerName) {
        this.heading = heading;
        this.distance = distance;
        this.startingSingleMark = new SingleMark(markerName);
        this.isFinishingLeg = false;
    }

    /**
     * Get the heading of this leg
     * @return int
     */
    public int getHeading() {
        return this.heading;
    }

    /**
     * Set the heading for this leg
     * @param heading
     */
    public void setHeading(int heading) {
        this.heading = heading;
    }

    /**
     * Get the total distance of this leg in meters
     * @return int
     */
    public int getDistance() {
        return this.distance;
    }

    /**
     * Set the distance of this leg in meters
     * @param distance
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * Returns the marker this leg started on
     * @return SingleMark
     */
    public SingleMark getMarker() {
        return this.startingSingleMark;
    }

    /**
     * Set the singleMark this leg starts on
     * @param singleMark
     */
    public void setMarker(SingleMark singleMark) {
        this.startingSingleMark = singleMark;
    }

    /**
     * Returns the name of the marker this leg started on
     * @return String
     */
    public String getMarkerLabel() {
        return this.startingSingleMark.getName();
    }



    /**
     * Specify whether or not the race finishes on this leg
     *
     * @param isFinishingLeg whether or not the race finishes on this leg
     */
    public void setFinishingLeg(boolean isFinishingLeg) {
        this.isFinishingLeg = isFinishingLeg;
    }

    /**
     * Returns whether or not the race finishes after this leg
     * @return true if this the race finishes after this leg
     */
    public boolean getIsFinishingLeg() {
        return this.isFinishingLeg;
    }
}