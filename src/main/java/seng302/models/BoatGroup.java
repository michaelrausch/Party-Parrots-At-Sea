package seng302.models;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Created by CJIRWIN on 25/04/2017.
 */
public class BoatGroup extends RaceObject{

    private static final double TEAMNAME_X_OFFSET = 15d;
    private static final double TEAMNAME_Y_OFFSET = -20d;
    private static final double VELOCITY_X_OFFSET = 15d;
    private static final double VELOCITY_Y_OFFSET = -10d;
    private static final double VELOCITY_WAKE_RATIO = 2d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Time between sections of race - Should be changed to 200 for actual program.
    private static double expectedUpdateInterval = 200;
    private static int WAKE_FRAME_INTERVAL = 40;

    private Boat boat;
    private int wakeCounter = WAKE_FRAME_INTERVAL;

    public BoatGroup (Boat boat, Color color){
        this.boat = boat;
        initChildren(color);
    }

    public BoatGroup (Boat boat, Color color, double... points)
    {
        initChildren(color, points);
    }

    private void initChildren (Color color, double... points) {
        Polygon boatPoly = new Polygon(points);
        boatPoly.setFill(color);

        Polygon wake = new Polygon(
                5.0,0.0,
                10.0, boat.getVelocity() * VELOCITY_WAKE_RATIO,
                0.0, boat.getVelocity() * VELOCITY_WAKE_RATIO
        );
        wake.setFill(Color.DARKBLUE);

        Text teamNameObject = new Text(boat.getShortName());
        Text velocityObject = new Text(String.valueOf(boat.getVelocity()));

        boatPoly.setLayoutX(0);
        boatPoly.setLayoutY(0);
        boatPoly.relocate(boatPoly.getLayoutX(), boatPoly.getLayoutY());

        teamNameObject.setX(TEAMNAME_X_OFFSET);
        teamNameObject.setY(TEAMNAME_Y_OFFSET);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(VELOCITY_X_OFFSET);
        velocityObject.setY(VELOCITY_Y_OFFSET);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());

        super.getChildren().addAll(boatPoly, teamNameObject, velocityObject);
    }

    private void initChildren (Color color) {
       initChildren(color,
               BOAT_WIDTH / 2, 0.0,
               BOAT_WIDTH, BOAT_HEIGHT,
               0.0, BOAT_HEIGHT);
    }
    /**
     * Moves the boat and its children annotations from its current coordinates by specified amounts.
     * @param dx The amount to move the X coordinate by
     * @param dy The amount to move the Y coordinate by
     */
    public void moveGroupBy(double dx, double dy, double rotation) {
        super.setLayoutX(super.getLayoutX() + dx);
        super.setLayoutY(super.getLayoutY() + dy);
        rotateTo(currentRotation + rotation);
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
        super.setLayoutX(x);
        super.setLayoutY(y);
    }

    public void updatePosition (double timeInterval) {
        double dx = pixelVelocityX * timeInterval;
        double dy = pixelVelocityY * timeInterval;
        double rotation = 0d;
        if (rotationalGoal > currentRotation && rotationalVelocity > 0) {
            rotation = rotationalVelocity * timeInterval;
        } else if (rotationalGoal < currentRotation && rotationalVelocity < 0) {
            rotation = rotationalVelocity * timeInterval;
        }
        moveGroupBy(dx, dy, rotation);
        for (Node wake : super.getChildren().subList(4, super.getChildren().size())) {
            if (!((Wake) wake).updatePosition(timeInterval))
                super.getChildren().remove(wake);
        }
        if (wakeCounter-- == 0) {
            wakeCounter = WAKE_FRAME_INTERVAL;
            super.getChildren().add(
                    new Wake(
                            super.getLayoutX(), super.getLayoutY(), pixelVelocityX, pixelVelocityY
                    )
            );
        }
    }

    public void setDestination (double newXValue, double newYValue, double rotation, int... raceIds) {
        if (hasRaceId(raceIds)) {
            this.pixelVelocityX = (newXValue - super.getLayoutX()) / expectedUpdateInterval;
            this.pixelVelocityY = (newYValue - super.getLayoutY()) / expectedUpdateInterval;
            this.rotationalGoal = rotation;
            if (Math.abs(rotationalGoal - currentRotation) > 180) {
                if (rotationalGoal - currentRotation >= 0) {
                    this.rotationalVelocity = ((rotationalGoal - currentRotation) - 360) / expectedUpdateInterval;
                } else {
                    this.rotationalVelocity = (360 + (rotationalGoal - currentRotation)) / expectedUpdateInterval;
                }
            } else {
                this.rotationalVelocity = (rotationalGoal - currentRotation) / expectedUpdateInterval;
            }
        }
    }

    public void setDestination (double newXValue, double newYValue, int... raceIDs) {
        if (hasRaceId(raceIDs)) {
            double rotation = Math.abs(
                    Math.toDegrees(
                            Math.atan(
                                    (newYValue - super.getLayoutY()) / (newXValue - super.getLayoutX())
                            )
                    )
            );
            if (super.getLayoutY() >= newYValue && super.getLayoutX() <= newXValue)
                rotation = 90 - rotation;
            else if (super.getLayoutY() < newYValue && super.getLayoutX() <= newXValue)
                rotation = 90 + rotation;
            else if (super.getLayoutY() >= newYValue && super.getLayoutX() > newXValue)
                rotation = 270 + rotation;
            else
                rotation = 270 - rotation;
            setDestination(newXValue, newYValue, rotation, raceIDs);
        }
    }

    public void rotateTo (double rotation) {
        Node boatPoly = super.getChildren().get(0);
        boatPoly.getTransforms().clear();
        boatPoly.getTransforms().add(new Rotate(rotation, BOAT_WIDTH/2, 0));
        Node wake = super.getChildren().get(1);
        wake.getTransforms().clear();
        wake.getTransforms().add(new Translate(0, BOAT_HEIGHT));
        wake.getTransforms().add(new Rotate(rotation, BOAT_WIDTH/2, -BOAT_HEIGHT));
    }

    public void forceRotation () {
        rotateTo (rotationalGoal);
    }

    public void toggleAnnotations () {
        for (Node node : super.getChildren().subList(1, super.getChildren().size())) {
            node.setVisible(false);
        }
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
}
