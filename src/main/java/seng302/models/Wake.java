package seng302.models;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.transform.Rotate;

/**
 * By default wake is a group containing 5 arcs. Each arc starts from the same point. Each arc is larger and more
 * transparent than the last. On calling updatePositions() arcs rotate at velocities given by setRotationalVelocity().
 * The larger and more transparent an arc is the longer the delay before it rotates at the latest velocity. It is
 * assumed that rotationalVelocities() are set regularly as wakes do not stop rotating and an array of velocities needs
 * to be populated for the class to work as expected.
 */
class Wake extends Group {
    private final double OPACITY_INCREASE = -0.10;
    private final double RADIUS_INCREASE = 10;
    final int numWakes = 5;
    private double[] velocities = new double[numWakes * 3];
    private Arc[] arcs = new Arc[numWakes];
    private double[] rotations = new double[numWakes];
    private int velocitiesIndex = 0;
    private double sum = 0;

    /**
     * Create a wake at the given location.
     * @param startingX x location where the tip of wake arcs will be.
     * @param startingY y location where the tip of wake arcs will be.
     */
    Wake(double startingX, double startingY) {
        super.setLayoutX(startingX);
        super.setLayoutY(startingY);
        Arc arc;
        for (int i = 0; i < numWakes; i++) {
             arc = new Arc(0,0,0,0,-110,40);
             arc.setFill(new Color(0.18, 0.7, 1.0, 0.50 + OPACITY_INCREASE * i));
             arc.setType(ArcType.ROUND);
             arcs[i] = arc;
        }
        super.getChildren().addAll(arcs);
    }

    /**
     * Sets the rotationalVelocity of each arc. Each arc is 3 velocities behind the next smallest arc. The smallest uses
     * the latest given velocity.
     * @param rotationalVelocity The rotationalVelocity the wake should move at.
     */
    void setRotationalVelocity (double rotationalVelocity, double rotationGoal, double velocityX, double velocityY) {
        sum -= Math.abs(velocities[velocitiesIndex]);
        sum += Math.abs(rotationalVelocity);
        if (sum < 0.0001)
            rotate (rotationGoal); //In relatively straight segments the wake snaps to match the boats current position.
                                   //This stops the wake from eventually becoming out of sync with the boat.
        velocitiesIndex = (velocitiesIndex + 1) % 14;
        velocities[velocitiesIndex] = rotationalVelocity;

        double scaleFactor = Math.abs(Math.log10(Math.abs(velocityX) + Math.abs(velocityY)));
        double baseRad = 30;
        for (Arc arc :arcs) {
            double rad = baseRad + 5 * scaleFactor;
            arc.setRadiusX(rad);
            arc.setRadiusY(rad);
            baseRad += RADIUS_INCREASE;
        }
    }

    /**
     * Arcs rotate based on the distance they would have travelled over the supplied time interval.
     * @param timeInterval the time interval, in microseconds, that the wake should move.
     */
    void updatePosition (long timeInterval) {
        int temp = velocitiesIndex;
        for (int i = 0; i < arcs.length; i++) {
            //temp = ((temp + 3) % 14);
            rotations[i] = rotations[i] + velocities[temp] * timeInterval;
            int j = 0;
            //I have no idea why I have to do this to make it work.
            //I will buy you a block of chocolate if you can tell me why.
            switch (i) {
                case 4:
                    j = 1; break;
                case 3:
                    j = 2; break;
                case 2:
                    j = 3; break;
                case 1:
                    j = 4; break;
            }
            arcs[j].getTransforms().setAll(new Rotate(rotations[i]));
            temp = ((temp + 3) % 14);
        }
    }

    void rotate (double rotation) {
        for (int i = 0; i < arcs.length; i++) {
            rotations[i] = rotation;
            arcs[i].getTransforms().setAll(new Rotate(rotation));
        }
    }

}
