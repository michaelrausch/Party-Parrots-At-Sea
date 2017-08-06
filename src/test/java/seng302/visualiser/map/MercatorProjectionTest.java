package seng302.visualiser.map;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import seng302.model.GeoPoint;
import seng302.v.map.MercatorProjection;

/**
 * Unit test for Mercator Project class.
 * Created by hyi25 on 15/05/17.
 */
public class MercatorProjectionTest {
	@Test
	public void toMapPoint() throws Exception {
		GeoPoint geo1 = new GeoPoint(12.485394, 19.38947);
		javafx.geometry.Point2D actualPoint1 = MercatorProjection.toMapPoint(geo1);
		javafx.geometry.Point2D expectedPoint1 = new javafx.geometry.Point2D(141.78806755555556, 119.0503853635612);
		assertEquals(expectedPoint1.getX(), actualPoint1.getX(), 0.0001);
		assertEquals(expectedPoint1.getY(), actualPoint1.getY(), 0.0001);

		GeoPoint geo2 = new GeoPoint(77.456432, -23.456462);
		javafx.geometry.Point2D actualPoint2 = MercatorProjection.toMapPoint(geo2);
		javafx.geometry.Point2D expectedPoint2 = new javafx.geometry.Point2D(111.31984924444444, 38.03143323746788);
		assertEquals(expectedPoint2.getX(), actualPoint2.getX(), 0.0001);
		assertEquals(expectedPoint2.getY(), actualPoint2.getY(), 0.0001);
	}

	@Test
	public void toMapGeo() throws Exception {
		javafx.geometry.Point2D point1 = new javafx.geometry.Point2D(123.1234, 25.4565);
		GeoPoint actualGeo1 = MercatorProjection.toMapGeo(point1);
		GeoPoint expectedGeo1 = new GeoPoint(80.77043127275441, -6.857718749999995);
		assertEquals(expectedGeo1.getLat(), actualGeo1.getLat(), 0.0001);
		assertEquals(expectedGeo1.getLng(), actualGeo1.getLng(), 0.0001);

		javafx.geometry.Point2D point2 = new javafx.geometry.Point2D(1.235, 255.4565);
		GeoPoint actualGeo2 = MercatorProjection.toMapGeo(point2);
		GeoPoint expectedGeo2 = new GeoPoint(-84.98475532898011, -178.26328125);
		assertEquals(expectedGeo2.getLat(), actualGeo2.getLat(), 0.0001);
		assertEquals(expectedGeo2.getLng(), actualGeo2.getLng(), 0.0001);
	}

}