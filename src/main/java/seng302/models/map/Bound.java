package seng302.models.map;

/**
 * The Bound class is to represent square territorial bounds on a map. It contains
 * four extremity double values(N, E, S, W). N and S are represented as latitudes
 * in radians. E and W are represented as longitudes in radians.
 *
 * Created by Haoming on 10/5/17
 */
public class Bound {

	private double north, east, south, west;

	public Bound(double north, double east, double south, double west) {
		this.north = north;
		this.east = east;
		this.south = south;
		this.west = west;
	}

	public double getCentreLat() {
		return (north + south) / 2;
	}

	public double getCentreLng() {
		return (east + west) / 2;
	}

	public double getNorth() {
		return north;
	}

	public double getEast() {
		return east;
	}

	public double getSouth() {
		return south;
	}

	public double getWest() {
		return west;
	}
}
