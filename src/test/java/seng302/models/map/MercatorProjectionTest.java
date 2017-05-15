package seng302.models.map;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for Mercator Project class.
 * Created by hyi25 on 15/05/17.
 */
public class MercatorProjectionTest {
	@Test
	public void toMapPoint() throws Exception {
		MapGeo geo1 = new MapGeo(12.485394, 19.38947);
		MapPoint actualPoint1 = MercatorProjection.toMapPoint(geo1);
		MapPoint expectedPoint1 = new MapPoint(141.78806755555556, 119.0503853635612);
		assertEquals(expectedPoint1.getX(), actualPoint1.getX(), 0.0001);
		assertEquals(expectedPoint1.getY(), actualPoint1.getY(), 0.0001);

		MapGeo geo2 = new MapGeo(77.456432, -23.456462);
		MapPoint actualPoint2 = MercatorProjection.toMapPoint(geo2);
		MapPoint expectedPoint2 = new MapPoint(111.31984924444444, 38.03143323746788);
		assertEquals(expectedPoint2.getX(), actualPoint2.getX(), 0.0001);
		assertEquals(expectedPoint2.getY(), actualPoint2.getY(), 0.0001);
	}

	@Test
	public void toMapGeo() throws Exception {
		MapPoint point1 = new MapPoint(123.1234, 25.4565);
		MapGeo actualGeo1 = MercatorProjection.toMapGeo(point1);
		MapGeo expectedGeo1 = new MapGeo(80.77043127275441, -6.857718749999995);
		assertEquals(expectedGeo1.getLat(), actualGeo1.getLat(), 0.0001);
		assertEquals(expectedGeo1.getLng(), actualGeo1.getLng(), 0.0001);

		MapPoint point2 = new MapPoint(1.235, 255.4565);
		MapGeo actualGeo2 = MercatorProjection.toMapGeo(point2);
		MapGeo expectedGeo2 = new MapGeo(-84.98475532898011, -178.26328125);
		assertEquals(expectedGeo2.getLat(), actualGeo2.getLat(), 0.0001);
		assertEquals(expectedGeo2.getLng(), actualGeo2.getLng(), 0.0001);
	}

}