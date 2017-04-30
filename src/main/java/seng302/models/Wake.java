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

    final int numWakes = 5;
    private double[] velocities = new double[13];
    private Arc[] arcs = new Arc[numWakes];
    private double[] rotations = new double[numWakes];
    private int[] velocityIndices = new int[numWakes];
    private double sum = 0;
    static double max = 0;
    static double min = Double.MAX_VALUE;

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
            //Default triangle is -110 deg out of phase with a default wake and has angle of 40 deg.
            arc = new Arc(0,0,0,0,-110,40);
            //Opacity increases from 0.5 -> 0 evenly over the 5 wake arcs.
            arc.setFill(new Color(0.18, 0.7, 1.0, 0.50 + -0.1 * i));
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
        if (Math.abs(rotationalVelocity) > 0.5) {
            rotationalVelocity = 0;
        }
        sum -= Math.abs(velocities[(velocityIndices[0] + 10) % 13]);
//        sum -= Math.abs(velocities[velocityIndices[0]]);
        sum += Math.abs(rotationalVelocity);
        System.out.println("sum = " + sum);
        if (sum < 0.9)
            rotate (rotationGoal); //In relatively straight segments the wake snaps to match the boats current position.
        //This stops the wake from eventually becoming out of sync with the boat.

        max = Math.max(max, sum);
        min = Math.min(max, sum);
        System.out.println("max = " + max);
        System.out.println("min = " + min);
        //Update the index of the array of recent velocities that each wake uses. Each wake is 3 velocities behind the
        //next smallest wake.
        velocityIndices[0] = (13 + (velocityIndices[0] - 1) % 13) % 13;
        velocities[velocityIndices[0]] = rotationalVelocity;
        for (int i = 1; i < numWakes; i++)
            velocityIndices[i] = (velocityIndices[0] + 3 * i) % 13;

        //Scale wakes based on velocity. Assumes boats are always moving at a decent pace.
        double scaleFactor = Math.abs(Math.log10(Math.abs(velocityX) + Math.abs(velocityY)));
        double baseRad = 25;
        for (Arc arc :arcs) {
            double rad = Math.min(baseRad + 5 * scaleFactor, baseRad + 15);
            arc.setRadiusX(rad);
            arc.setRadiusY(rad);
            baseRad += 10;
        }
    }

    /**
     * Arcs rotate based on the distance they would have travelled over the supplied time interval.
     * @param timeInterval the time interval, in microseconds, that the wake should move.
     */
    void updatePosition (long timeInterval) {
        for (int i = 0; i < numWakes; i++) {
            rotations[i] = rotations[i] + velocities[velocityIndices[i]] * timeInterval;
            arcs[i].getTransforms().setAll(new Rotate(rotations[i]));
        }
    }

    /**
     * Rotate all wakes to the given rotation.
     * @param rotation the from north angle in degrees to rotate to.
     */
    void rotate (double rotation) {
        for (int i = 0; i < arcs.length; i++) {
            rotations[i] = rotation;
            arcs[i].getTransforms().setAll(new Rotate(rotation));
        }
    }

}
