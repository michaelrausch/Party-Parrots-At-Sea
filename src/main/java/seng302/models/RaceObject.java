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

    /**
     *
     */
    public static void setExpectedUpdateInterval(double expectedUpdateInterval) {
        RaceObject.expectedUpdateInterval = expectedUpdateInterval;
    }

    /**
     * Calculates the rotational velocity required to reach the rotationalGoal from the currentRotation.
     */
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
        //Sometimes the rotation is too large to be realistic. In that case just do it instantly.
        if (Math.abs(rotationalVelocity) > 1) {
            rotationalVelocity = 0;
            rotateTo(rotationalGoal);
        }
    }

    /**
     * Sets the destination of everything within the RaceObject that has an ID in the array raceIds. The destination is
     * set to the co-ordinates (x, y) with the given rotation.
     * @param x X co-ordinate to move the graphics to.
     * @param y Y co-ordinate to move the graphics to.
     * @param rotation Rotation to move graphics to.
     * @param raceIds RaceID of the object to move.
     */
    public abstract void setDestination (double x, double y, double rotation, double speed, int... raceIds);
    /**
     * Sets the destination of everything within the RaceObject that has an ID in the array raceIds. The destination is
     * set to the co-ordinates (x, y).
     * @param x X co-ordinate to move the graphic to.
     * @param y Y co-ordinate to move the graphic to.
     * @param raceIds RaceID to the object to move.
     */
    public abstract void setDestination (double x, double y, double speed, int... raceIds);

    public abstract void updatePosition (long timeInterval);

    public abstract void moveTo (double x, double y, double rotation);

    public abstract void moveTo (double x, double y);

    public abstract void moveGroupBy(double x, double y, double rotation);

    public abstract void rotateTo (double rotation);

    public abstract boolean hasRaceId (int... raceIds);

    public abstract int[] getRaceIds ();
}
