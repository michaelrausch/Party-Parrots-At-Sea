//package seng302;
//
//import org.junit.Test;
//import seng302.models.Boat;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Unit test for the Team class.
// */
//public class BoatTest {
//
//    @Test
//    public void testBoatCreation() {
//        Boat boat1 = new Boat("Team 1");
//        assertEquals(boat1.getTeamName(), "Team 1");
//        assertEquals(boat1.getVelocity(), (double) 10.0, 1e-15);
//    }
//
//    @Test
//    public void testChangeTeamName() {
//        Boat boat1 = new Boat("Team 1");
//        boat1.setTeamName("Team 2");
//        assertEquals(boat1.getTeamName(), "Team 2");
//    }
//
//    @Test
//    public void testSetVelocity() {
//        Boat boat1 = new Boat("Team 1", 29.0, "", 100);
//        assertEquals(boat1.getVelocity(), (double) 29.0, 1e-15);
//
//        boat1.setVelocity(12.0);
//        assertEquals(boat1.getVelocity(), (double)12.0, 1e-15);
//    }
//}
