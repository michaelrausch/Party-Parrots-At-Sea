package seng302;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Event class containing the time of specific event, related team/boat, and
 * event location such as leg.
 *
 * @param eventTime, what time the event happens
 * @param eventBoat, the boat that the event belongs to
 * @param eventLeg, the leg the event happens on
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

	/**
	* Call when the boat reaches the marker, this will tell the marker the order
	* in which boats pass it
	*/
	public void addBoatToMarker(){
		this.leg.addBoatToMarker(boat);
	}

	/**
	* Get a string that contains the timestamp and course information for this event
	* @return A string that contains the timestamp and course information for this event
	*/
	public String getEventString(){
		String currentHeading = Integer.toString(this.getLeg().getHeading());
		String velocityKnots = String.format("%1.2f", this.getBoat().getVelocity() * 1.943844492); // Convert meters/second to knots

		return (this.getTimeString() + ", " + this.getBoat().getTeamName() + " passed " + this.getLeg().getMarkerLabel() + " going heading " + currentHeading + " at " + velocityKnots + " knots.");
	}
}
