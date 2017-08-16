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
        if (roundingSide == RoundingSide.PORT && angleOfEntry < angleOfExit && Math.abs(angleOfExit - angleOfEntry) < 180) {
            return makeInteriorAngle(roundingSide, angleOfExit, angleOfEntry, colour);
        } else if (roundingSide == RoundingSide.STARBOARD && angleOfEntry > angleOfExit && -Math.abs(angleOfEntry - angleOfExit) > -180) {
            return makeInteriorAngle(roundingSide, angleOfExit, angleOfEntry, colour);
        }

        angleOfEntry = 180 - angleOfEntry;
        Group arrow = new Group();
        Group exitSection = constructExitArrow(roundingSide, angleOfExit, colour);
        angleOfExit = 180 - angleOfExit;
        Arc roundSection = new Arc(
            0, 0, MARK_ARROW_SEPARATION, MARK_ARROW_SEPARATION,
            (roundingSide == RoundingSide.PORT ? -180 : 0) + angleOfEntry,
            roundingSide == RoundingSide.PORT ? Math.abs(angleOfExit - angleOfEntry) : -Math.abs(angleOfEntry - angleOfExit)
        );
        roundSection.setStrokeWidth(STROKE_WIDTH);
        roundSection.setType(ArcType.OPEN);
        roundSection.setStroke(colour);
        roundSection.setFill(new Color(0,0,0,0));
        Polygon entrySection = constructLineSegment(
                roundingSide == RoundingSide.PORT ? RoundingSide.STARBOARD : RoundingSide.PORT, 180 + angleOfEntry, colour
        );
        arrow.getChildren().addAll(exitSection, roundSection, entrySection);
        return arrow;
    }

    private static Group makeInteriorAngle (RoundingSide roundingSide, double angleOfExit, double angleOfEntry, Paint colour) {
        Group arrow = new Group();
        Polygon lineSegment;
        angleOfEntry = Math.toRadians(360 - angleOfEntry);
        angleOfExit = Math.toRadians(180 - angleOfExit);
        int multiplier = roundingSide == RoundingSide.STARBOARD ? -1 : 1;
        double xStart = multiplier * MARK_ARROW_SEPARATION * Math.sin(angleOfEntry + Math.PI / 2);
        double yStart = multiplier * MARK_ARROW_SEPARATION * Math.cos(angleOfEntry + Math.PI / 2);
        xStart = xStart + (ARROW_LENGTH * Math.sin(angleOfEntry));
        yStart = yStart + (ARROW_LENGTH * Math.cos(angleOfEntry));
        multiplier = roundingSide == RoundingSide.STARBOARD ? 1 : -1;
        double xEnd = multiplier * MARK_ARROW_SEPARATION * Math.sin(angleOfExit + Math.PI / 2);
        double yEnd = multiplier * MARK_ARROW_SEPARATION * Math.cos(angleOfExit + Math.PI / 2);
        xEnd = xEnd + (ARROW_LENGTH * Math.sin(angleOfExit));
        yEnd = yEnd + (ARROW_LENGTH * Math.cos(angleOfExit));
        lineSegment = new Polygon(
            xStart, yStart,
            xEnd, yEnd
        );
        lineSegment.setStroke(colour);
        lineSegment.setFill(Color.BLUE);
        lineSegment.setStrokeWidth(STROKE_WIDTH);
        lineSegment.setStrokeLineCap(StrokeLineCap.ROUND);
        Polyline arrowHead = constructArrowHead(
            90 + Math.toDegrees(Math.atan2(yStart - yEnd, xEnd - xStart)),
            colour
        );
        arrowHead.setLayoutX(xEnd);
        arrowHead.setLayoutY(yEnd);
        arrow.getChildren().addAll(lineSegment, arrowHead);
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
