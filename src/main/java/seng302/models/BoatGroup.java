package seng302.models;

import java.util.ArrayList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import seng302.GeometryUtils;
import seng302.controllers.CanvasController;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.SingleMark;
import seng302.models.stream.StreamParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * BoatGroup is a javafx group that by default contains a graphical objects for representing a 2
 * dimensional boat. It contains a single polygon for the boat, a group of lines to show it's path,
 * a wake object and two text labels to annotate the boat teams name and the boats velocity. The
 * boat will update it's position onscreen everytime UpdatePosition is called unless the window is
 * minimized in which case it attempts to store animations and apply them when the window is
 * maximised.
 */
public class BoatGroup extends Group {

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
    private Double lastRotation = 0.0;
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
    private Line leftLayLine;
    private Line rightLayline;
    private Double distanceTravelled = 0.0;
    private Point2D lastPoint;
    private boolean destinationSet;
    private Color textColor = Color.RED;

    private Boolean isSelected = true;  //All boats are initalised as selected

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
        this.textColor = color;
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
     * Return a text object with caching and a color applied
     *
     * @param defaultText The default text to display
     * @param fill The text fill color
     * @return The text object
     */
    private Text getTextObject(String defaultText, Color fill) {
        Text text = new Text(defaultText);

        text.setFill(fill);
        text.setCacheHint(CacheHint.SPEED);
        text.setCache(true);

        return text;
    }

    /**
     * Creates the javafx objects that will be the in the group by default.
     *
     * @param color The colour of the boat polygon and the trailing line.
     * @param points An array of co-ordinates x1,y1,x2,y2,x3,y3... that will make up the boat
     * polygon.
     */
    private void initChildren(Color color, double... points) {
        textColor = color;
        destinationSet = false;

        boatPoly = new Polygon(points);
        boatPoly.setFill(color);
        boatPoly.setOnMouseEntered(event -> boatPoly.setFill(Color.FLORALWHITE));
        boatPoly.setOnMouseExited(event -> boatPoly.setFill(color));
        boatPoly.setOnMouseClicked(event -> setIsSelected(!isSelected));
        boatPoly.setCache(true);
        boatPoly.setCacheHint(CacheHint.SPEED);

        teamNameObject = getTextObject(boat.getShortName(), textColor);
        velocityObject = getTextObject(boat.getVelocity().toString(), textColor);

        teamNameObject.setX(TEAMNAME_X_OFFSET);
        teamNameObject.setY(TEAMNAME_Y_OFFSET);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(VELOCITY_X_OFFSET);
        velocityObject.setY(VELOCITY_Y_OFFSET);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());

        updateLastMarkRoundingTime();
        updateTimeTillNextMark();

        if (estTimeToNextMarkObject != null) {
            estTimeToNextMarkObject.setX(ESTTIMETONEXTMARK_X_OFFSET);
            estTimeToNextMarkObject.setY(ESTTIMETONEXTMARK_Y_OFFSET);
            estTimeToNextMarkObject
                .relocate(estTimeToNextMarkObject.getX(), estTimeToNextMarkObject.getY());
        }

        if (legTimeObject != null) {
            legTimeObject.setX(LEGTIME_X_OFFSET);
            legTimeObject.setY(LEGTIME_Y_OFFSET);
            legTimeObject.relocate(legTimeObject.getX(), legTimeObject.getY());

        }

        leftLayLine = new Line();
        rightLayline = new Line();

        wake = new Wake(0, -BOAT_HEIGHT);
        super.getChildren()
            .addAll(teamNameObject, velocityObject, boatPoly, estTimeToNextMarkObject,
                legTimeObject, leftLayLine, rightLayline);
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
    private void moveGroupBy(double dx, double dy) {
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
        teamNameObject.setLayoutX(x);
        teamNameObject.setLayoutY(y);
        velocityObject.setLayoutX(x);
        velocityObject.setLayoutY(y);
        estTimeToNextMarkObject.setLayoutX(x);
        estTimeToNextMarkObject.setLayoutY(y);
        legTimeObject.setLayoutX(x);
        legTimeObject.setLayoutY(y);
        wake.setLayoutX(x);
        wake.setLayoutY(y);
        wake.rotate(rotation);
    }

    private void rotateTo(double rotation) {
        boatPoly.getTransforms().setAll(new Rotate(rotation));
    }

    /**
     * Updates the time until next mark label, will create a label if one doesn't exist
     */
    private void updateTimeTillNextMark() {
        if (estTimeToNextMarkObject == null) {
            estTimeToNextMarkObject = getTextObject("Next mark: -", textColor);
        }
        if (boat.getEstimateTimeAtNextMark() != null) {
            DateFormat format = new SimpleDateFormat("mm:ss");
            String timeToNextMark = format
                .format(boat.getEstimateTimeAtNextMark() - StreamParser.getCurrentTimeLong());
            estTimeToNextMarkObject.setText("Next mark: " + timeToNextMark);
        } else {
            estTimeToNextMarkObject.setText("Next mark: -");
        }
    }

