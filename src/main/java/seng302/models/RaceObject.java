package seng302.models;

import javafx.geometry.Point2D;
import javafx.scene.Group;

/**
 * RaceObject defines the behaviour that animated objects whose position is updated from a yacht race data stream must
 * adhere to.
 */
public abstract class RaceObject extends Group {

    //Time between sections of race
    protected static double expectedUpdateInterval = 200;

    protected double rotationalGoal;
    protected double currentRotation;
    protected double rotationalVelocity;
    protected double pixelVelocityX;
    protected double pixelVelocityY;

    public Point2D getPosition () {
        return new Point2D(super.getLayoutX(), getLayoutY());
    }

    public static double getExpectedUpdateInterval() {
        return expectedUpdateInterval;
    }

    public static void setExpectedUpdateInterval(double expectedUpdateInterval) {
        RaceObject.expectedUpdateInterval = expectedUpdateInterval;
    }

    protected void calculateRotationalVelocity () {
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

    /**
     * Sets the destination of everything within the RaceObject that has an ID in the array raceIds. The destination is
     * set to the co-ordinates (x, y) with the given rotation.
     * @param x
     * @param y
     * @param rotation
     * @param raceIds
     */
    public abstract void setDestination (double x, double y, double rotation, int... raceIds);

    public abstract void setDestination (double x, double y, int... raceIds);

    public abstract void updatePosition (long timeInterval);

    public abstract void moveTo (double x, double y, double rotation);

    public abstract void moveTo (double x, double y);

    public abstract void moveGroupBy(double x, double y, double rotation);

    public abstract void rotateTo (double rotation);

    public abstract boolean hasRaceId (int... raceIds);

    public abstract int[] getRaceIds ();

    public abstract void toggleAnnotations ();
}
