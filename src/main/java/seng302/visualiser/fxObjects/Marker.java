package seng302.visualiser.fxObjects;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 * Visual object for a mark.
 */
public class Marker extends Circle {

    public Marker() {
        super.setRadius(5);
    }

    public Marker(Paint colour) {
        this();
        super.setFill(colour);
    }
}