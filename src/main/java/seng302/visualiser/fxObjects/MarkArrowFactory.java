package seng302.visualiser.fxObjects;

import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

/**
 * Created by cir27 on 9/08/17.
 */
public class MarkArrowFactory {

    public enum RoundingType {
        PORT,
        STARBOARD,
    }

    public static Group constructEntryArrow (RoundingType roundingSide, double angleOfEntry,
        double angleOfExit, Paint colour) {
        Group arrow = new Group();
        return arrow;
    }

    public static Group constructExitArrow (RoundingType roundingSide, double angleOfEntry, Paint colour) {
        Group arrow = new Group();
        Line arrowBody;
        Polyline arrowHead = constructArrowHead();
        if (roundingSide == RoundingType.PORT) {
            arrowBody = new Line(
                -10, -10,
                -10, -30
            );
            arrowHead.setLayoutX(-10);
            arrowHead.setLayoutY(-10);
        } else {
            arrowBody = new Line(
                10, -10,
                10, -30
            );
            arrowHead.setLayoutX(10);
            arrowHead.setLayoutY(-10);
        }
        arrowBody.setFill(colour);
        arrowHead.setFill(colour);
        arrow.getChildren().addAll(arrowBody, arrowHead);
        return arrow;
    }

    private static Polyline constructArrowHead () {
        return new Polyline(
            -5, -5,
            0, 0,
            5, -5
        );
    }
}
