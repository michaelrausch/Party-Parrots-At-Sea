package seng302.visualiser.fxObjects;

import java.util.ArrayList;

import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import seng302.model.Boat;
import seng302.utilities.GeoUtility;
import seng302.model.mark.GateMark;
import seng302.model.mark.Mark;
import seng302.model.mark.SingleMark;
import seng302.model.stream.parsers.StreamParser;

/**
 * BoatGroup is a javafx group that by default contains a graphical objects for representing a 2
 * dimensional boat. It contains a single polygon for the boat, a group of lines to show it's path,
 * a wake object and two text labels to annotate the boat teams name and the boats velocity. The
 * boat will update it's position onscreen everytime UpdatePosition is called unless the window is
 * minimized in which case it attempts to store animations and apply them when the window is
 * maximised.
 */
public class BoatObject extends Group {

    //Constants for drawing
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Variables for boat logic.
    private boolean isStopped = true;
    private double xIncrement;
    private double yIncrement;
    private long lastTimeValid = 0;
    private Double lastRotation = 0.0;
    private long framesToMove;
    //Graphical objects
    private Boat boat;
    private Group lineGroup = new Group();
    private Polygon boatPoly;
    private Wake wake;
    private Line leftLayLine;
    private Line rightLayline;
    private Double distanceTravelled = 0.0;
    private Point2D lastPoint;
    private boolean destinationSet;
    private AnnotationBox annotationBox;

    private Paint colour = Color.BLACK;

    private Boolean isSelected = true;  //All boats are initialised as selected

    /**
     * Creates a BoatGroup with the default triangular boat polygon.
     */
    public BoatObject() {
        this(-BOAT_WIDTH / 2, BOAT_HEIGHT / 2,
            0.0, -BOAT_HEIGHT / 2,
            BOAT_WIDTH / 2, BOAT_HEIGHT / 2);
    }

    /**
     * Creates a BoatGroup with the boat being the default polygon. The head of the boat should be
     * at point (0,0).
     *
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat
     * polygon.
     */
    public BoatObject(double... points) {
        boatPoly = new Polygon(points);
        boatPoly.setFill(colour);
        boatPoly.setOnMouseEntered(event -> {
            boatPoly.setFill(Color.FLORALWHITE);
            boatPoly.setStroke(Color.RED);
        });
        boatPoly.setOnMouseExited(event -> {
            boatPoly.setFill(colour);
            boatPoly.setStroke(Color.BLACK);
        });
        boatPoly.setOnMouseClicked(event -> setIsSelected(!isSelected));
        boatPoly.setCache(true);
        boatPoly.setCacheHint(CacheHint.SPEED);

        annotationBox = new AnnotationBox();
        annotationBox.setFill(colour);

        leftLayLine = new Line();
        rightLayline = new Line();

        wake = new Wake(0, -BOAT_HEIGHT);
        super.getChildren().addAll(boatPoly, annotationBox);
    }

    public void setFill (Paint value) {
        this.colour = value;
        boatPoly.setFill(colour);
        annotationBox.setFill(colour);
    }

    /**
     * Moves the boat and its children annotations from its current coordinates by specified
     * amounts.
     *
     * @param dx The amount to move the X coordinate by
     * @param dy The amount to move the Y coordinate by
     */
    private void moveGroupBy(double dx, double dy) {
        boatPoly.setLayoutX(boatPoly.getLayoutX() + dx);
        boatPoly.setLayoutY(boatPoly.getLayoutY() + dy);
        annotationBox.setLayoutX(annotationBox.getLayoutX() + dx);
        annotationBox.setLayoutY(annotationBox.getLayoutY() + dy);
        wake.setLayoutX(wake.getLayoutX() + dx);
        wake.setLayoutY(wake.getLayoutY() + dy);
    }


