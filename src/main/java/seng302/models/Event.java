package seng302.models;

import seng302.models.mark.SingleMark;

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
    private SingleMark singleMark1; // This mark
    private SingleMark singleMark2; // Next SingleMark


    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Boat eventBoat, SingleMark singleMark1, SingleMark singleMark2) {
        this.time = eventTime;
        this.boat = eventBoat;
        //this.leg = eventLeg;
        this.singleMark1 = singleMark1;
        this.singleMark2 = singleMark2;
    }

    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Boat eventBoat, SingleMark singleMark1) {
        this.time = eventTime;
        this.boat = eventBoat;
        this.singleMark1 = singleMark1;
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
        System.out.println(this.getDistanceBetweenMarks());
        return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " passed " + this.singleMark1.getName() + " going heading " + this.getBoatHeading() + "°");
    }

    /**
     * @return the distance between the two marks
     */
    public double getDistanceBetweenMarks(){
        //return Math.sqrt(Math.pow(singleMark1.getLatitude()-singleMark2.getLatitude(), 2) + Math.pow(singleMark1.getLongitude()-singleMark2.getLongitude(), 2));
        double earth_radius = 6378.137;
        double dLat = this.singleMark2.getLatitude() * Math.PI / 180 - this.singleMark1.getLatitude() * Math.PI / 180;
        double dLon = this.singleMark2.getLongitude() * Math.PI / 180 - this.singleMark1.getLongitude() * Math.PI / 180;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(this.singleMark1.getLatitude() * Math.PI / 180) * Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = earth_radius * c;

        return d * 1000;
    }

    /**
     * @return the boats heading
     */
    public double getBoatHeading(){
        double bearing = Math.atan2(singleMark2.getLatitude() - singleMark1.getLatitude(), singleMark2.getLongitude() - singleMark1.getLongitude());
        if (bearing < 0) {
            bearing += Math.PI * 2;
        }
        return bearing * 180 / Math.PI;
    }

    /**
     * Get the mark the event happened on
     * @return the mark
     */
    public SingleMark getMark(){
        return this.singleMark1;
    }

    /**
     * Get the next mark
     * @return the next mark
     */
    public SingleMark getNextMark(){
        return this.singleMark2;
    }
}
