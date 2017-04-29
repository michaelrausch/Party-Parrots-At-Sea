package seng302.models;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import seng302.models.parsers.StreamParser;

/**
 * Created by CJIRWIN on 25/04/2017.
 */
public class BoatGroup extends RaceObject{

    private static final double TEAMNAME_X_OFFSET = 10d;
    private static final double TEAMNAME_Y_OFFSET = -15d;
    private static final double VELOCITY_X_OFFSET = 10d;
    private static final double VELOCITY_Y_OFFSET = -5d;
    private static final double VELOCITY_WAKE_RATIO = 2d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    private static final int LINE_INTERVAL = 180;
    private static double expectedUpdateInterval = 200;
    private static int WAKE_FRAME_INTERVAL = 30;
    private double framesForNewLine = 0;
    private boolean destinationSet;
    private Point2D lastPoint;
    private int wakeGenerationDelay;

    private Boat boat;
    private int wakeCounter = WAKE_FRAME_INTERVAL;
    private Group lineGroup = new Group();
    private Group wakeGroup = new Group();
    private Polygon boatPoly;
    private Polygon wakePoly;
    private Text teamNameObject;
    private Text velocityObject;
    private Wake wake;

    public BoatGroup (Boat boat, Color color){
        this.boat = boat;
        initChildren(color);
    }

    public BoatGroup (Boat boat, Color color, double... points)
    {
        initChildren(color, points);
    }

    private void initChildren (Color color, double... points) {
        boatPoly = new Polygon(points);
        boatPoly.setFill(color);

        teamNameObject = new Text(boat.getShortName());
        velocityObject = new Text(String.valueOf(boat.getVelocity()));

        teamNameObject.setX(TEAMNAME_X_OFFSET);
        teamNameObject.setY(TEAMNAME_Y_OFFSET);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(VELOCITY_X_OFFSET);
        velocityObject.setY(VELOCITY_Y_OFFSET);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());
        destinationSet = false;

