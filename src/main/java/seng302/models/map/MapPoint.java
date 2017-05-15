package seng302.models.map;

/**
 * A class represent euclidean planar point (x, y)
 * Created by Haoming on 15/5/2017
 */
class MapPoint {

	private double x, y;

	MapPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	double getX() {
		return x;
	}

	void setX(double x) {
		this.x = x;
	}

	double getY() {
		return y;
	}

	void setY(double y) {
		this.y = y;
	}
}
