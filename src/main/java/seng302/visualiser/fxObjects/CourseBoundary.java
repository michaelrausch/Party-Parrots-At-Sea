package seng302.visualiser.fxObjects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Polygon with default course border settings.
 */
public class CourseBoundary extends Polygon {
    public CourseBoundary() {
        this.setStroke(new Color(0.0f, 0.0f, 0.74509807f, 1));
        this.setStrokeWidth(3);
        this.setFill(new Color(0,0,0,0));
    }
}
