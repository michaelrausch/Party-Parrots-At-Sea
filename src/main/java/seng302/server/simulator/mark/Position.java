package seng302.server.simulator.mark;

public class Position {

	double lat, lng;

	public Position(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	public String toString() {
		return String.format("Position at lat:%f lng:%f.", lat, lng);
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
}
