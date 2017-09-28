package seng302.visualiser.fxObjects.assets_2D;

import javafx.application.Platform;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Rotate;

/**
 * A group containing objects used to represent wakes onscreen. Contains functionality for their animation.
 */
public class Wake extends Group {

    //The number of wakes
    private int numWakes = 8;


    private Arc[] arcs = new Arc[numWakes];
    private double[] rotationalVelocities = new double[numWakes];
    private double[] rotations = new double[numWakes];

    /**
     * Create a wake at the given location.
     *
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
            arc.setCache(true);
            arc.setCacheHint(CacheHint.ROTATE);
            arc.setType(ArcType.OPEN);
            arc.setStroke(
                new Color(
                    0.18, 0.7, 1.0, 1.0 + (-0.99 / numWakes * i)
                )
            );
            arc.setStrokeWidth(3.0);
            arc.setStrokeLineCap(StrokeLineCap.ROUND);
            arc.setFill(new Color(0.0, 0.0, 0.0, 0.0));
            arcs[i] = arc;
            arc.getTransforms().setAll(
                new Rotate(1)
            );
        }
        super.getChildren().addAll(arcs);
    }

    void setRotation (double rotation, double velocity) {
//        if (Math.abs(rotations[0] - rotation) > 20) {
        Platform.runLater(() -> {
            rotate(rotation);
            double rad = (14 / numWakes) + velocity;
            for (Arc arc : arcs) {
                arc.setRadiusX(rad);
                arc.setRadiusY(rad);
                rad += (14 / numWakes) + (velocity / 2.5);
            }
        });
    }

    /**
     * Arcs rotate based on the distance they would have travelled over the supplied time interval.
     */
    void updatePosition() {
        for (int i = 0; i < numWakes; i++) {
            rotations[i] = rotations[i] + rotationalVelocities[i];
            ((Rotate) arcs[i].getTransforms().get(0)).setAngle(rotations[i]);
        }
    }

    /**
     * Rotate all wakes to the given rotation.
     *
     * @param rotation the from north angle in degrees to rotate to.
     */
    void rotate(double rotation) {
        for (int i = 0; i < arcs.length; i++) {
            rotations[i] = rotation;
            rotationalVelocities[i] = 0;
            arcs[i].getTransforms().setAll(new Rotate(rotation));
        }
    }
}
