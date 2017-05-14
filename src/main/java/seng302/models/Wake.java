package seng302.models;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;

/**
 * By default wake is a group containing 5 arcs. Each arc starts from the same point. Each arc is larger and more
 * transparent than the last. On calling updatePositions() arcs rotate at velocities given by setRotationalVelocity().
 * The larger and more transparent an arc is the longer the delay before it rotates at the latest velocity. It is
 * assumed that rotationalVelocities() are set regularly as wakes do not stop rotating and an array of velocities needs
 * to be populated for the class to work as expected.
 */
class Wake extends Group {

    //Wake Settings. Should probably be hard coded in when the final values are decided upon.
    private enum functionType {LINEAR, LOGARITHMIC, POWER, ROOT, POWOUT_LOGIN}
    private functionType wakeFunction = functionType.LOGARITHMIC;
    private ArcType arcType = ArcType.OPEN;
    private int numWakes = 10;
    private double offSet = 0;
    private final double MAX_DIFF = 75.0;
    private final int UNIFICATION_SPEED = 500;
    private final int POWER = 2;

    private Arc[] arcs = new Arc[numWakes];
    private double[] rotationalVelocities = new double[numWakes];
    private double[] rotations = new double[numWakes];
    private double baseRad;
    private boolean spawnNewWake = false;
    private int count = 10;

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
            arc = new Arc(0, 0, 0, 0, -110, 40);

            if (arcType == ArcType.ROUND) {
            arc.setFill(new Color(0.18, 0.7, 1.0, 0.4 + (-0.35 / numWakes * i)));
            arc.setType(ArcType.ROUND);
            baseRad = 10;

            } else if (arcType == ArcType.OPEN) {
                arc.setType(ArcType.OPEN);
                arc.setStroke(new Color(0.18, 0.7, 1.0, 1.0 + (-0.99 / numWakes * i)));
                arc.setStrokeWidth(3.0);
                arc.setStrokeLineCap(StrokeLineCap.ROUND);
                arc.setFill(new Color(0.0, 0.0, 0.0, 0.0));
                baseRad = (20 / numWakes);
                arcs[i] = arc;
            }
        }
        super.getChildren().addAll(arcs);
    }

    /**
     * Sets the rotationalVelocity of each arc. Each arc is 3 velocities behind the next smallest arc. The smallest uses
     * the latest given velocity.
     * @param rotationalVelocity The rotationalVelocity the wake should move at.
     * @param velocity The real world velocity of the boat in m/s.
     */
    void setRotationalVelocity (double rotationalVelocity, double velocity) {
        rotationalVelocities[0] = rotationalVelocity;
        for (int i = 1; i < numWakes; i++) {
            double difference = Math.atan2(
                    Math.sin(
                            Math.toRadians(
                                    rotations[i - 1] - rotations[i]
                            )
                    ),
                    Math.cos(
                            Math.toRadians(
                                    rotations[i - 1] - rotations[i]
                            )
                    )
            );
            difference = Math.toDegrees(difference);

            if (wakeFunction == functionType.LOGARITHMIC) {
                if (rotationalVelocities[i-1] < 0.01 && rotationalVelocities[i-1] > -0.01) {
                    rotationalVelocities[i] = (MAX_DIFF / numWakes) / UNIFICATION_SPEED * Math.log(Math.abs(difference) + 1) / Math.log(MAX_DIFF / numWakes) * 1.5;
                    if (difference < 0)
                    {
                        rotationalVelocities[i] = -rotationalVelocities[i];
                    }
                } else {
                        rotationalVelocities[i] = rotationalVelocities[i-1] * Math.log(Math.abs(difference) + 1) / Math.log(MAX_DIFF / numWakes);
                }

            } else if (wakeFunction == functionType.LINEAR) {
                if (rotationalVelocities[i - 1] < 0.01 && rotationalVelocities[i - 1] > -0.01) {
                    rotationalVelocities[i] = difference / UNIFICATION_SPEED * 2;
                } else {
                    if (difference < (MAX_DIFF / numWakes))
                        rotationalVelocities[i] = rotationalVelocities[i - 1] * difference / (MAX_DIFF / numWakes);
                    else
                        rotationalVelocities[i] = rotationalVelocities[i - 1];
                }
            } else if (wakeFunction == functionType.POWER) {
                if (rotationalVelocities[i - 1] < 0.01 && rotationalVelocities[i - 1] > -0.01) {
                    rotationalVelocities[i] = difference / UNIFICATION_SPEED * Math.pow(difference, POWER) / Math.pow((MAX_DIFF / numWakes), POWER);
                } else {
                    if (difference < (MAX_DIFF / numWakes))
                        rotationalVelocities[i] = rotationalVelocities[i - 1] * Math.pow(difference, POWER) / Math.pow((MAX_DIFF / numWakes), POWER);
                    else
                        rotationalVelocities[i] = rotationalVelocities[i - 1];
                }
            } else if (wakeFunction == functionType.ROOT) {
                if (rotationalVelocities[i - 1] < 0.01 && rotationalVelocities[i - 1] > -0.01) {
                    rotationalVelocities[i] = (MAX_DIFF / numWakes) / UNIFICATION_SPEED * Math.sqrt(Math.abs(difference)) / Math.sqrt(MAX_DIFF / numWakes);
                } else {
                    if (difference < (MAX_DIFF / numWakes))
                        rotationalVelocities[i] = rotationalVelocities[i - 1] * Math.sqrt(Math.abs(difference)) / Math.sqrt(MAX_DIFF / numWakes);
                    else
                        rotationalVelocities[i] = rotationalVelocities[i - 1];
                }
                if (difference < 0)
                    rotationalVelocities[i] = -rotationalVelocities[i];
            } else if (wakeFunction == functionType.POWOUT_LOGIN) {
                if (rotationalVelocities[i - 1] < 0.01 && rotationalVelocities[i - 1] > -0.01) {
                    rotationalVelocities[i] = difference / UNIFICATION_SPEED * Math.log(Math.abs(difference) + 1) / Math.log(MAX_DIFF / numWakes);
                } else {
                    if (difference < (MAX_DIFF / numWakes))
                        rotationalVelocities[i] = rotationalVelocities[i - 1] * Math.pow(difference, POWER) / Math.pow((MAX_DIFF / numWakes), POWER);
                    else
                        rotationalVelocities[i] = rotationalVelocities[i - 1];
                }
            }

        }

        //Scale wakes based on velocity.
//        if (count--  == 0)
//        {
//            count = 10;
//            offSet = 0;
//        } else {
//            offSet += baseRad / 5;
//        }
        double rad = baseRad + velocity + offSet;
        for (Arc arc :arcs) {
            arc.setRadiusX(rad);
            arc.setRadiusY(rad);
            rad += (20 / numWakes) + (velocity / 2);
        }
    }

    /**
     * Arcs rotate based on the distance they would have travelled over the supplied time interval.
     * @param timeInterval the time interval, in microseconds, that the wake should move.
     */
    void updatePosition (long timeInterval) {
        for (int i = 0; i < numWakes; i++) {
            rotations[i] = rotations[i] + rotationalVelocities[i] * timeInterval;
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
            rotationalVelocities[i] = 0;
            arcs[i].getTransforms().setAll(new Rotate(rotation));
        }
    }

}
