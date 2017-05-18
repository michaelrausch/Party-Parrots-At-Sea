package seng302.models;

import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import seng302.models.stream.StreamParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
    private static final double TEAMNAME_Y_OFFSET = -29d;
    private static final double VELOCITY_X_OFFSET = 10d;
    private static final double VELOCITY_Y_OFFSET = -17d;
    private static final double ESTTIMETONEXTMARK_X_OFFSET = 10d;
    private static final double ESTTIMETONEXTMARK_Y_OFFSET = -5d;
    private static final double LEGTIME_X_OFFSET = 10d;
    private static final double LEGTIME_Y_OFFSET = 7d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Variables for boat logic.
    private boolean isStopped = true;
    private double xIncrement;
    private double yIncrement;
    private long lastTimeValid = 0;
    private long framesToMove;
    //Graphical objects
    private Yacht boat;
    private Group lineGroup = new Group();
    private Polygon boatPoly;
    private Text teamNameObject;
    private Text velocityObject;
    private Text estTimeToNextMarkObject;
    private Text legTimeObject;
    private Wake wake;

    private Boolean isSelected = true;  //All boats are initalised as selected

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
        boatPoly.setOnMouseEntered(event -> boatPoly.setFill(Color.FLORALWHITE));
        boatPoly.setOnMouseExited(event -> boatPoly.setFill(color));
        boatPoly.setOnMouseClicked(event -> setIsSelected(!isSelected));
        boatPoly.setCache(true);
        boatPoly.setCacheHint(CacheHint.SPEED);


        teamNameObject = new Text(boat.getShortName());
        teamNameObject.setCache(true);
        teamNameObject.setCacheHint(CacheHint.SPEED);
        velocityObject = new Text(String.valueOf(boat.getVelocity()));
        DateFormat format = new SimpleDateFormat("mm:ss");
        String timeToNextMark = format
                .format(boat.getEstimateTimeAtNextMark() - StreamParser.getCurrentTimeLong());
        estTimeToNextMarkObject = new Text("Next mark: " + timeToNextMark);
        if (boat.getMarkRoundingTime() != null) {
            String elapsedTime = format
                    .format(StreamParser.getCurrentTimeLong() - boat.getMarkRoundingTime());
            legTimeObject = new Text("Last mark: " + elapsedTime);
        } else {
            legTimeObject = new Text("Last mark: -");
        }
        velocityObject.setCache(true);
        velocityObject.setCacheHint(CacheHint.SPEED);

        teamNameObject.setX(TEAMNAME_X_OFFSET);
        teamNameObject.setY(TEAMNAME_Y_OFFSET);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(VELOCITY_X_OFFSET);
        velocityObject.setY(VELOCITY_Y_OFFSET);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());

        estTimeToNextMarkObject.setX(ESTTIMETONEXTMARK_X_OFFSET);
        estTimeToNextMarkObject.setY(ESTTIMETONEXTMARK_Y_OFFSET);
        estTimeToNextMarkObject
                .relocate(estTimeToNextMarkObject.getX(), estTimeToNextMarkObject.getY());

        legTimeObject.setX(LEGTIME_X_OFFSET);
        legTimeObject.setY(LEGTIME_Y_OFFSET);
        legTimeObject.relocate(legTimeObject.getX(), legTimeObject.getY());

        wake = new Wake(0, -BOAT_HEIGHT);
        super.getChildren()
                .addAll(teamNameObject, velocityObject, boatPoly, estTimeToNextMarkObject,
                        legTimeObject);
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
        estTimeToNextMarkObject.setLayoutX(estTimeToNextMarkObject.getLayoutX() + dx);
        estTimeToNextMarkObject.setLayoutY(estTimeToNextMarkObject.getLayoutY() + dy);
        legTimeObject.setLayoutX(legTimeObject.getLayoutX() + dx);
        legTimeObject.setLayoutY(legTimeObject.getLayoutY() + dy);
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
        estTimeToNextMarkObject.setLayoutX(x);
        estTimeToNextMarkObject.setLayoutY(y);
        legTimeObject.setLayoutX(x);
        legTimeObject.setLayoutY(y);
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
        framesToMove = Math.round((frameRate/(1000.0f/(timeValid-lastTimeValid))));
        double dx = newXValue - boatPoly.getLayoutX();
        double dy = newYValue - boatPoly.getLayoutY();
        xIncrement = dx/framesToMove;
        yIncrement = dy/framesToMove;
        rotateTo(rotation);

        velocityObject.setText(String.format("%.2f m/s", groundSpeed));
        lastTimeValid = timeValid;
        isStopped = false;
    }


    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
        setTeamNameObjectVisible(isSelected);
        setVelocityObjectVisible(isSelected);
        setLineGroupVisible(isSelected);
        setWakeVisible(isSelected);
        setEstTimeToNextMarkObjectVisible(isSelected);
        setLegTimeObjectVisible(isSelected);
    }



    public void setTeamNameObjectVisible(Boolean visible) {
        teamNameObject.setVisible(visible);
    }

    public void setVelocityObjectVisible(Boolean visible) {
        velocityObject.setVisible(visible);
    }

    public void setEstTimeToNextMarkObjectVisible(Boolean visible) {
        estTimeToNextMarkObject.setVisible(visible);
    }

    public void setLegTimeObjectVisible(Boolean visible) {
        legTimeObject.setVisible(visible);
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

    @Override
    public String toString() {
        return boat.toString();
    }
}