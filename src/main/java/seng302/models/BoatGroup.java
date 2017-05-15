package seng302.models;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import seng302.controllers.RaceViewController;

import java.util.ArrayList;
import java.util.List;

/**
 * BoatGroup is a javafx group that by default contains a graphical objects for representing a 2
 * dimensional boat. It contains a single polygon for the boat, a group of lines to show it's path,
 * a wake object and two text labels to annotate the boat teams name and the boats velocity. The
 * boat will update it's position onscreen everytime UpdatePosition is called unless the window is
 * minimized in which case it attempts to store animations and apply them when the window is
 * maximised.
 */
public class BoatGroup extends RaceObject {

    //Constants for drawing
    private static final double TEAMNAME_X_OFFSET = 10d;
    private static final double TEAMNAME_Y_OFFSET = -15d;
    private static final double VELOCITY_X_OFFSET = 10d;
    private static final double VELOCITY_Y_OFFSET = -5d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Variables for boat logic.
    private Point2D lastPoint;
    private int wakeGenerationDelay = 10;
    private double distanceTravelled;
    //Graphical objects
    private Yacht boat;
    private Group lineGroup = new Group();
    private Polygon boatPoly;
    private Text teamNameObject;
    private Text velocityObject;
    private Wake wake;
    private boolean isSelected = true;  //Boats annotations are visible by default at the start
    //Handles boat moving when connecting to a stream
    private boolean setToInitialLocation = false;
    private boolean destinationSet;
    //Variables for handling minimization
    private Stage stage;
    private boolean isMaximized = true;
    private List<Line> lineStorage = new ArrayList<>();
    private int setCallCount = 5;

    /**
     * Creates a BoatGroup with the default triangular boat polygon.
     *
     * @param boat The boat that the BoatGroup will represent. Must contain an ID which will be used
     * to tell which BoatGroup to update.
     * @param color The colour of the boat polygon and the trailing line.
     */
    public BoatGroup(Yacht boat, Color color) {
        this.boat = boat;
        initChildren(color);
    }

    /**
     * Creates a BoatGroup with the boat being the default polygon. The head of the boat should be
     * at point (0,0).
     *
     * @param boat The boat that the BoatGroup will represent. Must contain an ID which will be used
     * to tell which BoatGroup to update.
     * @param color The colour of the boat polygon and the trailing line.
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat
     * polygon.
     */
    public BoatGroup(Yacht boat, Color color, double... points) {
        this.boat = boat;
        initChildren(color, points);
    }