    /**
     * Updates the time since last mark rounding, will create a label if one doesn't exist
     */
    private void updateLastMarkRoundingTime() {
        if (legTimeObject == null) {
            legTimeObject = getTextObject("Last mark: -", textColor);
        }

        if (boat.getMarkRoundingTime() != null) {
            DateFormat format = new SimpleDateFormat("mm:ss");
            String elapsedTime = format
                .format(StreamParser.getCurrentTimeLong() - boat.getMarkRoundingTime());
            legTimeObject.setText("Last mark: " + elapsedTime);
        } else {
            legTimeObject.setText("Last mark: -");

        }
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

        wake.updatePosition(1000 / 60);
    }

    /**
     * Calculates the rotational velocity required to reach the rotationalGoal from the
     * currentRotation.
     */
    protected Double calculateRotationalVelocity(Double rotationalGoal) {
        Double rotationalVelocity = 0.0;

        if (Math.abs(rotationalGoal - lastRotation) > 180) {
            if (rotationalGoal - lastRotation >= 0.0) {
                rotationalVelocity = ((rotationalGoal - lastRotation) - 360) / 200;
            } else {
                rotationalVelocity = (360 + (rotationalGoal - lastRotation)) / 200;
            }
        } else {
            rotationalVelocity = (rotationalGoal - lastRotation) / 200;
        }

        //Sometimes the rotation is too large to be realistic. In that case just do it instantly.
        if (Math.abs(rotationalVelocity) > 1) {
            rotationalVelocity = 0.0;
        }

        return rotationalVelocity;
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
        double groundSpeed, long timeValid, double frameRate, long id) {
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

        Double rotationalVelocity = calculateRotationalVelocity(rotation);

        updateTimeTillNextMark();
        updateLastMarkRoundingTime();

        if (Math.abs(rotationalVelocity) > 0.075) {
            rotationalVelocity = 0.0;
            wake.rotate(rotation);
        }

        rotateTo(rotation);
        wake.setRotationalVelocity(rotationalVelocity, groundSpeed);

        velocityObject.setText(String.format("%.2f m/s", groundSpeed));
        lastTimeValid = timeValid;
        isStopped = false;

        lastRotation = rotation;
    }


    /**
     * This function works out if a boat is going upwind or down wind. It looks at the boats current position, the next
     * gates position and the current wind
     * If bot the wind vector from the next gate and the boat from the next gate lay on the same side, then the boat is
     * going up wind, if they are on different sides of the gate, then the boat is going downwind
     * @param canvasController
     */
    public Boolean isUpwindLeg(CanvasController canvasController, Mark nextMark) {

        Double windAngle = StreamParser.getWindDirection();
        GateMark thisGateMark = (GateMark) nextMark;
        SingleMark nextMark1 = thisGateMark.getSingleMark1();
        SingleMark nextMark2 = thisGateMark.getSingleMark2();
        Point2D nextMarkPoint1 = canvasController.findScaledXY(nextMark1.getLatitude(), nextMark1.getLongitude());
        Point2D nextMarkPoint2 = canvasController.findScaledXY(nextMark2.getLatitude(), nextMark2.getLongitude());

        Point2D boatCurrentPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
        Point2D windTestPoint = GeometryUtils.makeArbitraryVectorPoint(nextMarkPoint1, windAngle, 10d);


        Integer boatLineFuncResult = GeometryUtils.lineFunction(nextMarkPoint1, nextMarkPoint2, boatCurrentPoint);
        Integer windLineFuncResult = GeometryUtils.lineFunction(nextMarkPoint1, nextMarkPoint2, windTestPoint);


        /*
        If both the wind vector from the gate and the boat from the gate are on the same side of that gate, then the
        boat is travelling into the wind. thus upwind. Otherwise if they are on different sides, then the boat is going
        with the wind.
         */
        if (boatLineFuncResult == windLineFuncResult) {
            return true;
        } else {
            return false;
        }

    }


    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
        setTeamNameObjectVisible(isSelected);
        setVelocityObjectVisible(isSelected);
        setLineGroupVisible(isSelected);
        setWakeVisible(isSelected);
        setEstTimeToNextMarkObjectVisible(isSelected);
        setLegTimeObjectVisible(isSelected);
        setLayLinesVisible(isSelected);
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
     * Due to javaFX limitations annotations associated with a boat that you want to appear below
     * all boats in the Z-axis need to be pulled out of the BoatGroup and added to the parent group
     * of the BoatGroups. This function returns these annotations as a group.
     *
     * @return A group containing low priority annotations.
     */
    public Group getLowPriorityAnnotations() {
        Group group = new Group();
        group.getChildren().addAll(wake, lineGroup);
        return group;
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