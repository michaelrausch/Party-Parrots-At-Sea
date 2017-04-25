package seng302.server;

import org.junit.Test;
import seng302.server.messages.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class TestMessage {
    private static int XML_MESSAGE_LEN = 14;
    private static int RACE_STATUS_BASE_LEN = 24;
    private static int BOAT_SUB_MESSAGE_LEN = 20;
    private static int CRC_LEN = 4;

    /**
     * Test generated output is the same as the expected output
     */
    @Test
    public void testHeatBetBufferOutputLength(){
        Message m = new Heartbeat(1);
        List<Integer> output = new ArrayList<>();

        DataOutputStream ds = new DataOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                output.add(b);
            }
        });

        m.send(ds);
        assertTrue(output.size() == (m.getSize() + CRC_LEN + Header.getSize()));
    }

    /**
     * Test output expected is the same as the spec
     */
    @Test
    public void testXmlMessageSize(){
        Message m = new XMLMessage("12345", XMLMessageSubType.BOAT, 1);
        assertTrue(m.getSize() == (XML_MESSAGE_LEN + "12345".length()));
    }

    /**
     * Ensure that when no boats are in the race, that only the base message is sent
     */
    @Test
    public void testRaceStatusMessageBufferLenNoBoats(){
        Message m = new RaceStatusMessage(1, RaceStatus.PRESTART,1,WindDirection.EAST,1,
                0,RaceType.MATCH_RACE,1, new ArrayList<BoatSubMessage>());

        List<Integer> output = new ArrayList<>();

        DataOutputStream ds = new DataOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                output.add(b);
            }
        });

        m.send(ds);
        assertTrue(output.size() == RACE_STATUS_BASE_LEN + Header.getSize() + CRC_LEN);
    }

    /**
     * Test that each boat status is added to the message
     */
    @Test
    public void testRaceStatusMessageBufferLenWithBoats(){
        List<BoatSubMessage> boatMessages = new ArrayList<>();
        List<Integer> output = new ArrayList<>();

        BoatSubMessage boat1 = new BoatSubMessage(1, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        BoatSubMessage boat2 = new BoatSubMessage(2, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        BoatSubMessage boat3 = new BoatSubMessage(3, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        boatMessages.add(boat1);
        boatMessages.add(boat2);
        boatMessages.add(boat3);

        Message m = new RaceStatusMessage(1, RaceStatus.PRESTART,1,WindDirection.EAST,1,
                3,RaceType.MATCH_RACE,1, boatMessages);

        DataOutputStream ds = new DataOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                output.add(b);
            }
        });

        m.send(ds);
        assertTrue(output.size() == (RACE_STATUS_BASE_LEN + (BOAT_SUB_MESSAGE_LEN * 3) + CRC_LEN + Header.getSize()));
    }

    /**
     * IllegalArgumentException should be thrown when numBoatsInRace is smaller
     * than the number of boats actually in the race
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRaceStatusTooManyBoats(){
        List<BoatSubMessage> boatMessages = new ArrayList<>();
        List<Integer> output = new ArrayList<>();

        BoatSubMessage boat1 = new BoatSubMessage(1, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        BoatSubMessage boat2 = new BoatSubMessage(2, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        BoatSubMessage boat3 = new BoatSubMessage(3, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        boatMessages.add(boat1);
        boatMessages.add(boat2);
        boatMessages.add(boat3);

        Message m = new RaceStatusMessage(1, RaceStatus.PRESTART,1,WindDirection.EAST,1,
                1,RaceType.MATCH_RACE,1, boatMessages);

        m.send(new DataOutputStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                System.out.print("");
            }
        }));
    }
}
