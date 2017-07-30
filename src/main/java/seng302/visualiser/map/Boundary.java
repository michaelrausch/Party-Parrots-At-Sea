package seng302.visualiser.map;

/**
 * The Boundary class represents a rectangle territorial boundary on a map. It
 * contains four extremity double values(N, E, S, W). N and S are represented as
 * latitudes in radians. E and W are represented as longitudes in radians.
 *
 * Created by Haoming on 10/5/17
 */
class Boundary {

	private double northLat, eastLng, southLat, westLng;

	Boundary(double northLat, double eastLng, double southLat, double westLng) {
		this.northLat = northLat;
		this.eastLng = eastLng;
		this.southLat = southLat;
		this.westLng = westLng;
	}

	double getCentreLat() {
		return (northLat + southLat) / 2;
	}

	double getCentreLng() {
		return (eastLng + westLng) / 2;
	}

	double getNorthLat() {
		return northLat;
	}

	double getEastLng() {
		return eastLng;
	}

	double getSouthLat() {
		return southLat;
	}

	double getWestLng() {
		return westLng;
	}
}
