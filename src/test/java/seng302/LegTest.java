package seng302;

import org.junit.Test;
import seng302.models.Leg;
import seng302.models.mark.SingleMark;

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
        Leg leg = new Leg(010, 100, "SingleMark");

        assertEquals(leg.getHeading(), 010);
        assertEquals(leg.getDistance(), 100);
        assertEquals(leg.getMarkerLabel(), "SingleMark");
        assertEquals(leg.getIsFinishingLeg(), false);
    }

    /**
     * Test creation of the leg by providing a
     * SingleMark object
     */
    @Test
    public void testLegCreation() {
        Leg leg = new Leg(010, 100, new SingleMark("SingleMark"));

        assertEquals(leg.getHeading(), 010);
        assertEquals(leg.getDistance(), 100);
        assertEquals(leg.getMarkerLabel(), "SingleMark");
        assertEquals(leg.getIsFinishingLeg(), false);
    }

    /**
     * Test changing whether or not a
     * leg is the finishing leg
     */
    @Test
    public void testSetFinishLeg() {
        Leg leg = new Leg(010, 100, "SingleMark");

        leg.setFinishingLeg(true);
        assertEquals(leg.getIsFinishingLeg(), true);
    }

}
