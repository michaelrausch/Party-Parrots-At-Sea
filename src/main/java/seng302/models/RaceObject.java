package seng302.models;

import javafx.geometry.Point2D;
import javafx.scene.Group;

/**
 * Created by CJIRWIN on 26/04/2017.
 */
public abstract class RaceObject extends Group {

    double rotationalGoal;
    double currentRotation;
    double rotationalVelocity;
    double pixelVelocityX;
    double pixelVelocityY;

    public boolean isSamePos (Point2D point) {
        return point.getX() == super.getLayoutX() && point.getY() == super.getLayoutY();
    }

    public Point2D getPosition () {
        return new Point2D(super.getLayoutX(), getLayoutY());
    }
    public abstract void setDestination (double x, double y, double rotation);
    public abstract void setDestination (double x, double y);
    public abstract void updatePosition (double timeInterval);
    public abstract void moveTo (double x, double y, double rotation);
    public abstract void moveTo (double x, double y);
    public abstract boolean hasRaceId (int... raceIds);
    public abstract void toggleAnnotations ();

}
