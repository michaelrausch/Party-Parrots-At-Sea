package seng302.visualiser.ClientToServerTests;

import java.util.ArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.server.messages.BoatActionType;
import seng302.model.Yacht;
import seng302.visualiser.ClientToServerThread;

/**
 * Test for checking how regularly packets are sent from ClientToServer Thread.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    public void Test1PacketsSentAtRegularIntervals () throws Exception {
        final double TEST_DISTANCE = 10.0;
        serverThread.startGame();
        SleepThreadMaxDelay();
        Yacht yacht = new ArrayList<>(GameState.getYachts().values()).get(0);
        double startAngle = yacht.getHeading();
        long startTime = System.currentTimeMillis();
        clientThread.sendBoatEvent(BoatActionType.UPWIND);
        Thread.sleep(200);
        while (Math.abs(yacht.getHeading() - startAngle) < TEST_DISTANCE) {
            //Wait for yacht to move
        }
        clientThread.sendBoatEvent(BoatActionType.MAINTAIN_HEADING);
        long endTime = System.currentTimeMillis();
        SleepThreadMaxDelay();
        //Allowed to be two loops of delay due to loop delay and processing delay at client + server ends.
        Assert.assertEquals(TEST_DISTANCE / Yacht.TURN_STEP * ClientToServerThread.PACKET_SENDING_INTERVAL_MS,
            (endTime - startTime), 2 * ClientToServerThread.PACKET_SENDING_INTERVAL_MS);
    }

    @Test
    public void Test2ArbitraryPacketSentOnRelease() throws Exception {
        serverThread.startGame();
        SleepThreadMaxDelay();
        Yacht yacht = new ArrayList<>(GameState.getYachts().values()).get(0);
        boolean startState = yacht.getSailIn();
        clientThread.sendBoatEvent(BoatActionType.SAILS_IN);
        SleepThreadMaxDelay();
        Assert.assertEquals(startState, !yacht.getSailIn());
    }

    @Test
    public void Test3ArbitraryPacketSentOnPress() throws Exception {
        serverThread.startGame();
        SleepThreadMaxDelay();
        Yacht yacht = new ArrayList<>(GameState.getYachts().values()).get(0);
        double heading = yacht.getHeading();
        double windDirection = GameState.getWindDirection();
        Yacht testYacht = new Yacht("", 0, "", "", "", "");
        testYacht.setHeading(heading);
        testYacht.tackGybe(windDirection);
        clientThread.sendBoatEvent(BoatActionType.TACK_GYBE);
        SleepThreadMaxDelay();
        Assert.assertEquals(testYacht.getHeading(), yacht.getHeading(), 1);
    }

    /**
     * Give time for processing and packet sending. 200ms listed as absolute maximum for an
     * acceptable delay.
     * @throws Exception Thrown if thread crashes or something
     */
    private void SleepThreadMaxDelay() throws Exception {
        Thread.sleep(200);
    }

    @After
    public void teardown () {
        serverThread.terminate();
        clientThread.setSocketToClose();
        GameState.setCurrentStage(GameStages.LOBBYING);
    }
}
