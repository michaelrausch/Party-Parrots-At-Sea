package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

/**
 * Created by CJIRWIN on 27/04/2017.
 */
class Wake extends Arc {

    private static int VELOCITY_SCALE_FACTOR = 3;
    private static int MAX_LIFESPAN = 180;
    private static double LIFESPAN_PER_FRAME = 1.0 / MAX_LIFESPAN;

    private double velocityX;
    private double velocityY;
    private int lifespan = MAX_LIFESPAN;

    Wake (double startingX, double startingY, double velocityX, double velocityY) {
        super(startingX, startingY, 25, 15, 160, 40);
        super.setFill(Color.BLUE);
        super.setType(ArcType.OPEN);
        super.setStrokeWidth(2.0);
        this.velocityX = -velocityX;
        this.velocityY = -velocityY;
    }

    boolean updatePosition (double timeInterval) {
        lifespan--;
        super.setLayoutX(super.getLayoutX() + velocityX * timeInterval);
        super.setLayoutX(super.getLayoutX() + velocityX * timeInterval);
        super.setOpacity(LIFESPAN_PER_FRAME * lifespan * super.getOpacity());
        return lifespan == 0;
    }

}
