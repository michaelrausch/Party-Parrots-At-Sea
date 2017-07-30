package seng302.model;

/**
 * A class represent Geo location (latitude, lnggitude).
 * Created by Haoming on 15/5/2017
 */
public class GeoPoint {

	private double lat, lng;

	public GeoPoint(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
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
