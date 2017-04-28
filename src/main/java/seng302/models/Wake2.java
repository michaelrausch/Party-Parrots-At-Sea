package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.transform.Rotate;

/**
 * Created by cir27 on 28/04/17.
 */
class Wake2 extends Arc {
    private final double OPACITY_INCREASE = -0.2;
    private final double RADIUS_INCREASE = 10;
    private final int DELAY = 60;
    private double opacity;
    private double rotationalVelocity;
    private Wake2 subWake;
    private boolean hasSubWake = false;
    private int delayCount = DELAY;

    Wake2 (double startingX, double startingY, double rotationalVelocity) {
        super(startingX, startingY, 30, 30, 150, 60);
        opacity = 1.0;
        super.setFill(new Color(0.0, 0.0, 0.0, opacity));
        subWake = new Wake2(
                startingX,
                startingY,
                super.getRadiusX() + RADIUS_INCREASE,
                rotationalVelocity,
                opacity + OPACITY_INCREASE
        );
        hasSubWake = true;
        this.rotationalVelocity = rotationalVelocity;

    }
    Wake2 (double startingX, double startingY, double radius, double rotationalVelocity, double opacity) {
        super(startingX, startingY, radius, radius, 150, 60);
        super.setFill(new Color(0.0, 0.0, 0.0, opacity));
        this.opacity = opacity;
        if (!(opacity < 0)) {
            subWake = new Wake2(
                    startingX,
                    startingY,
                    super.getRadiusX() + RADIUS_INCREASE,
                    rotationalVelocity,
                    opacity + OPACITY_INCREASE
            );
            hasSubWake = true;
        }
        this.rotationalVelocity = rotationalVelocity;
    }

    void setRotationalVelocity (double rotationalVelocity) {
        this.rotationalVelocity = rotationalVelocity;
        delayCount = DELAY;
    }

    void updatePosition (long timeInterval) {
        if (delayCount-- == 0)
            subWake.setRotationalVelocity(rotationalVelocity);
        super.getTransforms().clear();
        super.getTransforms().add(
                new Rotate(
                        rotationalVelocity * timeInterval,
                        super.getCenterX(),
                        super.getCenterY()
                )
        );
        if(hasSubWake)
            subWake.updatePosition(timeInterval);
    }

    void moveTo (double x, double y, double rotation) {
        super.setLayoutX(x);
        super.setLayoutY(y);
        super.getTransforms().clear();
        super.getTransforms().add(
                new Rotate(
                        rotation,
                        super.getCenterX(),
                        super.getCenterY()
                )
        );
        if(hasSubWake)
            subWake.moveTo(x, y, rotation);
    }
}
