package seng302.utilities;

public class GeoUtility {

	private static double EARTH_RADIUS = 6378.137;

	/**
	 * Calculates the euclidean distance between two markers on the canvas using xy coordinates
	 *
	 * @param p1 first geographical position
	 * @param p2 second geographical position
	 * @return the distance in meter between two points in meters
	 */
	public static Double getDistance(GeoPoint p1, GeoPoint p2) {

		double dLat = Math.toRadians(p2.getLat() - p1.getLat());
		double dLon = Math.toRadians(p2.getLng() - p1.getLng());

		double a = Math.pow(Math.sin(dLat / 2), 2.0)
				+ Math.cos(Math.toRadians(p1.getLat())) * Math.cos(Math.toRadians(p2.getLat()))
				* Math.pow(Math.sin(dLon / 2), 2.0);

		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = EARTH_RADIUS * c;

		return d * 1000; // distance from km to meter
	}

	/**
	 * Calculates the angle between to angular co-ordinates on a sphere.
	 *
	 * @param p1 the first geographical position, start point
	 * @param p2 the second geographical position, end point
	 * @return the initial bearing in degree from p1 to p2, value range (0 ~ 360 deg.).
	 * vertical up is 0 deg. horizontal right is 90 deg.
	 *
	 * NOTE:
	 * The final bearing will differ from the initial bearing by varying degrees
	 * according to distance and latitude (if you were to go from say 35°N,45°E
	 * (≈ Baghdad) to 35°N,135°E (≈ Osaka), you would start on a heading of 60°
	 * and end up on a heading of 120°
	 */
	public static Double getBearing(GeoPoint p1, GeoPoint p2) {

		double dLon = Math.toRadians(p2.getLng() - p1.getLng());

		double y = Math.sin(dLon) * Math.cos(Math.toRadians(p2.getLat()));
		double x = Math.cos(Math.toRadians(p1.getLat())) * Math.sin(Math.toRadians(p2.getLat()))
				- Math.sin(Math.toRadians(p1.getLat())) * Math.cos(Math.toRadians(p2.getLat())) * Math.cos(dLon);

		double bearing = Math.toDegrees(Math.atan2(y, x));

		return (bearing + 360.0) % 360.0;
	}

	/**
	 * Given an existing point in lat/lng, distance in (in meter) and bearing
	 * (in degrees), calculates the new lat/lng.
	 *
	 * @param origin   the original position within lat / lng
	 * @param bearing  the bearing in degree, from original position to the new position
	 * @param distance the distance in meter, from original position to the new position
	 * @return the new position
	 */
	public static GeoPoint getGeoCoordinate(GeoPoint origin, Double bearing, Double distance) {
		double b = Math.toRadians(bearing); // bearing to radians
		double d = distance / 1000.0; // distance to km

		double originLat = Math.toRadians(origin.getLat());
		double originLng = Math.toRadians(origin.getLng());

		double endLat = Math.asin(Math.sin(originLat) * Math.cos(d / EARTH_RADIUS)
				+ Math.cos(originLat) * Math.sin(d / EARTH_RADIUS) * Math.cos(b));
		double endLng = originLng
				+ Math.atan2(Math.sin(b) * Math.sin(d / EARTH_RADIUS) * Math.cos(originLat),
				Math.cos(d / EARTH_RADIUS) - Math.sin(originLat) * Math.sin(endLat));

		return new GeoPoint(Math.toDegrees(endLat), Math.toDegrees(endLng));
	}
}
