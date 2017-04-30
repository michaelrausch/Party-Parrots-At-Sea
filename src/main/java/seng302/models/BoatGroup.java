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
 * BoatGroup is a javafx group that by default contains a graphical objects for representing a 2 dimensional boat.
 * It contains a single polygon for the boat, a group of lines to show it's path, a wake object and two text labels to
 * annotate the boat teams name and the boats velocity.
 */
public class BoatGroup extends RaceObject{

    private static final double TEAMNAME_X_OFFSET = 10d;
    private static final double TEAMNAME_Y_OFFSET = -15d;
    private static final double VELOCITY_X_OFFSET = 10d;
    private static final double VELOCITY_Y_OFFSET = -5d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    private static final int LINE_INTERVAL = 180;
    private static double expectedUpdateInterval = 200;
    private double framesForNewLine = 0;
    private boolean destinationSet;
    private Point2D lastPoint;
    private int wakeGenerationDelay;

    private Boat boat;
    private Group lineGroup = new Group();
    private Polygon boatPoly;
    private Text teamNameObject;
    private Text velocityObject;
    private Wake wake;

    /**
     * Creates a BoatGroup with the default triangular boat polygon.
     * @param boat The boat that the BoatGroup will represent. Must contain an ID which will be used to tell which
     *             BoatGroup to update.
     * @param color The colour of the boat polygon and the trailing line.
     */
    public BoatGroup (Boat boat, Color color){
        this.boat = boat;
        initChildren(color);
    }

    /**
     * Creates a BoatGroup with the boat being the default polygon. The head of the boat should be at point (0,0).
     * @param boat The boat that the BoatGroup will represent. Must contain an ID which will be used to tell which
     *             BoatGroup to update.
     * @param color The colour of the boat polygon and the trailing line.
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat polygon.
     */
    public BoatGroup (Boat boat, Color color, double... points)
    {
        initChildren(color, points);
    }

    /**
     * Creates the javafx objects that will be the in the group by default.
     * @param color The colour of the boat polygon and the trailing line.
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat polygon.
     */
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

        wake = new Wake(0, -BOAT_HEIGHT);
        wakeGenerationDelay = wake.numWakes;
        super.getChildren().addAll(teamNameObject, velocityObject, boatPoly);
    }

    /**
     * Creates the javafx objects that will be the in the group by default.
     * @param color The colour of the boat polygon and the trailing line.
     */
    private void initChildren (Color color) {
        initChildren(color,
                -BOAT_WIDTH / 2, BOAT_HEIGHT / 2,
                0.0, -BOAT_HEIGHT / 2,
                BOAT_WIDTH / 2, BOAT_HEIGHT / 2);
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
     * @param rotation The heading in degrees from north the boat should rotate to.
     */
    public void moveTo (double x, double y, double rotation) {
        rotateTo(rotation);
        moveTo(x, y);
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
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

    /**
     * Updates the position of all graphics in the BoatGroup based off of the given time interval.
     * @param timeInterval The interval, in microseconds, the boat should update it's position based on.
     */
    public void updatePosition (long timeInterval) {
        //Calculate the movement of the boat.
        double dx = pixelVelocityX * timeInterval;
        double dy = pixelVelocityY * timeInterval;
        double rotation = rotationalVelocity * timeInterval;

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
            if (lineGroup.getChildren().size() > 60)
                lineGroup.getChildren().remove(0);
        }
        wake.updatePosition(timeInterval);
    }

    public void setDestination (double newXValue, double newYValue, double rotation, int... raceIds) {
        destinationSet = true;
        boat.setVelocity(StreamParser.boatSpeeds.get((long)boat.getId()));
        if (hasRaceId(raceIds)) {
            double dx = newXValue - boatPoly.getLayoutX();
            if ((dx > 0 && pixelVelocityX < 0) || (dx < 0 && pixelVelocityX > 0)) {
                pixelVelocityX = 0;
            } else {
                pixelVelocityX = dx / expectedUpdateInterval;
            }
            double dy = newYValue - boatPoly.getLayoutY();
            if ((dy > 0 && pixelVelocityY < 0) || (dy < 0 && pixelVelocityY > 0)) {
                pixelVelocityY = 0;
            } else {
                pixelVelocityY = dy / expectedUpdateInterval;
            }
//            this.pixelVelocityX = (newXValue - boatPoly.getLayoutX()) / expectedUpdateInterval;
//            this.pixelVelocityY = (newYValue - boatPoly.getLayoutY()) / expectedUpdateInterval;
            rotationalGoal = rotation;
            calculateRotationalVelocity();
            if (Math.abs(rotationalVelocity) > 0.00003)
                rotationalVelocity = 0;
            
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
