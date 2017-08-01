package seng302.gameServer.server.simulator;

import seng302.model.GeoPoint;
import seng302.utilities.GeoUtility;

public class Boat {

	private int sourceID;
	private double lat;
	private double lng;
	private double speed; // in mm/sec
	private String boatName, shortName, shorterName;
	private boolean isFinished;
	private long estimatedTimeTillFinish;

	private Corner lastPassedCorner, headingCorner;

	public Boat(int sourceID, String boatName) {
		this.sourceID = sourceID;
		this.boatName = boatName;
		this.isFinished = false;
		estimatedTimeTillFinish = 0;
	}

	/**
	 * Moves boat to the heading direction for a given time duration
	 * @param heading moving direction in degree.
	 * @param duration moving duration in millisecond.
	 */
	public void move(double heading, double duration) {
		Double distance = speed * duration / 1000000; // convert mm to meter
		GeoPoint originPos = new GeoPoint(lat, lng);
		GeoPoint newPos = GeoUtility.getGeoCoordinate(originPos, heading, distance);
		this.lat = newPos.getLat();
		this.lng = newPos.getLng();
	}

	public String toString() {
		return String.format("Boat (%d): lat: %f, lng: %f", sourceID, lat, lng);
	}

	public int getSourceID() {
		return sourceID;
	}

	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public String getBoatName() {
		return boatName;
	}

	public void setBoatName(String boatName) {
		this.boatName = boatName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShorterName() {
		return shorterName;
	}

	public void setShorterName(String shorterName) {
		this.shorterName = shorterName;
	}

	public Corner getLastPassedCorner() {
		return lastPassedCorner;
	}

	public void setLastPassedCorner(Corner lastPassedCorner) {
		this.lastPassedCorner = lastPassedCorner;
	}

	public Corner getHeadingCorner() {
		return headingCorner;
	}

	public void setHeadingCorner(Corner headingCorner) {
		this.headingCorner = headingCorner;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	public long getEstimatedTimeTillFinish(){
		return (long) (-getSpeed()) + System.currentTimeMillis();
	}
}
