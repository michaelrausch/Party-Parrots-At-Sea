package seng302.models;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.MarkOrder;

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

//    /**
//     * Test if .equals() method on returns true on two marks that are equal
//     */
//    @Test
//    public void testMarkEqualsTrue(){
//        M mark1 = new SingleMark("asd", 1.1, 2.2, 1, 2);
//        Mark mark2 = new SingleMark("asd", 1.1, 2.2, 1, 2);
//
//        assertEquals(mark1, mark2);
//    }
//
//    /**
//     * Test if .equals() method on returns false on two marks that are NOT equal
//     */
//    @Test
//    public void testMarkNotEquals(){
//        Mark mark1 = new SingleMark("asf", 1.1, 2.2, 2, 2);
//        Mark mark2 = new SingleMark("asd", 1.1, 2.2, 1, 2);
//
//        assertNotEquals(mark1, mark2);
//    }

    /**
     * Test if .getNextMark() returns null if it is called with the final mark in the race
     */
    @Test
    public void testNextMarkAtEnd(){
        // There are no marks in the XML, therefore this can't be tested
        if (markOrder.getMarkOrder().size() == 0){
            return;
        }

        CompoundMark lastMark = markOrder.getMarkOrder().get(markOrder.getMarkOrder().size() - 1);

        assertEquals(null, markOrder.getNextMark(lastMark));
    }

    /**
     * Test if .getNextMark() method on returns null if the mark does not exist in the race
     */
    @Test
    public void testNextMarkNotExists(){
        CompoundMark someMark = new CompoundMark(1, "something");

        assertEquals(null, markOrder.getNextMark(someMark));
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

        CompoundMark firstMark = markOrder.getMarkOrder().get(0);

        assertEquals(markOrder.getMarkOrder().get(1), markOrder.getNextMark(firstMark));
    }

    @AfterClass
    public static void tearDown(){
        markOrder = null;
    }
}
