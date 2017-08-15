package seng302.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seng302.gameServer.GameState;
import seng302.utilities.GeoUtility;

/**
 * Test update function in Yacht.java to make sure yacht will not be collide each other within 25.0
 * meters.
 */
public class UpdateYachtTest {

    private Yacht yacht1 = new Yacht("Yacht", 1, "1", "Yacht" + 1, "Yacht" + 1, "Test1");
    private Yacht yacht2 = new Yacht("Yacht", 2, "2", "Yacht" + 2, "Yacht" + 2, "Test2");
    private GeoPoint geoPoint1 = new GeoPoint(50.0, 50.0);
    private GeoPoint geoPoint2 = GeoUtility.getGeoCoordinate(geoPoint1, 90.0, 50.0);

    @Before
    public void setUpRace() {
        new GameState("");
        GameState.addYacht(1, yacht1);
        GameState.addYacht(2, yacht2);
        PolarTable.parsePolarFile(getClass().getResourceAsStream("/config/acc_polars.csv"));
    }

    @Test
    public void testUpdateYachtWithCollision() {
        // Yacht 1 heading towards 90 degrees heading
        yacht1.setLocation(geoPoint1);
        yacht1.updateLocation(geoPoint1.getLat(), geoPoint1.getLng(), 90.0, 5.0);

        // Yacht 2 heading towards 270 degrees heading
        yacht2.setLocation(geoPoint2);
        yacht2.updateLocation(geoPoint2.getLat(), geoPoint2.getLng(), 270.0, 5.0);

        // Start yacht 1 and rest yacht 2
        if (!yacht1.getSailIn()) {
            yacht1.toggleSailIn();
        }

        for (int i = 0; i < 6; i++) {
            yacht1.update((long) 1000);

            // Making sure boat is moving
            double moved = GeoUtility.getDistance(yacht1.getLocation(), geoPoint1);
            Assert.assertTrue(moved > 0);

            // Making sure no collision
            Double distance = GeoUtility.getDistance(yacht1.getLocation(), geoPoint2);

            Assert.assertTrue(distance > Math.min(Yacht.MARK_COLLISION_DISTANCE, Yacht.YACHT_COLLISION_DISTANCE));
        }
    }

    @Test
    public void testUpdateYachtWithoutCollision() {
        // Yacht 1 heading towards 90 degrees heading
        yacht1.setLocation(geoPoint1);
        yacht1.updateLocation(geoPoint1.getLat(), geoPoint1.getLng(), 90.0, 5.0);

        // Yacht 2 heading towards 90 degrees heading
        yacht2.setLocation(geoPoint2);
        yacht2.updateLocation(geoPoint2.getLat(), geoPoint2.getLng(), 90.0, 5.0);

        // Start yacht 1 and yacht 2
        if (!yacht1.getSailIn()) {
            yacht1.toggleSailIn();
        }
        if (!yacht2.getSailIn()) {
            yacht2.toggleSailIn();
        }

        double previousDistance1 = 0;
        double previousDistance2 = 0;

        for (int i = 0; i < 6; i++) {
            yacht1.update((long) 1000);
            yacht2.update((long) 1000);

            // Making sure boat is moving
            double yachtMoved1 = GeoUtility.getDistance(yacht1.getLocation(), geoPoint1);
            Assert.assertTrue(yachtMoved1 > previousDistance1);
            previousDistance1 = yachtMoved1;

            double yachtMoved2 = GeoUtility.getDistance(yacht2.getLocation(), geoPoint2);
            Assert.assertTrue(yachtMoved2 > previousDistance2);
            previousDistance2 = yachtMoved2;
        }
    }
}
