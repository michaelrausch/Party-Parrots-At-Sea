package seng302.models;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Created by CJIRWIN on 25/04/2017.
 */
public class BoatGroup extends Group{

    private static final double TEAMNAME_X_OFFSET = 15d;
    private static final double TEAMNAME_Y_OFFSET = -20d;
    private static final double VELOCITY_X_OFFSET = 15d;
    private static final double VELOCITY_Y_OFFSET = -10d;
    private static final double VELOCITY_WAKE_RATIO = 2d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Time between sections of race - Should be changed to 200 for actual program.
    private static double expectedUpdateInterval = 2000;

    private Boat boat;

    private double rotationalGoal;
    private double currentRotation;
    private double rotationalVelocity;
    private double pixelVelocityX;
    private double pixelVelocityY;

    public BoatGroup (Boat boat, Color color){
        super();
        this.boat = boat;
        initChildren(color);
    }

    public BoatGroup (Boat boat, Color color, double... points)
    {
        super();
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

        wake.setLayoutX(0);
        wake.setLayoutY(0);
        wake.relocate(wake.getLayoutX(), wake.getLayoutY());

        super.getChildren().addAll(boatPoly, wake, teamNameObject, velocityObject);
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
    void moveBy(Double dx, Double dy, Double rotation) {
        super.setLayoutX(super.getLayoutX() + dx);
        super.setLayoutY(super.getLayoutY() + dy);
        rotateBoat(rotation);
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveBoatTo(Double x, Double y, Double rotation) {
        super.setLayoutX(x);
        super.setLayoutY(y);
        currentRotation = 0;
        rotateBoat(rotation);
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
        moveBy(dx, dy, rotation);
    }

    public void setDestination (double newXValue, double newYValue, double rotation) {
        this.pixelVelocityX = (newXValue - super.getLayoutX()) / expectedUpdateInterval;
        this.pixelVelocityY = (newYValue - super.getLayoutY()) / expectedUpdateInterval;
        //this.destinationX = newXValue;
        //this.destinationY = newYValue;
        this.rotationalGoal = rotation;
//        if (super.getLayoutY() >= newYValue && super.getLayoutX() <= newXValue)
//            rotationalGoal = 90 - rotationalGoal;
//        else if (super.getLayoutY() < newYValue && super.getLayoutX() <= newXValue)
//            rotationalGoal = 90 + rotationalGoal;
//        else if (super.getLayoutY() >= newYValue && super.getLayoutX() > newXValue)
//            rotationalGoal = 270 + rotationalGoal;
//        else
//            rotationalGoal = 270 - rotationalGoal;
//        if (Math.abs(360 - rotationalGoal + currentRotation) < Math.abs(rotationalGoal - currentRotation)) {
//            System.out.println("ROTATE");
//            this.rotationalVelocity = (360 - rotationalGoal + currentRotation) / expectedUpdateInterval;
//        } else {
//            this.rotationalVelocity = (rotationalGoal - currentRotation) / expectedUpdateInterval;
//        }
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

    public void setDestination (double newXValue, double newYValue) {
        this.pixelVelocityX = (newXValue - super.getLayoutX()) / expectedUpdateInterval;
        this.pixelVelocityY = (newYValue - super.getLayoutY()) / expectedUpdateInterval;
        //this.destinationX = newXValue;
        //this.destinationY = newYValue;
        this.rotationalGoal = Math.abs(
                Math.toDegrees(
                        Math.atan(
                                (newYValue - super.getLayoutY()) / (newXValue - super.getLayoutX())
                        )
                )
        );
        if (super.getLayoutY() >= newYValue && super.getLayoutX() <= newXValue)
            rotationalGoal = 90 - rotationalGoal;
        else if (super.getLayoutY() < newYValue && super.getLayoutX() <= newXValue)
            rotationalGoal = 90 + rotationalGoal;
        else if (super.getLayoutY() >= newYValue && super.getLayoutX() > newXValue)
            rotationalGoal = 270 + rotationalGoal;
        else
            rotationalGoal = 270 - rotationalGoal;
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

    public void rotateBoat (double rotationDeg) {
        currentRotation += rotationDeg;
        Node boatPoly = super.getChildren().get(0);
        boatPoly.getTransforms().clear();
        boatPoly.getTransforms().add(new Rotate(currentRotation, BOAT_WIDTH/2, 0));
        Node wake = super.getChildren().get(1);
        wake.getTransforms().clear();
        wake.getTransforms().add(new Translate(0, BOAT_HEIGHT));
        wake.getTransforms().add(new Rotate(currentRotation, BOAT_WIDTH/2, -BOAT_HEIGHT));
    }

    public static double getExpectedUpdateInterval() {
        return expectedUpdateInterval;
    }

    public static void setExpectedUpdateInterval(double expectedUpdateInterval) {
        BoatGroup.expectedUpdateInterval = expectedUpdateInterval;
    }

    public void forceRotation () {
        rotateBoat (rotationalGoal - currentRotation);
    }

    public void toogleAnnotations () {
        super.getChildren().get(1).setVisible(false);
        super.getChildren().get(2).setVisible(false);
        super.getChildren().get(3).setVisible(false);
    }

    public Boat getBoat() {
        return boat;
    }
}