    /**
     * Creates the javafx objects that will be the in the group by default.
     *
     * @param color The colour of the boat polygon and the trailing line.
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat
     * polygon.
     */
    private void initChildren(Color color, double... points) {
        boatPoly = new Polygon(points);
        boatPoly.setFill(color);
        boatPoly.setOnMouseEntered(event -> boatPoly.setFill(Color.FLORALWHITE));
        boatPoly.setOnMouseExited(event -> boatPoly.setFill(color));
        boatPoly.setOnMouseClicked(event -> setIsSelected(!isSelected));

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
     *
     * @param color The colour of the boat polygon and the trailing line.
     */
    private void initChildren(Color color) {
        initChildren(color,
            -BOAT_WIDTH / 2, BOAT_HEIGHT / 2,
            0.0, -BOAT_HEIGHT / 2,
            BOAT_WIDTH / 2, BOAT_HEIGHT / 2);
    }

    /**
     * Moves the boat and its children annotations from its current coordinates by specified
     * amounts.
     *
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
     *
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     * @param rotation The heading in degrees from north the boat should rotate to.
     */
    public void moveTo(double x, double y, double rotation) {
        rotateTo(rotation);
        moveTo(x, y);
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     *
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveTo(double x, double y) {
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
     *
     * @param timeInterval The interval, in milliseconds, the boat should update it's position based
     * on.
     */
    public void updatePosition(long timeInterval) {
        //Calculate the movement of the boat.
        if (isMaximized) {
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
                    l.setStroke(boat.getColour());
                    lineGroup.getChildren().add(l);
                }
                if (destinationSet) { //Only begin drawing after the first destination is set
                    lastPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
                }
            }
            wake.updatePosition(timeInterval);
        }
    }

    /**
     * Sets the destination of the boat and the headng it should have once it reaches
     *
     * @param newXValue The X co-ordinate the boat needs to move to.
     * @param newYValue The Y co-ordinate the boat needs to move to.
     * @param rotation Rotation to move graphics to.
     * @param raceIds RaceID of the object to move.
     */
    public void setDestination(double newXValue, double newYValue, double rotation,
        double groundSpeed, int... raceIds) {
        if (hasRaceId(raceIds)) {
            if (setToInitialLocation) {
                destinationSet = true;
                boat.setVelocity(groundSpeed);
                if (currentRotation < 0) {
                    currentRotation = 360 - currentRotation;
                }
                double dx = newXValue - boatPoly.getLayoutX();
                double dy = newYValue - boatPoly.getLayoutY();
                //Check movement is reasonable. Assumes a 1000 * 1000 canvas
                if (Math.abs(dx) > 50 || Math.abs(dy) > 50) {
                    dx = 0;
                    dy = 0;
                    moveTo(newXValue, newYValue);
                }

                pixelVelocityX = dx / expectedUpdateInterval;
                pixelVelocityY = dy / expectedUpdateInterval;
                rotationalGoal = rotation;
                calculateRotationalVelocity();

                if (wakeGenerationDelay > 0) {
                    wake.rotate(rotationalGoal);
                    rotateTo(rotationalGoal); //Need to test with this removed.
                    rotationalVelocity = 0;
                    wakeGenerationDelay--;
                } else {
                    wake.setRotationalVelocity(rotationalVelocity, rotationalGoal,
                        boat.getVelocity());
                }
                velocityObject.setText(String.format("%.2f m/s", boat.getVelocity()));
            } else {
                setToInitialLocation = true;
                rotationalGoal = rotation;
                moveTo(newXValue, newYValue, rotation);
            }
        }
        //If minimized generate lines every 5 calls to set destination.
        if (!isMaximized) {
            setToInitialLocation = false;
            wakeGenerationDelay = 2;
            if (setCallCount-- == 0) {
                setCallCount = 5;
                if (lastPoint != null) {
                    Line l = new Line(
                        lastPoint.getX(),
                        lastPoint.getY(),
                        newXValue,
                        newYValue
                    );
                    l.getStrokeDashArray().setAll(3d, 7d);
                    l.setStroke(boatPoly.getFill());
                    lineStorage.add(l);
                }
                if (destinationSet) { //Only begin drawing after the first destination is set
                    lastPoint = new Point2D(newXValue, newYValue);
                }
            }
        }
    }

    public void setDestination(double newXValue, double newYValue, double groundSpeed,
        int... raceIDs) {
        destinationSet = true;

        if (hasRaceId(raceIDs)) {
            double rotation = Math.abs(
                Math.toDegrees(
                    Math.atan(
                        (newYValue - boatPoly.getLayoutY()) / (newXValue - boatPoly.getLayoutX())
                    )
                )
            );
            setDestination(newXValue, newYValue, rotation, groundSpeed, raceIDs);
        }
    }

    public void rotateTo(double rotation) {
        currentRotation = rotation;
        boatPoly.getTransforms().setAll(new Rotate(rotation));
    }

    public void forceRotation() {
        rotateTo(rotationalGoal);
        wake.rotate(rotationalGoal);
    }

    public void paintBoat(Color color) {
        boatPoly.setFill(color);
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

    /**
     * This function sets the boats isSelected property AS WELL as actually acting upon the value of
     * that selection. (Painting or not painting annotations)
     *
     * @param isSelected A Boolean indicating whether or not the boat is selected
     */
    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
        setTeamNameObjectVisible(isSelected);
        setVelocityObjectVisible(isSelected);
        setLineGroupVisible(isSelected);
        setWakeVisible(isSelected);
        paintBoat((isSelected) ? Color.WHITE : boat.getColour());
    }

    /**
     * Returns true if this BoatGroup contains at least one of the given IDs.
     *
     * @param raceIds The ID's to check the BoatGroup for.
     * @return True if the BoatGroup contains at east one of the given IDs, false otherwise.
     */
    public boolean hasRaceId(int... raceIds) {
        for (int id : raceIds) {
            if (id == boat.getSourceID()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all raceIds associated with this group. For BoatGroups the ID's are for the boat.
     *
     * @return An array containing all ID's associated with this RaceObject.
     */
    public int[] getRaceIds() {
        return new int[]{boat.getSourceID()};
    }

    /**
     * Due to javaFX limitations annotations associated with a boat that you want to appear below
     * all boats in the Z-axis need to be pulled out of the BoatGroup and added to the parent group
     * of the BoatGroups. This function returns these annotations as a group.
     *
     * @return A group containing low priority annotations.
     */
    public Group getLowPriorityAnnotations() {
        Group group = new Group();
        group.getChildren().addAll(wake, lineGroup, teamNameObject, velocityObject);
        return group;
    }

    /**
     * Use this function to let the BoatGroup know about the stage it is in. If it knows about it's
     * stage then it will listen to the iconified property of that stage and change it's behaviour
     * upon minimization. Without setting the Stage there is guarantee that the BoatGroup will draw
     * properly when the stage is minimized.
     *
     * @param stage The stage that the BoatGroup is added to.
     */
    public void setStage(Stage stage) {
        /* TODO: 4/05/17 cir27 - Find a way to get the stage to this point. Need to pass it through multiple controllers.
                                 App.start() -> Controller.setContentPane -> RaceViewController -> CanvasController
         */
        this.stage = stage;
        this.stage.iconifiedProperty().addListener(e -> {
            isMaximized = !stage.isIconified();
            if (!lineStorage.isEmpty()) {
                lineGroup.getChildren().addAll(lineStorage);
                lineStorage.clear();
            }
        });
    }

    @Override
    public String toString() {
        return boat.toString();
    }
}
