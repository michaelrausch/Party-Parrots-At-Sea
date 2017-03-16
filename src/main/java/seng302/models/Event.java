package seng302.models;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
* Event class containing the time of specific event, related team/boat, and
* event location such as leg.
*/
public class Event {
    private Double time; // Time the event occurs
    private Boat boat; 
    private boolean isFinishingEvent = false; // This event occurs when a boat finishes the race
    private Mark mark1; // This mark
    private Mark mark2; // Next Mark


    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Boat eventBoat, Mark mark1, Mark mark2) {
        this.time = eventTime;
        this.boat = eventBoat;
        //this.leg = eventLeg;
        this.mark1 = mark1;
        this.mark2 = mark2;
    }

    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Boat eventBoat, Mark mark1) {
        this.time = eventTime;
        this.boat = eventBoat;
        this.mark1 = mark1;
        this.isFinishingEvent = true;
    }

    /**
     * Gets the time for the event
     *
     * @return the time for event in millisecond
     */
    public double getTime() {
        return this.time;
    }

    /**
     * Sets the time for the event
     *
     * @param eventTime the time for event in millisecond
     */
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
     * Called when the boat in this event passes
     * the marker.
     */
    public void boatPassedMarker() {
        this.mark1.addBoat(boat);
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
        // This event is a boat finishing the race
        if (this.isFinishingEvent) {
            return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " finished the race");
        }

        return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " passed " + this.mark1.getName() + " going heading " + this.getBoatHeading() + "°");
    }

    /**
     * @return the distance between the two marks
     */
    public double getDistanceBetweenMarks(){
        return Math.sqrt(Math.pow(mark1.getLatitude()-mark2.getLatitude(), 2) + Math.pow(mark1.getLongitude()-mark2.getLongitude(), 2));
    }

    /**
     * @return the boats heading
     */
    public double getBoatHeading(){
        double bearing = Math.atan2(mark2.getLatitude() - mark1.getLatitude(), mark2.getLongitude() - mark1.getLongitude());
        if (bearing < 0) {
            bearing += Math.PI * 2;
        }
        return bearing * 180 / Math.PI;
    }

    /**
     * Get the mark the event happened on
     * @return the mark
     */
    public Mark getMark(){
        return this.mark1;
    }

    /**
     * Get the next mark
     * @return the next mark
     */
    public Mark getNextMark(){
        return this.mark2;
    }
}
