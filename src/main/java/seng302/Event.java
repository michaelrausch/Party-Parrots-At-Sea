package seng302;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {

    private long time; // Time the event occurs
    private Boat boat; 
    private Leg leg; // Leg of the race the event occurs on
    private boolean isFinishingEvent = false; // This event occurs when a boat finishes the race

    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     * @param eventLeg,  the leg the event happens on
     */
    public Event(long eventTime, Boat eventBoat, Leg eventLeg) {
        this.time = eventTime;
        this.boat = eventBoat;
        this.leg = eventLeg;
    }

    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     * @param eventLeg,  the leg the event happens on
     * @param isFinishingEvent,  true if this event is the boat crossing the finishing line
     */
    public Event(long eventTime, Boat eventBoat, Leg eventLeg, boolean isFinishingEvent) {
        this.time = eventTime;
        this.boat = eventBoat;
        this.leg = eventLeg;
        this.isFinishingEvent = isFinishingEvent;
    }

    /**
     * Gets the time for the event
     *
     * @return the time for event in millisecond
     */
    public long getTime() {
        return this.time;
    }

    /**
     * Sets the time for the event
     *
     * @param eventTime the time for event in millisecond
     */
    public void setTime(long eventTime) {
        this.time = eventTime;
    }

    /**
     * Gets the time in a formatted string
     *
     * @return the string of time
     */
    public String getTimeString() {
        return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(time));
    }

    /**
     * Gets the involved boat
     *
     * @return the boat involved in the event
     */
    public Boat getBoat() {
        return this.boat;
    }

    /**
     * Sets the involved boat
     *
     * @param eventBoat the involved boat
     */
    public void setBoat(Boat eventBoat) {
        this.boat = eventBoat;
    }

    /**
     * Gets the involved location/leg
     *
     * @return the leg involved in the event
     */
    public Leg getLeg() {
        return this.leg;
    }

    /**
     * Sets the involved location/leg
     *
     * @param eventLeg the involved leg
     */
    public void setLeg(Leg eventLeg) {
        this.leg = eventLeg;
    }

    /**
     * Called when the boat in this event passes
     * the marker.
     */
    public void boatPassedMarker() {
        this.leg.addBoatToMarker(boat);
    }

    /**
     * Returns true if this event is the boat finishing the race
     */
    public boolean getIsFinishingEvent() {
        return this.isFinishingEvent;
    }

    /**
     * Get a string that contains the timestamp and course information for this event
     *
     * @return A string that details what happened in this event
     */
    public String getEventString() {
        String currentHeading = Integer.toString(this.getLeg().getHeading());

        // This event is a boat finishing the race
        if (this.isFinishingEvent) {
            return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " finished the race");
        }

        return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " passed " + this.getLeg().getMarkerLabel() + " going heading " + currentHeading + "°");
    }
}
