package seng302.visualiser.fxObjects;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 * Visual object for a mark.
 */
public class Marker extends Group {

    Circle mark = new Circle();
    Group enterArrow;
    Group exitArrow;

    public Marker() {
        mark.setRadius(5);
    }

    public Marker(Paint colour) {
        this();
        mark.setFill(colour);
    }

    public void showEnterArrow () {
        Platform.runLater(() -> this.getChildren().setAll(enterArrow));
    }

    public void showExitArrow () {
        Platform.runLater(() -> this.getChildren().setAll(exitArrow));
    }
}