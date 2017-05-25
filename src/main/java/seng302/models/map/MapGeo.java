package seng302.models.map;

/**
 * A class represent Geo location (latitude, longitude).
 * Created by Haoming on 15/5/2017
 */
class MapGeo {

	private double lat, lng;

	MapGeo(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	double getLat() {
		return lat;
	}

	void setLat(double lat) {
		this.lat = lat;
	}

	double getLng() {
		return lng;
	}

	void setLng(double lng) {
		this.lng = lng;
	}
}