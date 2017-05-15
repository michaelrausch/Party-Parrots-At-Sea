package seng302.models.map;

public class MercatorProjection {

	private static final double MERCATOR_RANGE = 256;
	private static final double pixelsPerLngDegree = MERCATOR_RANGE / 360.0;
	private static final double pixelsPerLngRadian = MERCATOR_RANGE / (2 * Math.PI);

	/**
	 * A help function keeps the value in bound between -0.9999 and 0.9999.
	 * @param value in bound value
	 * @return the value in bound
	 */
	private static double bound(double value) {
		return Math.min(Math.max(value, -0.9999), 0.9999);
	}

	/**
	 * Projects a Geo Location (lat, lng) on a planar
	 * @param geo MapGeo (lat, lng) location to be projected
	 * @return the projection GeoPoint (x, y) on planar
	 */
	public static MapPoint toMapPoint(MapGeo geo) {
		MapPoint point = new MapPoint(0, 0);
		MapPoint origin = new MapPoint(MERCATOR_RANGE / 2.0, MERCATOR_RANGE / 2.0);
		point.setX(origin.getX() + geo.getLng() * pixelsPerLngDegree);

//		NOTE(appleton): Truncating to 0.9999 effectively limits latitude to
//      89.189.  This is about a third of a tile past the edge of the world tile.
		double sinY = bound(Math.sin(Math.toRadians(geo.getLat())));
		point.setY(origin.getY() + 0.5 * Math.log((1 + sinY) / (1 - sinY)) * (-pixelsPerLngRadian));
		return point;
	}

	/**
	 * Converts the planar projection (x, y) back to Geo Location (lat, lng)
	 * @param point MapPoint (x, y) to be converted back
	 * @return the original Geo location converted from the given projection point
	 */
	public static MapGeo toMapGeo(MapPoint point) {
		MapPoint origin = new MapPoint(MERCATOR_RANGE / 2.0, MERCATOR_RANGE / 2.0);
		double lng = (point.getX() - origin.getX()) / pixelsPerLngDegree;
		double latRadians = (point.getY() - origin.getY()) / (-pixelsPerLngRadian);
		double lat = Math.toDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2.0);
		return new MapGeo(lat, lng);
	}
}
