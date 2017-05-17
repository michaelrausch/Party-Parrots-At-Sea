package seng302.models;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * BoatGroup is a javafx group that by default contains a graphical objects for representing a 2 dimensional boat.
 * It contains a single polygon for the boat, a group of lines to show it's path, a wake object and two text labels to
 * annotate the boat teams name and the boats velocity. The boat will update it's position onscreen everytime
 * UpdatePosition is called unless the window is minimized in which case it attempts to store animations and apply them
 * when the window is maximised.
 */
public class BoatGroup extends Group{

    //Constants for drawing
    private static final double TEAMNAME_X_OFFSET = 10d;
    private static final double TEAMNAME_Y_OFFSET = -15d;
    private static final double VELOCITY_X_OFFSET = 10d;
    private static final double VELOCITY_Y_OFFSET = -5d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Variables for boat logic.
    private boolean isStopped = true;
    private double xIncrement;
    private double yIncrement;
    private long lastTimeValid = 0;
    private long framesToMove;
    private Point2D lastPoint;
    double oldTime;
    double newTime;
    double lastYValue = 0;
    double lastXValue = 0;
    private double pixelVelocityX;
    private double pixelVelocityY;
    private static final int expectedUpdateInterval = 200;
    //Graphical objects
    private Yacht boat;
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
    public BoatGroup (Yacht boat, Color color){
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
    public BoatGroup (Yacht boat, Color color, double... points)
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
    public void moveGroupBy(double dx, double dy) {
        boatPoly.setLayoutX(boatPoly.getLayoutX() + dx);
        boatPoly.setLayoutY(boatPoly.getLayoutY() + dy);
        teamNameObject.setLayoutX(teamNameObject.getLayoutX() + dx);
        teamNameObject.setLayoutY(teamNameObject.getLayoutY() + dy);
        velocityObject.setLayoutX(velocityObject.getLayoutX() + dx);
        velocityObject.setLayoutY(velocityObject.getLayoutY() + dy);
    }


    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveTo (double x, double y, double rotation) {
        rotateTo(rotation);
        boatPoly.setLayoutX(x);
        boatPoly.setLayoutY(y);
        teamNameObject.setLayoutX(x);
        teamNameObject.setLayoutY(y);
        velocityObject.setLayoutX(x);
        velocityObject.setLayoutY(y);
    }

    public void rotateTo (double rotation) {
        boatPoly.getTransforms().setAll(new Rotate(rotation));
    }

    public void move() {
        moveGroupBy(xIncrement, yIncrement);
        framesToMove = framesToMove - 1;
        if (framesToMove <= 0){
            isStopped = true;
        }
    }

    /**
     * Sets the destination of the boat and the headng it should have once it reaches
     * @param newXValue The X co-ordinate the boat needs to move to.
     * @param newYValue The Y co-ordinate the boat needs to move to.
     * @param rotation Rotation to move graphics to.
     * @param timeValid the time the position values are valid for
     */
    public void setDestination (double newXValue, double newYValue, double rotation, double groundSpeed, long timeValid, double frameRate, long id) {
        if (lastTimeValid == 0){
            lastTimeValid = timeValid - 200;
            moveTo(newXValue, newYValue, rotation);
        }


        rotateTo(rotation);
        framesToMove = Math.round((frameRate/(1000.0f/(timeValid-lastTimeValid))));

        double dx = newXValue - boatPoly.getLayoutX();
        double dy = newYValue - boatPoly.getLayoutY();

        xIncrement = dx/framesToMove;
        yIncrement = dy/framesToMove;

        if (id == 106){
            System.out.println(framesToMove);
            System.out.println("xIncrement = " + xIncrement);
        }

        velocityObject.setText(String.format("%.2f m/s", groundSpeed));
        lastTimeValid = timeValid;
        isStopped = false;
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

    public Yacht getBoat() {
        return boat;
    }

//    /**
//     * Returns true if this BoatGroup contains at least one of the given IDs.
//     *
//     * @param raceIds The ID's to check the BoatGroup for.
//     * @return True if the BoatGroup contains at east one of the given IDs, false otherwise.
//     */
//    public boolean hasRaceId (long... raceIds) {
//        for (long id : raceIds) {
//            if (id == boat.getSourceID())
//                return true;
//        }
//        return false;
//    }

    /**
     * Returns all raceIds associated with this group. For BoatGroups the ID's are for the boat.
     *
     * @return An array containing all ID's associated with this RaceObject.
     */
    public long getRaceId() {
        return boat.getSourceID();
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

    public boolean isStopped() {
        return isStopped;
    }
}