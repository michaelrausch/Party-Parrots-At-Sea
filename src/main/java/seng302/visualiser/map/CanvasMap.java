package seng302.visualiser.map;

import java.net.URL;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.lang.Math;

/**
 * CanvasMap retrieves a map image with given geo boundary from Google Map server.
 * By passing a rectangle like geo boundary, it returns a map image with the
 * highest resolution. However, due to free quote account usage limit, the maximum
 * resolution is only 1280 * 1280.
 *
 * Created by Haoming on 15/5/2017
 */
public class CanvasMap {

	private Boundary boundary;
	private long width, height;  // desired image size
	private int zoom;

	private String KEY = "AIzaSyC-5oOShMCY5Oy_9L7guYMPUPFHDMr37wE";

	public CanvasMap(Boundary boundary) {
		this.boundary = boundary;
		calculateOptimalMapSize();
	}

	public Image getMapImage() {
		try {
			URL url = new URL(getRequest());
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			return new Image(connection.getInputStream());
		} catch (Exception e) {
			System.out.println("[CanvasMap] Exception");
			return null;
		}
	}

	private String getRequest() {
		StringBuilder sb = new StringBuilder();
		sb.append("https://maps.googleapis.com/maps/api/staticmap?");
		sb.append(String.format("center=%f,%f", boundary.getCentreLat(), boundary.getCentreLng()));
		sb.append(String.format("&zoom=%d", zoom));
		sb.append(String.format("&size=%dx%d&scale=2", width, height));
		sb.append("&style=feature:all|element:labels|visibility:off"); // hide all labels on map
//		sb.append(String.format("&markers=%f,%f", boundary.getSouthLat(), boundary.getWestLng()));
//		sb.append(String.format("&key=%s", KEY));
		return sb.toString();
	}

	private void calculateOptimalMapSize() {
		for (int z = 20; z > 0; z--) {
			MapSize mapSize = getMapSize(z, boundary);
			zoom = z;
			width = mapSize.width;
			height = mapSize.height;
			// if map size is valid, exit the loop as we have the highest resolution
			if (mapSize.isValid()) break;
		}
	}

	private MapSize getMapSize(int zoom, Boundary boundary) {
		double scale = Math.pow(2, zoom);
		GeoPoint geoSW = new GeoPoint(boundary.getSouthLat(), boundary.getWestLng());
		GeoPoint geoNE = new GeoPoint(boundary.getNorthLat(), boundary.getEastLng());
		Point2D pointSW = MercatorProjection.toMapPoint(geoSW);
		Point2D pointNE = MercatorProjection.toMapPoint(geoNE);
		return new MapSize(Math.abs(pointNE.getX() - pointSW.getX()) * scale,
				Math.abs(pointNE.getY() - pointSW.getY()) * scale);
	}

	class MapSize {
		long width, height;

		MapSize(double width, double height) {
			this.width = Math.round(width);
			this.height = Math.round(height);
		}

		/**
		 * Map size is valid when width and height are both less than 640 pixels
		 * @return true if both dimensions are less than 640px
		 */
		boolean isValid() {
			return Math.max(width, height) <= 640;
		}
	}

	public long getWidth() {
		return width;
	}

	public long getHeight() {
		return height;
	}

	public int getZoom() {
		return zoom;
	}
}
