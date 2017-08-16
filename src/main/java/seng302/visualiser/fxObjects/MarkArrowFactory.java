package seng302.visualiser.fxObjects;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

// TODO: 16/08/17 this class used to be well written... FeelsBadMan. Maybe lose the ternary operators.
/**
 * Factory class for making rounding arrows for mark objects out of JavaFX objects.
 */
public class MarkArrowFactory {

    /**
     * The side of the boat that will be closest to the mark.
     */
    public enum RoundingSide {
        PORT,
        STARBOARD,
    }

    public static final double MARK_ARROW_SEPARATION = 15;
    public static final double ARROW_LENGTH = 75;
    public static final double ARROW_HEAD_DEPTH = 10;
    public static final double ARROW_HEAD_WIDTH = 6;
    public static final double STROKE_WIDTH = 3;

    /**
     * Creates an entry arrow group showing an arrow into and out of the rounding area. It is centered on (0, 0).
     * @param roundingSide The side of the boat that will be closest to the mark.
     * @param angleOfEntry The angle between this mark and the last one as a heading from north in degrees.
     * @param angleOfExit The angle between this mark and the next one as a heading from north in degrees.
     * @param colour The desired colour of the arrows.
     * @return The group containing all JavaFX objects.
     */
    public static Group constructEntryArrow (RoundingSide roundingSide, double angleOfEntry,
                                             double angleOfExit, Paint colour) {
        angleOfEntry = 180 - angleOfEntry;
        Group arrow = new Group();
        Group exitSection = constructExitArrow(roundingSide, angleOfExit, colour);
        angleOfExit = 180 - angleOfExit;
//        double minAngle = Math.min(angleOfEntry, angleOfExit);
//        double arcLen = Math.max(angleOfEntry, angleOfExit) - minAngle;
//        Arc roundSection = new Arc(
//                0, 0, MARK_ARROW_SEPARATION, MARK_ARROW_SEPARATION,
//                angleOfEntry, 180 - (angleOfEntry - angleOfExit)
//        );
        System.out.println(angleOfEntry);
        System.out.println(angleOfExit);
        System.out.println(angleOfExit - angleOfEntry);
        Arc roundSection = new Arc(
            0, 0, MARK_ARROW_SEPARATION, MARK_ARROW_SEPARATION,
            (roundingSide == RoundingSide.PORT ? -180 : 0) + angleOfEntry,
//            roundingSide == RoundingSide.PORT ? Math.abs(angleOfExit - angleOfEntry) : angleOfExit - angleOfEntry
            roundingSide == RoundingSide.PORT ? angleOfExit- angleOfEntry : angleOfEntry - angleOfExit
        );
        roundSection.setStrokeWidth(STROKE_WIDTH);
        roundSection.setType(ArcType.OPEN);
        roundSection.setStroke(colour);
        roundSection.setFill(new Color(0,0,0,0));
        Polygon entrySection = constructLineSegment(
                roundingSide == RoundingSide.PORT ? RoundingSide.STARBOARD : RoundingSide.PORT, 180 + angleOfEntry, colour
        );
//        Polygon entrySection = new Polygon();
        arrow.getChildren().addAll(exitSection, roundSection, entrySection);
        return arrow;
    }

    /**
     * Creates an exit arrow group pointing towards the next mark.
     * @param roundingSide The side of the boat that will be closest to the mark.
     * @param angle The angle to the next mark as a heading from north in degrees.
     * @param colour The colour of the arrow.
     * @return The group containing all the JavaFX objects.
     */
    public static Group constructExitArrow (RoundingSide roundingSide, double angle, Paint colour) {
        angle = 180 - angle;
        Group arrow = new Group();
        Polygon arrowBody = constructLineSegment(roundingSide, angle, colour);
        Polyline arrowHead = constructArrowHead(angle, colour);
        arrowHead.setLayoutX(arrowBody.getPoints().get(2));
        arrowHead.setLayoutY(arrowBody.getPoints().get(3));
        arrow.getChildren().addAll(arrowBody, arrowHead);
        return arrow;
    }

    private static Polygon constructLineSegment (RoundingSide roundingSide, double angle, Paint colour) {
        Polygon lineSegment;
        angle = Math.toRadians(angle);
        int multiplier = roundingSide == RoundingSide.STARBOARD ? 1 : -1;
//        System.out.println("rounding side " + roundingSide);
//        System.out.println("multiplier = " + multiplier);
//        int multiplier = 1;
        double xStart = multiplier * MARK_ARROW_SEPARATION * Math.sin(angle + Math.PI / 2);
        double yStart = multiplier * MARK_ARROW_SEPARATION * Math.cos(angle + Math.PI / 2);
        double xEnd = xStart + (ARROW_LENGTH * Math.sin(angle));
        double yEnd = yStart + (ARROW_LENGTH * Math.cos(angle));
        lineSegment = new Polygon(
                xStart, yStart,
                xEnd, yEnd
        );
        lineSegment.setStroke(colour);
        lineSegment.setFill(Color.BLUE);
        lineSegment.setStrokeWidth(STROKE_WIDTH);
        lineSegment.setStrokeLineCap(StrokeLineCap.ROUND);
        return lineSegment;
    }

    private static Polyline constructArrowHead (double rotation, Paint colour) {
        Polyline arrow = new Polyline(
            -ARROW_HEAD_WIDTH, -ARROW_HEAD_DEPTH,
            0, 0,
            ARROW_HEAD_WIDTH, -ARROW_HEAD_DEPTH
        );
        arrow.getTransforms().add(new Rotate(-rotation));
        arrow.setStrokeLineCap(StrokeLineCap.ROUND);
        arrow.setStroke(colour);
        arrow.setStrokeWidth(STROKE_WIDTH);
        return arrow;
    }
}
