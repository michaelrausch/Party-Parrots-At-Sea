package seng302;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Event class containing the time of specific event, related team/boat, and
 * event location such as leg.
 */
public class Event {

	private long time;
	private Boat boat;
	private Leg leg;

	public Event(long eventTime, Boat eventBoat, Leg eventLeg) {
		this.time = eventTime;
		this.boat = eventBoat;
		this.leg = eventLeg;
	}

	/**
	 * Sets the time for the event
	 * @param eventTime the time for event in millisecond
	 */
	public void setTime(long eventTime) {
		this.time = eventTime;
	}

	/**
	 * Gets the time for the event
	 * @return the time for event in millisecond
	 */
	public long getTime() {
		return this.time;
	}

	/**
	 * Gets the time in a formatted string
	 * @return the string of time
	 */
	public String getTimeString() {
		return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(time));
	}

	/**
	 * Sets the involved boat
	 * @param eventBoat the involved boat
	 */
	public void setBoat(Boat eventBoat) {
		this.boat = eventBoat;
	}

	/**
	 * Gets the involved boat
	 * @return the boat involved in the event
	 */
	public Boat getBoat() {
		return this.boat;
	}

	/**
	 * Sets the involved location/leg
	 * @param eventLeg the involved leg
	 */
	public void setLeg(Leg eventLeg) {
		this.leg = eventLeg;
	}

	/**
	 * Gets the involved location/leg
	 * @return the leg involved in the event
	 */
	public Leg getLeg() {
		return this.leg;
	}
}
