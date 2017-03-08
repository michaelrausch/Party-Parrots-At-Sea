package seng302;

/**
* Represents the leg of a race.
*/
public class Leg {
    private int heading;
    private int distance;
    private boolean isFinishingLeg;
    private Marker startingMarker;

    /**
     * Create a new leg
     *
     * @param heading,  the magnetic heading of this leg
     * @param distance, the total distance of this leg in meters
     * @param marker,   the marker this leg starts on
     */
    public Leg(int heading, int distance, Marker marker) {
        this.heading = heading;
        this.distance = distance;
        this.startingMarker = marker;
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
        this.startingMarker = new Marker(markerName);
        this.isFinishingLeg = false;
    }

    /**
     * Get the heading of this leg
     */
    public int getHeading() {
        return this.heading;
    }

    /**
     * Set the heading for this leg
     */
    public void setHeading(int heading) {
        this.heading = heading;
    }

    /**
     * Get the total distance of this leg in meters
     */
    public int getDistance() {
        return this.distance;
    }

    /**
     * Set the distance of this leg in meters
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * Returns the marker this leg started on
     */
    public Marker getMarker() {
        return this.startingMarker;
    }

    /**
     * Set the marker this leg starts on
     */
    public void setMarker(Marker marker) {
        this.startingMarker = marker;
    }

    /**
     * Returns the name of the marker this leg started on
     */
    public String getMarkerLabel() {
        return this.startingMarker.getName();
    }

    /**
     * Tell the marker that the boat has passed it
     */
    public void addBoatToMarker(Boat boat) {
        this.startingMarker.addBoat(boat);
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