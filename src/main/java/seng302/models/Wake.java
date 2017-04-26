package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

/**
 * Created by CJIRWIN on 27/04/2017.
 */
class Wake extends Arc {

    private static int VELOCITY_SCALE_FACTOR = 3;
    private static int MAX_LIFESPAN = 420;
    private static double LIFESPAN_PER_FRAME = 1.0 / MAX_LIFESPAN;
    //private static double LENGTH_PER_FRAME = 120 / MAX_LIFESPAN;
    private static double LENGTH_PER_FRAME = 0.08;

    private double velocityX;
    private double velocityY;
    private double opacity;
    private int lifespan = MAX_LIFESPAN;

    Wake (double startingX, double startingY, double velocityX, double velocityY) {
        super(0, 0, 20, 30, 180, 0);
        //super.setFill(Color.BLUE);
        super.setStroke(Color.BLUE);
        super.setType(ArcType.OPEN);
        super.setStrokeWidth(2.0);
        this.velocityX = -velocityX / 2;
        this.velocityY = -velocityY / 2;
    }

    boolean updatePosition (double timeInterval) {
        lifespan--;
        //super.setOpacity(LIFESPAN_PER_FRAME * lifespan * super.getOpacity());
        opacity = LIFESPAN_PER_FRAME * lifespan * opacity;
        super.setFill(new Color(0.0f, 0.0f, 1.0f, opacity));
        super.setLayoutX(super.getLayoutX() + velocityX * timeInterval);
        super.setLayoutY(super.getLayoutY() + velocityY * timeInterval);
        super.setStartAngle(super.getStartAngle() - LENGTH_PER_FRAME);
        super.setLength(super.getLength() + LENGTH_PER_FRAME * 2);
        return lifespan == 0;
    }

}
