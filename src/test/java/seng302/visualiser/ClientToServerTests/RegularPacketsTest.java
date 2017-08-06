package seng302.visualiser.ClientToServerTests;

import java.lang.reflect.Field;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.testfx.api.FxToolkit;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.model.Yacht;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.GameClient;

/**
 * Test for checking how regularly packets are sent from ClientToServer Thread.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegularPacketsTest {

    private MainServerThread serverThread;
    private GameClient gameClient;
    private Pane pane;
    private Scene scene;
    private Stage stage;

//    @BeforeClass
//    public static void preSetup() throws Exception {
//        FxToolkit toolkit = FxToolkit.registerPrimaryStage();
//    }

    @Before
    public void setup() throws Exception {
        stage = FxToolkit.registerPrimaryStage();
        Platform.runLater(() -> {
            pane = new Pane();
            scene = new Scene(pane);
            stage.setScene(scene);
        });
        while (stage != null && pane != null && scene != null && stage.getScene() != null);
        gameClient = new GameClient(pane);
        gameClient.runAsHost("localhost", 4942);
        Field server = gameClient.getClass().getDeclaredField("server");
        server.setAccessible(true);
        serverThread = (MainServerThread) server.get(gameClient);
        GameState.setCurrentStage(GameStages.RACING);
        serverThread.startGame();
    }

    @Test
    public void Test1PacketsSentAtRegularIntervals () throws Exception {
        Yacht yacht = GameState.getYachts().get(0);
        double startAngle = yacht.getHeading();
        KeyEvent keyPress = new KeyEvent(null, scene,
            KeyEvent.KEY_PRESSED, "page_up", "page_up", KeyCode.PAGE_UP,
            false, false, false, false
        );
        KeyEvent keyRelease = new KeyEvent(null, scene,
            KeyEvent.KEY_RELEASED, "page_up", "page_up", KeyCode.PAGE_UP,
            false, false, false, false
        );
        long startTime = System.currentTimeMillis();
        pane.fireEvent(keyPress);
        while (yacht.getHeading() - startAngle > 100);
        pane.fireEvent(keyRelease);
        long endTime = System.currentTimeMillis();
        SleepThreadMaxDelay(); //Allowed to be two loops of delay due to loop delay and processing delay at client + server ends.
        Assert.assertEquals(100 / Yacht.TURN_STEP * ClientToServerThread.PACKET_SENDING_INTERVAL_MS,
            endTime - startTime / (100 / Yacht.TURN_STEP), 2 * ClientToServerThread.PACKET_SENDING_INTERVAL_MS);
    }

    @Test
    public void Test2ArbitraryPacketSentOnRelease() throws Exception {
        KeyEvent keyPress = new KeyEvent(null, scene,
            KeyEvent.KEY_PRESSED, "shift", "shift", KeyCode.SHIFT,
            false, false, false, false
        );
        KeyEvent keyRelease = new KeyEvent(null, scene,
            KeyEvent.KEY_RELEASED, "shift", "shift", KeyCode.SHIFT,
            false, false, false, false
        );
        Yacht yacht = GameState.getYachts().get(0);
        boolean startState = yacht.getSailIn();
        pane.fireEvent(keyPress);
        pane.fireEvent(keyRelease);
        SleepThreadMaxDelay();
        Assert.assertEquals(startState, !yacht.getSailIn());
    }

    @Test
    public void Test3ArbitraryPacketSentOnPress() throws Exception {
        KeyEvent keyPress = new KeyEvent(null, scene,
            KeyEvent.KEY_PRESSED, "space", "space", KeyCode.SPACE,
            false, false, false, false
        );
        Yacht yacht = GameState.getYachts().get(0);
        double heading = yacht.getHeading();
        double windDirection = GameState.getWindDirection();
        Yacht testYacht = new Yacht("", 0, "", "", "", "");
        testYacht.setHeading(heading);
        testYacht.tackGybe(windDirection);
        pane.fireEvent(keyPress);
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
}
