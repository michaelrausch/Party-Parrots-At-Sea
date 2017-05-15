package seng302.models.map;

import javafx.scene.image.Image;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

import java.lang.Math;

public class CanvasMap {

	private Boundary bound;
	private double width, height;  // desired image size
	private int zoom;

	private String KEY = "AIzaSyC-5oOShMCY5Oy_9L7guYMPUPFHDMr37wE";

	public CanvasMap(Boundary bound, double width, double height) {
		this.bound = bound;
		this.width = width;
		this.height = height;
	}

	public Image getMapImage() {
		try {
			System.out.println(getRequest());
			URL url = new URL(getRequest());
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			return new Image(connection.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getRequest() {
		zoom = 15;
		StringBuilder sb = new StringBuilder();
		sb.append("https://maps.googleapis.com/maps/api/staticmap?");
		sb.append(String.format("center=%f,%f", bound.getCentreLat(), bound.getCentreLng()));
		sb.append(String.format("&zoom=%d", zoom));
		sb.append(String.format("&size=%.0fx%.0f&scale=2", width / 2, height / 2));
		sb.append("&style=feature:all|element:labels|visibility:off"); // hide all labels on map
//		sb.append(String.format("&key=%s", KEY));
		return sb.toString();
	}

	private MapSize getMapSize(int zoom, Boundary boundary) {
		double scale = Math.pow(2, zoom);
		MapGeo geoSW = new MapGeo(boundary.getSouthLat(), boundary.getWestLng());
		MapGeo geoNE = new MapGeo(boundary.getNorthLat(), boundary.getEastLng());
		MapPoint pointSW = MercatorProjection.toMapPoint(geoSW);
		MapPoint pointNE = MercatorProjection.toMapPoint(geoNE);
		return new MapSize(Math.abs(pointNE.getX() - pointSW.getX()),
				Math.abs(pointNE.getY() - pointSW.getY()));
	}

	class MapSize {
		long width, height;

		MapSize(double width, double height) {
			this.width = (long) width;
			this.height = (long) height;
		}
	}
}
