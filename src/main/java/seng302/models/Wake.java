package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Created by CJIRWIN on 27/04/2017.
 */
class Wake extends Arc {

    private static int VELOCITY_SCALE_FACTOR = 3;
    private static int MAX_LIFESPAN = 210;
    private static double LIFESPAN_PER_FRAME = 1.0 / MAX_LIFESPAN;
    //private static double LENGTH_PER_FRAME = 120 / MAX_LIFESPAN;
    private static double LENGTH_PER_FRAME = 0.25;

    private double velocityX;
    private double velocityY;
    private double opacity;
    private int lifespan = MAX_LIFESPAN;

    Wake (double startingX, double startingY, double velocityX, double velocityY, double rotation) {
        super(startingX, startingY, 20, 30, 180, 0);
        //super.setFill(Color.BLUE);
        super.setStroke(Color.DEEPSKYBLUE);
        super.setType(ArcType.OPEN);
        super.setFill(new Color(0, 0, 0 ,0));
        super.setStrokeWidth(2.0);
        super.getTransforms().add(new Rotate(rotation, 5, -15));
//        this.velocityX = -velocityX;
//        this.velocityY = -velocityY;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    boolean updatePosition (double timeInterval) {
        lifespan--;
        //super.setOpacity(LIFESPAN_PER_FRAME * lifespan * super.getOpacity());
        //opacity = LIFESPAN_PER_FRAME * lifespan * opacity;
        //super.setFill(new Color(0.0f, 0.0f, 1.0f, opacity));
        super.setLayoutX(super.getLayoutX() + velocityX * timeInterval);
        super.setLayoutY(super.getLayoutY() + velocityY * timeInterval);
        super.setStartAngle(super.getStartAngle() - LENGTH_PER_FRAME);
        super.setLength(super.getLength() + LENGTH_PER_FRAME * 2);
        return lifespan < 0;
    }

}
