package seng302.models;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.gameServer.messages.XMLMessageSubType;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.MarkOrder;
import seng302.model.stream.packets.StreamPacket;
import seng302.utilities.StreamParser;
import seng302.utilities.XMLParser;

public class MarkOrderTest {
    private static MarkOrder markOrder;
    private static Integer currentSeqID;

    @BeforeClass
    public static void setup(){
        /*
                    %%%%%%
                   %%%% = =
                   %%C    >
                    _)' _( .' ,
                 __/ |_/\   " *. o
                /` \_\ \/     %`= '_  .
               /  )   \/|      .^',*. ,
              /' /-   o/       - " % '_
             /\_/     <       = , ^ ~ .
             )_o|----'|          .`  '
         ___// (_  - (\
        ///-(    \'   \\

         */
        markOrder = new MarkOrder(
            XMLParser.parseRace(
                StreamParser.extractXmlMessage(
                    new StreamPacket(
                        XMLMessageSubType.RACE.getType(),
                        XMLParser.parseRaceDef("/maps/default.xml", "test", 2).getValue().length(),
                        0, XMLParser.parseRaceDef("/maps/default.xml", "test", 2).getValue().getBytes()
                    )
                )
            )
        );
        currentSeqID = 0;
    }

    /**
     * Test to ensure marks are loaded from XML
     */
    @Test
    public void testMarkOrderLoadedFromXML(){
        assertTrue(markOrder != null);
    }


    @Test
    public void testIsLastMark() {
        currentSeqID = 0;
        assertFalse(markOrder.isLastMark(currentSeqID));

        currentSeqID = markOrder.getMarkOrder().size() - 1;
        assertTrue(markOrder.isLastMark(currentSeqID));
    }

    @Test
    public void testGetNextMark() {
        currentSeqID = 4;
        CompoundMark nextMark = markOrder.getMarkOrder().get(4 + 1);
        assertEquals(nextMark, markOrder.getNextMark(currentSeqID));

        currentSeqID = 3;
        nextMark = markOrder.getMarkOrder().get(3 + 1);
        assertEquals(nextMark, markOrder.getNextMark(currentSeqID));
    }

    @Test
    public void testGetCurrentMark() {
        currentSeqID = 0;
        CompoundMark currentMark = markOrder.getMarkOrder().get(0);
        assertEquals(currentMark, markOrder.getCurrentMark(0));
    }

    @Test
    public void testGetPreviousMark() {
        currentSeqID = 1;
        CompoundMark prevMark = markOrder.getMarkOrder().get(0);
        assertEquals(prevMark, markOrder.getPreviousMark(currentSeqID));
    }

    @AfterClass
    public static void tearDown(){
        markOrder = null;
    }
}
