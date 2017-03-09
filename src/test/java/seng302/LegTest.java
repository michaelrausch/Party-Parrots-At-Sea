package seng302;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for the Leg class.
 */
public class LegTest {

    /**
     * Test creation of the leg by specifying a string
     * for the marker label
     */
    @Test
    public void testLegCreationUsingMarkerLabel() {
        Leg leg = new Leg(010, 100, "Marker");

        assertEquals(leg.getHeading(), 010);
        assertEquals(leg.getDistance(), 100);
        assertEquals(leg.getMarkerLabel(), "Marker");
        assertEquals(leg.getIsFinishingLeg(), false);
    }

    /**
     * Test creation of the leg by providing a
     * Marker object
     */
    @Test
    public void testLegCreation() {
        Leg leg = new Leg(010, 100, new Marker("Marker"));

        assertEquals(leg.getHeading(), 010);
        assertEquals(leg.getDistance(), 100);
        assertEquals(leg.getMarkerLabel(), "Marker");
        assertEquals(leg.getIsFinishingLeg(), false);
    }

    /**
     * Test changing whether or not a
     * leg is the finishing leg
     */
    @Test
    public void testSetFinishLeg() {
        Leg leg = new Leg(010, 100, "Marker");

        leg.setFinishingLeg(true);
        assertEquals(leg.getIsFinishingLeg(), true);
    }

}
