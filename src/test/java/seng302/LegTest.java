package seng302;

import org.junit.Test;
import seng302.models.Leg;
import seng302.models.Mark;

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
        Leg leg = new Leg(010, 100, "Mark");

        assertEquals(leg.getHeading(), 010);
        assertEquals(leg.getDistance(), 100);
        assertEquals(leg.getMarkerLabel(), "Mark");
        assertEquals(leg.getIsFinishingLeg(), false);
    }

    /**
     * Test creation of the leg by providing a
     * Mark object
     */
    @Test
    public void testLegCreation() {
        Leg leg = new Leg(010, 100, new Mark("Mark"));

        assertEquals(leg.getHeading(), 010);
        assertEquals(leg.getDistance(), 100);
        assertEquals(leg.getMarkerLabel(), "Mark");
        assertEquals(leg.getIsFinishingLeg(), false);
    }

    /**
     * Test changing whether or not a
     * leg is the finishing leg
     */
    @Test
    public void testSetFinishLeg() {
        Leg leg = new Leg(010, 100, "Mark");

        leg.setFinishingLeg(true);
        assertEquals(leg.getIsFinishingLeg(), true);
    }

}
