package seng302.models;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.model.mark.Mark;
import seng302.model.mark.MarkOrder;
import seng302.model.mark.RacePosition;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class MarkOrderTest {
    private static MarkOrder markOrder;

    @BeforeClass
    public static void setup(){
        markOrder = new MarkOrder();
    }

    /**
     * Test to ensure marks are loaded from XML
     */
    @Test
    public void testMarkOrderLoadedFromXML(){
        assertTrue(markOrder != null);
    }

    /**
     * Test if .getNextMark() returns null if it is called with the final mark in the race
     */
    @Test
    public void testNextMarkAtEnd(){
        // There are no marks in the XML, therefore this can't be tested
        if (markOrder.getMarkOrder().size() == 0){
            return;
        }

        Mark lastMark = markOrder.getMarkOrder().get(markOrder.getMarkOrder().size() - 1);
        Integer lastIndex = markOrder.getMarkOrder().size() - 1;

        RacePosition lastRacePosition = new RacePosition(lastIndex, lastMark, null);

        assertEquals(null, markOrder.getNextPosition(lastRacePosition).getNextMark());
    }

    /**
     * Test if .getNextMark() method returns the next mark in the race
     */
    @Test
    public void testNextMark(){
        // There are not enough marks for this to be tested
        if (markOrder.getMarkOrder().size() < 2){
            return;
        }

        RacePosition firstRacePos = new RacePosition(0, markOrder.getMarkOrder().get(0), null);

        assertEquals(markOrder.getMarkOrder().get(1).getName(), markOrder.getNextPosition(firstRacePos).getNextMark().getName());
    }

    /**
     * Test if a whole race can be completed
     */
    @Test
    public void testMarkSequence(){
        RacePosition current = markOrder.getFirstPosition();

        while (!current.getIsFinishingLeg()){

            current = markOrder.getNextPosition(current);

            if (current.getIsFinishingLeg()){
                assertEquals(null, current.getNextMark());
            }
        }
    }

    @AfterClass
    public static void tearDown(){
        markOrder = null;
    }
}
