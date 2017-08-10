package seng302.models;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import seng302.gameServer.GameState;
import seng302.model.Yacht;


public class YachtTest {

    private static Yacht y1;
    //Yacht y2;
    private static Double windDirection = 45d;
    private static Double windSpeed = 20d;
    private static GameState gs;

    @BeforeClass
    public static void setUp() {
        y1 = new Yacht("Yacht", 101, "Y1", "Y1", "Yacht 1", "C1");
        gs = new GameState("localhost");
    }

    @Test
    public void tackGybeTest() {
        HashMap<Double, Double> values = new HashMap<>();
        values.put(280.0, 80.0);
        values.put(270.0, 90.0);
        values.put(359.0, 1.0);
        values.put(180.0, 180.0);
        values.put(75.0, 285.0);

        for (Double begin : values.keySet()) {
            y1.setHeading(begin);
            y1.tackGybe(windDirection);
            for (int i = 0; i < 50; i++) {
                y1.runAutoPilot();
            }
            assertEquals(values.get(begin), y1.getHeading(), 5.0);
        }

    }


    @AfterClass
    public static void tearDown() {
        y1 = null;
    }

}