    /**
     * Moves the boat and its children annotations to coordinates specified
     *
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    private void moveTo(double x, double y, double rotation) {
        rotateTo(rotation);
        boatPoly.setLayoutX(x);
        boatPoly.setLayoutY(y);
        annotationBox.setLayoutX(x);
        annotationBox.setLayoutY(y);
        wake.setLayoutX(x);
        wake.setLayoutY(y);
        wake.rotate(rotation);
    }

    private void rotateTo(double rotation) {
        boatPoly.getTransforms().setAll(new Rotate(rotation));
    }

    public void move() {
        double dx = xIncrement * framesToMove;
        double dy = yIncrement * framesToMove;

        distanceTravelled += Math.abs(dx) + Math.abs(dy);
        moveGroupBy(xIncrement, yIncrement);
        framesToMove = framesToMove - 1;

        if (framesToMove <= 0) {
            isStopped = true;
        }

        if (distanceTravelled > 70) {
            distanceTravelled = 0d;

            if (lastPoint != null) {
                Line l = new Line(
                    lastPoint.getX(),
                    lastPoint.getY(),
                    boatPoly.getLayoutX(),
                    boatPoly.getLayoutY()
                );
                l.getStrokeDashArray().setAll(3d, 7d);
                l.setStroke(boat.getColour());
                l.setCache(true);
                l.setCacheHint(CacheHint.SPEED);
                lineGroup.getChildren().add(l);
            }

            if (destinationSet) {
                lastPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
            }
        }
        wake.updatePosition();
    }

    /**
     * Sets the destination of the boat and the headng it should have once it reaches
     *
     * @param newXValue The X co-ordinate the boat needs to move to.
     * @param newYValue The Y co-ordinate the boat needs to move to.
     * @param rotation Rotation to move graphics to.
     * @param timeValid the time the position values are valid for
     */
    public void setDestination(double newXValue, double newYValue, double rotation,
        double groundSpeed, long timeValid, double frameRate) {
        if (lastTimeValid == 0) {
            lastTimeValid = timeValid - 200;
            moveTo(newXValue, newYValue, rotation);
        }
        framesToMove = Math.round((frameRate / (1000.0f / (timeValid - lastTimeValid))));
        double dx = newXValue - boatPoly.getLayoutX();
        double dy = newYValue - boatPoly.getLayoutY();

        xIncrement = dx / framesToMove;
        yIncrement = dy / framesToMove;

        destinationSet = true;

        rotateTo(rotation);
        wake.setRotation(rotation, groundSpeed);
        boat.setVelocity(groundSpeed);
        lastTimeValid = timeValid;
        isStopped = false;
        lastRotation = rotation;
    }


//    /**
//     * This function works out if a boat is going upwind or down wind. It looks at the boats current position, the next
//     * gates position and the current wind
//     * If bot the wind vector from the next gate and the boat from the next gate lay on the same side, then the boat is
//     * going up wind, if they are on different sides of the gate, then the boat is going downwind
//     * @param canvasController
//     */
//    public Boolean isUpwindLeg(GameViewController canvasController, Mark nextMark) {
//
//        Double windAngle = StreamParser.getWindDirection();
//        GateMark thisGateMark = (GateMark) nextMark;
//        SingleMark nextMark1 = thisGateMark.getSingleMark1();
//        SingleMark nextMark2 = thisGateMark.getSingleMark2();
//        Point2D nextMarkPoint1 = canvasController.findScaledXY(nextMark1.getLatitude(), nextMark1.getLongitude());
//        Point2D nextMarkPoint2 = canvasController.findScaledXY(nextMark2.getLatitude(), nextMark2.getLongitude());
//
//        Point2D boatCurrentPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
//        Point2D windTestPoint = GeoUtility.makeArbitraryVectorPoint(nextMarkPoint1, windAngle, 10d);
//
//
//        Integer boatLineFuncResult = GeoUtility.lineFunction(nextMarkPoint1, nextMarkPoint2, boatCurrentPoint);
//        Integer windLineFuncResult = GeoUtility.lineFunction(nextMarkPoint1, nextMarkPoint2, windTestPoint);
//
//
//        /*
//        If both the wind vector from the gate and the boat from the gate are on the same side of that gate, then the
//        boat is travelling into the wind. thus upwind. Otherwise if they are on different sides, then the boat is going
//        with the wind.
//         */
//        return boatLineFuncResult.equals(windLineFuncResult);
//        return true;
//    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
        setLineGroupVisible(isSelected);
        setWakeVisible(isSelected);
        annotationBox.setVisible(isSelected);
        setLayLinesVisible(isSelected);
    }

    public void setVisibility (boolean teamName, boolean velocity, boolean estTime, boolean legTime,
        boolean trail, boolean wake) {
        this.wake.setVisible(wake);
        this.lineGroup.setVisible(trail);
    }

    public void setLineGroupVisible(Boolean visible) {
        lineGroup.setVisible(visible);
    }

    public void setWakeVisible(Boolean visible) {
        wake.setVisible(visible);
    }

    public void setLayLinesVisible(Boolean visible) {
        leftLayLine.setVisible(visible);
        rightLayline.setVisible(visible);
    }

    public void setLaylines(Line line1, Line line2) {
        this.leftLayLine = line1;
        this.rightLayline = line2;
    }

    public ArrayList<Line> getLaylines() {
        ArrayList<Line> laylines = new ArrayList<>();
        laylines.add(leftLayLine);
        laylines.add(rightLayline);
        return laylines;
    }

    public Boat getBoat() {
        return boat;
    }

    /**
     * Returns all raceIds associated with this group. For BoatGroups the ID's are for the boat.
     *
     * @return An array containing all ID's associated with this RaceObject.
     */
    public long getRaceId() {
        return boat.getSourceID();
    }

    public Group getWake () {
        return wake;
    }

    public Group getTrail() {
        return lineGroup;
    }

    public Group getAnnotations() {
        return annotationBox;
    }

    public Double getBoatLayoutX() {
        return boatPoly.getLayoutX();
    }


    public Double getBoatLayoutY() {
        return boatPoly.getLayoutY();
    }

    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public String toString() {
        return boat.toString();
    }

}