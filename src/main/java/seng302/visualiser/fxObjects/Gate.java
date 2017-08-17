package seng302.visualiser.fxObjects;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

/**
 * Visual object representing a gate, intended to connect two mark objects.
 */
public class Gate extends Line {

    public Gate () {
        super.setStrokeWidth(2);
        super.getStrokeDashArray().setAll(2d, 5d);
    }

    public Gate (Paint colour) {
        this();
        super.setStroke(colour);
    }
}
