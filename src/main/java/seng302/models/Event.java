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
    private Boat boat;
    private boolean isFinishingEvent = false; // This event occurs when a boat finishes the race
    private Mark mark1; // This mark
    private Mark mark2; // Next mark
    private int markPosInRace; // the position of the current mark in the race course


    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Boat eventBoat, Mark mark1, Mark mark2, int markPosInRace) {
        this.time = eventTime;
        this.boat = eventBoat;
        this.mark1 = mark1;
        this.mark2 = mark2;
        this.markPosInRace = markPosInRace;
    }

    /**
     * Event class containing the time of specific event, related team/boat, and
     * event location such as leg.
     *
     * @param eventTime, what time the event happens
     * @param eventBoat, the boat that the event belongs to
     */
    public Event(Double eventTime, Boat eventBoat, Mark mark1, int markPosInRace) {
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

    public Boat getBoat() {
        return this.boat;
    }

    public void setBoat(Boat eventBoat) {
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
            return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " finished the race");
        }
        System.out.println(this.getDistanceBetweenMarks());
        return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " passed " + this.mark1.getName() + " going heading " + this.getBoatHeading() + "°");
    }

    /**
     * @return the distance between the two marks
     */
    public double getDistanceBetweenMarks() {
        //return Math.sqrt(Math.pow(mark1.getLatitude()-mark2.getLatitude(), 2) + Math.pow(mark1.getLongitude()-mark2.getLongitude(), 2));
        double earth_radius = 6378.137;
        double dLat = this.mark2.getLatitude() * Math.PI / 180 - this.mark1.getLatitude() * Math.PI / 180;
        double dLon = this.mark2.getLongitude() * Math.PI / 180 - this.mark1.getLongitude() * Math.PI / 180;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(this.mark1.getLatitude() * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = earth_radius * c;

        return d * 1000;
    }

    /**
     * @return the boats heading
     */
    public double getBoatHeading() {
        double bearing = Math.atan2(mark2.getLatitude() - mark1.getLatitude(), mark2.getLongitude() - mark1.getLongitude());
        if (bearing < 0) {
            bearing += Math.PI * 2;
        }
        return bearing * 180 / Math.PI;
    }

    public Mark getThisMark() {
        return this.mark1;
    }

    public int getMarkPosInRace() {
        return markPosInRace;
    }
}