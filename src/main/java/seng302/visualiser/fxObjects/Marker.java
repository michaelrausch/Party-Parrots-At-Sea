package seng302.visualiser.fxObjects;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

/**
 * Visual object for a mark.
 */
public class Marker extends Group {

    Circle mark = new Circle();
    Paint colour = Color.BLACK;
    Group enterArrow;
    Group exitArrow;

    public Marker() {
        mark.setRadius(5);
        mark.setCenterX(0);
        mark.setCenterY(0);
        Platform.runLater(() -> this.getChildren().add(mark));
    }

    public Marker(Paint colour) {
        this();
        this.colour = colour;
        mark.setFill(colour);
    }

    public void constructArrows(MarkArrowFactory.RoundingSide roundingSide, double entryAngle, double exitAngle) {
        enterArrow = MarkArrowFactory.constructEntryArrow(roundingSide, entryAngle, exitAngle, colour);
        exitArrow = MarkArrowFactory.constructExitArrow(roundingSide, exitAngle, colour);
    }

    public void showEnterArrow () {
        if (!this.getChildren().contains(enterArrow)) {
            Platform.runLater(() -> {
                this.getChildren().remove(exitArrow);
                this.getChildren().add(enterArrow);
            });
        }
    }

    public void showExitArrow () {
        if (!this.getChildren().contains(exitArrow)) {
            Platform.runLater(() -> {
                this.getChildren().remove(enterArrow);
                this.getChildren().add(exitArrow);
            });
        }
    }

    public void hideAllArows () {
        Platform.runLater(() -> {
            this.getChildren().removeAll(enterArrow, exitArrow);
        });
    }
}