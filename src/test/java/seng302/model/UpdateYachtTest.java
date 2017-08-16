//package seng302.model;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import seng302.gameServer.GameState;
//import seng302.utilities.GeoUtility;
//
///**
// * Test update function in Yacht.java to make sure yacht will not be collide each other within 25.0
// * meters.
// */
//public class UpdateYachtTest {
//
//    private ServerYacht yacht1 = new ServerYacht("Yacht", 1, "1", "Yacht" + 1, "Yacht" + 1, "Test1");
//    private ServerYacht yacht2 = new ServerYacht("Yacht", 2, "2", "Yacht" + 2, "Yacht" + 2, "Test2");
//    private GeoPoint geoPoint1 = new GeoPoint(50.0, 50.0);
//    private GeoPoint geoPoint2 = GeoUtility.getGeoCoordinate(geoPoint1, 90.0, 50.0);
//
//    @Before
//    public void setUpRace() {
//        new GameState("");
//        GameState.addYacht(1, yacht1);
//        GameState.addYacht(2, yacht2);
//        PolarTable.parsePolarFile(getClass().getResourceAsStream("/config/acc_polars.csv"));
//    }
//
//    @Test
//    public void testUpdateYachtWithCollision() {
//        // Yacht 1 heading towards 90 degrees heading
//        yacht1.setLocation(geoPoint1);
//
//        // Yacht 2 heading towards 270 degrees heading
//        yacht2.setLocation(geoPoint1);
//
//        // Start yacht 1 and rest yacht 2
//        if (!yacht1.getSailIn()) {
//            yacht1.toggleSailIn();
//        }
//        checkCollision(yacht1);
//        double moved = GeoUtility.getDistance(yacht1.getLocation(), geoPoint1);
//        Assert.assertEquals(GameState.BOUNCE_DISTANCE_YACHT, moved, 0.1);
//    }
//
//    @Test
//    public void testUpdateYachtWithoutCollision() {
//        // Yacht 1 heading towards 90 degrees heading
//        yacht1.setLocation(geoPoint1);
//
//        // Yacht 2 heading towards 270 degrees heading
//        yacht2.setLocation(geoPoint2);
//
//        // Start yacht 1 and rest yacht 2
//        if (!yacht1.getSailIn()) {
//            yacht1.toggleSailIn();
//        }
//        checkCollision(yacht1);
//        Assert.assertTrue(
//            GameState.YACHT_COLLISION_DISTANCE < GeoUtility.getDistance(geoPoint1, geoPoint2
//            )
//        ); //Check that yachts are actually far enough apart for no collision.
//        Assert.assertEquals(geoPoint1.getLat(), yacht1.getLocation().getLat(), 0.001);
//        Assert.assertEquals(geoPoint1.getLng(), yacht1.getLocation().getLng(), 0.001);
//        Assert.assertEquals(geoPoint2.getLat(), yacht1.getLocation().getLat(), 0.001);
//        Assert.assertEquals(geoPoint2.getLng(), yacht1.getLocation().getLng(), 0.001);
//    }
//}
