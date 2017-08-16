package seng302.visualiser.ClientToServerTests;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.messages.BoatAction;
import seng302.model.ServerYacht;
import seng302.visualiser.ClientToServerThread;

/**
 * Test for checking how regularly packets are sent from ClientToServer Thread.
 */
public class RegularPacketsTest {

    private MainServerThread serverThread;
    private ClientToServerThread clientThread;

    @Before
    public void setup() throws Exception {
        new GameState("localhost");
        serverThread = new MainServerThread();
        clientThread = new ClientToServerThread("localhost", 4942);
        GameState.setCurrentStage(GameStages.RACING);
    }

    @Test
    public void packetsSentAtRegularIntervals () throws Exception {
        final double TEST_DISTANCE = 10.0;
        serverThread.startGame();
        SleepThreadMaxDelay();
        ServerYacht yacht = new ArrayList<>(GameState.getYachts().values()).get(0);
        double startAngle = yacht.getHeading();
        long startTime = System.currentTimeMillis();
        clientThread.sendBoatAction(BoatAction.UPWIND); //start sending
        Thread.sleep(200);
        while (Math.abs(yacht.getHeading() - startAngle) < TEST_DISTANCE) {
            Thread.sleep(1);
        }
        clientThread.sendBoatAction(BoatAction.MAINTAIN_HEADING); //stop sending
        long endTime = System.currentTimeMillis();
        SleepThreadMaxDelay();
        //Allowed to be two loops of delay due to loop delay and processing delay at client + server ends.
        Assert.assertEquals(
            TEST_DISTANCE / ServerYacht.TURN_STEP * ClientToServerThread.PACKET_SENDING_INTERVAL_MS,
            (endTime - startTime), 2 * ClientToServerThread.PACKET_SENDING_INTERVAL_MS);
    }

//    @Test
//    public void testArbitraryPacketSent() throws Exception {
//        serverThread.startGame();
//        SleepThreadMaxDelay();
//        ServerYacht yacht = new ArrayList<>(GameState.getYachts().values()).get(0);
//        boolean startState = yacht.getSailIn();
//        clientThread.sendBoatAction(BoatAction.SAILS_IN);
//        SleepThreadMaxDelay();
//        Assert.assertEquals(startState, !yacht.getSailIn());
//    }

    /**
     * Give time for processing and packet sending. 200ms listed as absolute maximum for an
     * acceptable delay.
     * @throws Exception Thrown if thread crashes or something
     */
    private void SleepThreadMaxDelay() throws Exception {
        Thread.sleep(100);
    }

    @After
    public void teardown() {
        try {
            clientThread.setSocketToClose();
            serverThread.terminate();
            GameState.setCurrentStage(GameStages.LOBBYING);
        } catch (Exception e) {
            System.out.println("Caught expected exception.");
        }
    }
}
