// TODO: 4/05/17 cir27 - Find out why this test is causing build failures
//
//package seng302.models;
//
//public class MarkGroupTest {
//
//}
package seng302.models;

import javafx.scene.shape.Circle;
import seng302.*;
import javafx.geometry.Point2D;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seng302.models.mark.*;

/**
 * Created by cir27 on 4/05/17.
 */
public class MarkGroupTest {
    private MarkGroup gateMG;
    private MarkGroup singleMG;

    @Before
    public void setUp () {
        Mark single = new SingleMark("SM", 0, 0 , 0);
        Mark gate = new GateMark(
                "GM",
                MarkType.OPEN_GATE,
                new SingleMark("GM1", 0, 0, 1),
                new SingleMark("GM2", 0, 0, 2),
                0,
                0);
        gateMG = new MarkGroup(gate, new Point2D(10, 10), new Point2D(20, 20));
        singleMG = new MarkGroup(single, new Point2D(0, 0));
    }

    @Test
    public void hasIDSingle () {
        Assert.assertTrue(singleMG.hasRaceId(0));
        Assert.assertTrue(!singleMG.hasRaceId(100,12));
    }

    @Test
    public void hasIdGate () {
        Assert.assertTrue(gateMG.hasRaceId(1));
        Assert.assertTrue(gateMG.hasRaceId(2));
        Assert.assertTrue(!gateMG.hasRaceId(100,12));
    }

    @Test
    public void nothingOnWrongId () {
        double originalX = singleMG.getChildren().get(0).getLayoutX();
        double originalY = singleMG.getChildren().get(0).getLayoutY();
        singleMG.setDestination(10, 10, 0, 4);
        singleMG.updatePosition(400);
        Assert.assertTrue(originalX == singleMG.getChildren().get(0).getLayoutX());
        Assert.assertTrue(originalY == singleMG.getChildren().get(0).getLayoutY());
    }

    @Test
    public void correctMovementCorrectIdSingle () {
        double originalX = singleMG.getChildren().get(0).getLayoutX();
        double originalY = singleMG.getChildren().get(0).getLayoutY();
        long timeinterval = 1000/60;
        double expectedChange = 10 / 200 * timeinterval;
        singleMG.setDestination(originalX + 10, originalY + 10, 0, 0);
        singleMG.updatePosition(timeinterval);
        Assert.assertTrue(originalX + expectedChange == singleMG.getChildren().get(0).getLayoutX());
        Assert.assertTrue(originalY + expectedChange == singleMG.getChildren().get(0).getLayoutY());
    }

    @Test
    public void correctMovementCorrectIDGate () {
        double originalX1 = gateMG.getChildren().get(0).getLayoutX();
        double originalY1 = gateMG.getChildren().get(0).getLayoutY();
        double originalX2 = gateMG.getChildren().get(1).getLayoutX();
        double originalY2 = gateMG.getChildren().get(1).getLayoutY();
        long timeinterval = 1000/60;
        double expectedChange = 10 / 200 * timeinterval;
        gateMG.setDestination(originalX1 + 10, originalY1 + 10, 0, 1);
        gateMG.setDestination(originalX2 + 10, originalY2 + 10, 0, 2);
        gateMG.updatePosition(timeinterval);
        Assert.assertTrue(originalX1 + expectedChange == gateMG.getChildren().get(0).getLayoutX());
        Assert.assertTrue(originalY1 + expectedChange == gateMG.getChildren().get(0).getLayoutY());
        Assert.assertTrue(originalX2 + expectedChange == gateMG.getChildren().get(1).getLayoutX());
        Assert.assertTrue(originalY2 + expectedChange == gateMG.getChildren().get(1).getLayoutY());
    }
}
