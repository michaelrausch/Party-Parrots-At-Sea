package seng302.visualiser.fxObjects.assets_2D;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Created by cir27 on 5/09/17.
 */
public class WindArrow extends Polyline {
    public WindArrow(Paint fill) {
        this.getPoints().addAll(
            -10d, 15d,
            0d, 25d,
            0d, -25d,
            0d, 25d,
            10d, 15d
        );
        this.setStrokeLineCap(StrokeLineCap.ROUND);
        this.setStroke(fill);
        this.setStrokeWidth(5);
        this.setStrokeLineJoin(StrokeLineJoin.ROUND);
    }
}
