package seng302.gameServer.server;

import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.model.GeoPoint;
import seng302.model.Yacht;
import seng302.utilities.GeoUtility;

/**
 * Created by ryantan on 5/08/2017.
 */
public class TestInitialiseBoatPosition {
    private GeoPoint mark1 = new GeoPoint(50, 50);
    private GeoPoint mark2 = new GeoPoint(0, 0);

    private GameState gameState = new GameState("");

    @Test
    public void testInitialiseBoatPosition(){
//        // Calculating midpoint
//        Double perpendicularAngle = GeoUtility.getBearing(mark1, mark2);
//        Double length = GeoUtility.getDistance(mark1, mark2);
//        GeoPoint midpoint = GeoUtility.getGeoCoordinate(mark1, perpendicularAngle, length / 2);
//
//        // Create 8 yacht in game state
//        for (int i = 0; i < 8; i++) {
//            GameState.addYacht(i, new Yacht("Yacht", i, "1", "Yacht" + i, "Yacht" + i, "Test" ));
//        }
//
//        int i = 0;
//        for (Yacht yacht : GameState.getYachts().values()) {
//            GameState.startBoatInPosition(mark1, mark2, i, yacht);
//            double distance = GeoUtility.getDistance(midpoint, yacht.getLocation());
//            System.out.println(i + " " + distance);
//
//            double distanceApart = i / 2;
//            if (i % 2 == 1 && i != 0) {
//                distanceApart++;
//            }
//
//            assertTrue(distance <= (distanceApart * 50.01) && distance >= (distanceApart * 49.99));
//            i++;
//        }
    }
}
