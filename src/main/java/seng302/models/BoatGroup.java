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
    private static double expectedUpdateInterval = 200;
    private boolean destinationSet;
    private Point2D lastPoint;
    private int wakeGenerationDelay = 10;
    private double distanceTravelled;

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
        this.boat = boat;
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
     * @param timeInterval The interval, in milliseconds, the boat should update it's position based on.
     */
    public void updatePosition (long timeInterval) {
        //Calculate the movement of the boat.
        double dx = pixelVelocityX * timeInterval;
        double dy = pixelVelocityY * timeInterval;
        double rotation = rotationalVelocity * timeInterval;
        distanceTravelled += Math.abs(dx) + Math.abs(dy);
        moveGroupBy(dx, dy, rotation);
        //Draw a new section of the trail every 20 pixels of movement.
        if (distanceTravelled > 20) {
            distanceTravelled = 0;
            if (lastPoint != null) {
                Line l = new Line(
                        lastPoint.getX(),
                        lastPoint.getY(),
                        boatPoly.getLayoutX(),
                        boatPoly.getLayoutY()
                );
                l.getStrokeDashArray().setAll(3d, 7d);
                l.setStroke(boatPoly.getFill());
                lineGroup.getChildren().add(l);
            }
            if (destinationSet){ //Only begin drawing after the first destination is set
                lastPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
            }
        }
        wake.updatePosition(timeInterval);
    }

    /**
     * Sets the destination of the boat and the headng it should have once it reaches
     * @param newXValue
     * @param newYValue
     * @param rotation Rotation to move graphics to.
     * @param raceIds RaceID of the object to move.
     */
    public void setDestination (double newXValue, double newYValue, double rotation, double speed, int... raceIds) {
        if (hasRaceId(raceIds)) {
            destinationSet = true;
            boat.setVelocity(speed);
            if (currentRotation < 0)
                currentRotation = 360 - currentRotation;
            double dx = newXValue - boatPoly.getLayoutX();
            if ((dx > 0 && pixelVelocityX < 0) || (dx < 0 && pixelVelocityX > 0)) {
                pixelVelocityX = 0;
            } else {
                pixelVelocityX = dx / expectedUpdateInterval;
            }
            double dy = newYValue - boatPoly.getLayoutY();
            //Check movement is reasonable. Assumes a 1000 * 1000 canvas
            if (Math.abs(dx) > 50 || Math.abs(dy) > 50) {
//                System.out.println("dx = " + dx);
//                System.out.println("dy = " + dy);
                dx = 0;
                dy = 0;
                moveTo(newXValue, newYValue);
            }
            //Slight delay on changing X/Y direction that could help jitter. Disabled since there was an issue with
            //packets that might be causing it.
//            if ((dx > 0 && pixelVelocityX < 0) || (dx < 0 && pixelVelocityX > 0)) {
//                pixelVelocityX = 0;
//            } else {
//                pixelVelocityX = dx / expectedUpdateInterval;
//            }
//            if ((dy > 0 && pixelVelocityY < 0) || (dy < 0 && pixelVelocityY > 0)) {
//                pixelVelocityY = 0;
//            } else {
//                pixelVelocityY = dy / expectedUpdateInterval;
//            }
            pixelVelocityX = dx / expectedUpdateInterval;
            pixelVelocityY = dy / expectedUpdateInterval;
            rotationalGoal = rotation;
            calculateRotationalVelocity();
            if (wakeGenerationDelay > 0) {
                wake.rotate(rotationalGoal);
                wakeGenerationDelay--;
            } else {
                wake.setRotationalVelocity(rotationalVelocity, currentRotation, boat.getVelocity());
            }
            velocityObject.setText(String.format("%.2f m/s", boat.getVelocity()));
        }
    }

    public void setDestination (double newXValue, double newYValue, double speed, int... raceIDs) {
        destinationSet = true;

        if (hasRaceId(raceIDs)) {
            double rotation = Math.abs(
                    Math.toDegrees(
                            Math.atan(
                                    (newYValue - boatPoly.getLayoutY()) / (newXValue - boatPoly.getLayoutX())
                            )
                    )
            );
            setDestination(newXValue, newYValue, rotation, speed, raceIDs);
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

    public void setTeamNameObjectVisible(Boolean visible) {
        teamNameObject.setVisible(visible);
    }

    public void setVelocityObjectVisible(Boolean visible) {
        velocityObject.setVisible(visible);
    }

    public void setLineGroupVisible(Boolean visible) {
        lineGroup.setVisible(visible);
    }

    public void setWakeVisible(Boolean visible) {
        wake.setVisible(visible);
    }

    public Boat getBoat() {
        return boat;
    }

    /**
     * Returns true if this BoatGroup contains at least one of the given IDs.
     *
     * @param raceIds The ID's to check the BoatGroup for.
     * @return True if the BoatGroup contains at east one of the given IDs, false otherwise.
     */
    public boolean hasRaceId (int... raceIds) {
        for (int id : raceIds) {
            if (id == boat.getId())
                return true;
        }
        return false;
    }

    /**
     * Returns all raceIds associated with this group. For BoatGroups the ID's are for the boat.
     *
     * @return An array containing all ID's associated with this RaceObject.
     */
    public int[] getRaceIds () {
        return new int[] {boat.getId()};
    }

    /**
     * Due to javaFX limitations annotations associated with a boat that you want to appear below all boats in the
     * Z-axis need to be pulled out of the BoatGroup and added to the parent group of the BoatGroups. This function
     * returns these annotations as a group.
     *
     * @return A group containing low priority annotations.
     */
    public Group getLowPriorityAnnotations () {
        Group group = new Group();
        group.getChildren().addAll(wake, lineGroup);
        return group;
    }
}
