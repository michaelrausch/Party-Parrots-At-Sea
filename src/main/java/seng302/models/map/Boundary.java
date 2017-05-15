package seng302.models.map;

/**
 * The Boundary class represents a square territorial bound on a map. It contains
 * four extremity double values(N, E, S, W). N and S are represented as latitudes
 * in radians. E and W are represented as longitudes in radians.
 *
 * Created by Haoming on 10/5/17
 */
public class Boundary {

	private double northLat, eastLng, southLat, westLng;

	public Boundary(double northLat, double eastLng, double southLat, double westLng) {
		this.northLat = northLat;
		this.eastLng = eastLng;
		this.southLat = southLat;
		this.westLng = westLng;
	}

	public double getCentreLat() {
		return (northLat + southLat) / 2;
	}

	public double getCentreLng() {
		return (eastLng + westLng) / 2;
	}

	public double getNorthLat() {
		return northLat;
	}

	public double getEastLng() {
		return eastLng;
	}

	public double getSouthLat() {
		return southLat;
	}

	public double getWestLng() {
		return westLng;
	}
}
