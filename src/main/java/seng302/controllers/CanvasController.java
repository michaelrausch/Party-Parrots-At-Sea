package seng302.controllers;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Created by ptg19 on 15/03/17.
 */
public class CanvasController {
    @FXML private Canvas canvas;

    public void initialize() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        drawBoat(gc, 0, 0, Color.GREEN);
        drawBoat(gc, 100, 100, Color.BLUE);
        drawBoat(gc, 32.296577, -64.854304, Color.RED);
        drawBoat(gc, 32.293771 , -64.855242, Color.RED);
    }

    private void drawBoat(GraphicsContext gc, double x, double y, Color color) {
        x += 180;  // to prevent negative longtitude
        y += 90;  // to prevent negative latitude
        int diameter = 10;
        gc.setFill(color);
        gc.fillOval(x, y, diameter, diameter);
    }
}