        wake = new Wake(0, 0);
        wakeGenerationDelay = wake.numWakes;
        super.getChildren().addAll(teamNameObject, velocityObject, boatPoly);
    }

    private void initChildren (Color color) {
       initChildren(color,
               -BOAT_WIDTH / 2, BOAT_HEIGHT,
               0.0, 0.0,
               BOAT_WIDTH / 2, BOAT_HEIGHT);
    }

    /**
     * Moves the boat and its children annotations from its current coordinates by specified amounts.
     * @param dx The amount to move the X coordinate by
     * @param dy The amount to move the Y coordinate by
     */
    public void moveGroupBy(double dx, double dy, double rotation) {
        boatPoly.setLayoutX(boatPoly.getLayoutX() + dx);
        boatPoly.setLayoutY(boatPoly.getLayoutY() + dy);
        teamNameObject.setLayoutX(teamNameObject.getLayoutX() + dx);
        teamNameObject.setLayoutY(teamNameObject.getLayoutY() + dy);
        velocityObject.setLayoutX(velocityObject.getLayoutX() + dx);
        velocityObject.setLayoutY(velocityObject.getLayoutY() + dy);
        wake.setLayoutX(wake.getLayoutX() + dx);
        wake.setLayoutY(wake.getLayoutY() + dy);
        rotateTo(rotation + currentRotation);
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveTo (double x, double y, double rotation) {
        rotateTo(rotation);
        moveTo(x, y);
    }

    public void moveTo (double x, double y) {
        boatPoly.setLayoutX(x);
        boatPoly.setLayoutY(y);
        teamNameObject.setLayoutX(x);
        teamNameObject.setLayoutY(y);
        velocityObject.setLayoutX(x);
        velocityObject.setLayoutY(y);
        wake.setLayoutX(x);
        wake.setLayoutY(y);
        wake.rotate(currentRotation);
    }

    public void updatePosition (long timeInterval) {
        double dx = pixelVelocityX * timeInterval;
        double dy = pixelVelocityY * timeInterval;
        double rotation = 0d;

        moveGroupBy(dx, dy, rotation);

        if (framesForNewLine-- == 0) {
            framesForNewLine = LINE_INTERVAL;
            if (lastPoint != null) {
                Line l = new Line(
                        lastPoint.getX(),
                        lastPoint.getY(),
                        boatPoly.getLayoutX(),
                        boatPoly.getLayoutY()
                );
                l.getStrokeDashArray().setAll(4d, 6d);
                l.setStroke(boatPoly.getFill());
                lineGroup.getChildren().add(l);
            }
            if (destinationSet){
                lastPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
            }
            if (lineGroup.getChildren().size() > 100)
                lineGroup.getChildren().remove(0);
        }
        wake.updatePosition(timeInterval);
    }

    public void setDestination (double newXValue, double newYValue, double rotation, int... raceIds) {
        destinationSet = true;
        boat.setVelocity(StreamParser.boatSpeeds.get((long)boat.getId()));
        velocityObject.setText(String.valueOf(boat.getVelocity()));
        if (hasRaceId(raceIds)) {
            this.pixelVelocityX = (newXValue - boatPoly.getLayoutX()) / expectedUpdateInterval;
            this.pixelVelocityY = (newYValue - boatPoly.getLayoutY()) / expectedUpdateInterval;
            this.rotationalGoal = rotation;
            calculateRotationalVelocity();
            rotateTo(rotation);
            if (wakeGenerationDelay > 0) {
                wake.rotate(rotationalGoal);
                wakeGenerationDelay--;
            } else {
                wake.setRotationalVelocity(rotationalVelocity, rotationalGoal, pixelVelocityX, pixelVelocityY);
            }
        }
    }

    public void setDestination (double newXValue, double newYValue, int... raceIDs) {
        destinationSet = true;

        if (hasRaceId(raceIDs)) {
            double rotation = Math.abs(
                    Math.toDegrees(
                            Math.atan(
                                    (newYValue - boatPoly.getLayoutY()) / (newXValue - boatPoly.getLayoutX())
                            )
                    )
            );
            setDestination(newXValue, newYValue, rotation, raceIDs);
        }
    }

    void resizeWake(){
        velocityObject.setText(String.valueOf(boat.getVelocity()));
        super.getChildren().remove(wakePoly);
        wakePoly = new Polygon(
                5.0,0.0,
                10.0, boat.getVelocity() * VELOCITY_WAKE_RATIO,
                0.0, boat.getVelocity() * VELOCITY_WAKE_RATIO
        );
        wakePoly.setLayoutX(boatPoly.getLayoutX());
        wakePoly.setLayoutY(boatPoly.getLayoutY());
        wakePoly.setFill(Color.DARKBLUE);
        super.getChildren().add(wakePoly);

    }

    public void rotateTo (double rotation) {
        currentRotation = rotation;
        boatPoly.getTransforms().clear();
        boatPoly.getTransforms().add(new Rotate(rotation));
    }



    public void forceRotation () {
        rotateTo (rotationalGoal);
        wake.rotate(rotationalGoal);
    }

    public void toggleAnnotations () {
        teamNameObject.setVisible(!teamNameObject.isVisible());
        velocityObject.setVisible(!velocityObject.isVisible());
        lineGroup.setVisible(!lineGroup.isVisible());
        wake.setVisible(!wake.isVisible());
    }

    public Boat getBoat() {
        return boat;
    }

    public boolean hasRaceId (int... raceIds) {
        for (int id : raceIds) {
            if (id == boat.getId())
                return true;
        }
        return false;
    }

    public int[] getRaceIds () {
        return new int[] {boat.getId()};
    }

    public Group getLowPriorityAnnotations () {
        Group group = new Group();
        group.getChildren().addAll(wake, lineGroup);
        return group;
    }
}
