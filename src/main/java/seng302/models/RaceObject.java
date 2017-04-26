package seng302.models;

import javafx.geometry.Point2D;
import javafx.scene.Group;

/**
 * Created by CJIRWIN on 26/04/2017.
 */
public abstract class RaceObject extends Group {

    //Time between sections of race - Should be changed to 200 for actual program.
    protected static double expectedUpdateInterval = 2000;

    protected double rotationalGoal;
    protected double currentRotation;
    protected double rotationalVelocity;
    protected double pixelVelocityX;
    protected double pixelVelocityY;

    public boolean isSamePos (Point2D point) {
        return point.getX() == super.getLayoutX() && point.getY() == super.getLayoutY();
    }

    public Point2D getPosition () {
        return new Point2D(super.getLayoutX(), getLayoutY());
    }

    public static double getExpectedUpdateInterval() {
        return expectedUpdateInterval;
    }

    public static void setExpectedUpdateInterval(double expectedUpdateInterval) {
        RaceObject.expectedUpdateInterval = expectedUpdateInterval;
    }

    public abstract void setDestination (double x, double y, double rotation, int... raceIds);

    public abstract void setDestination (double x, double y, int... raceIds);

    public abstract void updatePosition (double timeInterval);

    public abstract void moveTo (double x, double y, double rotation);

    public abstract void moveTo (double x, double y);

    public abstract void moveGroupBy(double x, double y, double rotation);

    public abstract void rotateTo (double rotation);

    public abstract boolean hasRaceId (int... raceIds);

    public abstract int[] getRaceIds ();

    public abstract void toggleAnnotations ();

}
