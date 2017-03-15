package seng302.controllers;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Created by ptg19 on 15/03/17.
 */
public class CanvasController {
    @FXML private Canvas canvas;

    public void initialize() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.fillOval(100, 200, 100, 80);
    }
}
