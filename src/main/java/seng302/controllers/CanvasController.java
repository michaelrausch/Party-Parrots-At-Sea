package seng302.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import seng302.models.Boat;
import seng302.models.TimelineInfo;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.MarkType;
import seng302.models.mark.SingleMark;

import java.util.*;

/**
 * Created by ptg19 on 15/03/17.
 * Modified by Haoming Yin (hyi25) on 20/3/2017.
 */
public class CanvasController {

    @FXML
    private AnchorPane canvasPane;

    private RaceViewController raceViewController;
    private ResizableCanvas canvas;
    private GraphicsContext gc;

    private final double ORIGIN_LAT = 32.321504;
    private final double ORIGIN_LON = -64.857063;
    private final int SCALE = 16000;

    public void setup(RaceViewController raceViewController){
        this.raceViewController = raceViewController;
    }

    public void initialize() {
        canvas = new ResizableCanvas();
        canvasPane.getChildren().add(canvas);
        // Bind canvas size to stack pane size.
        canvas.widthProperty().bind(canvasPane.widthProperty());
        canvas.heightProperty().bind(canvasPane.heightProperty());
        gc = canvas.getGraphicsContext2D();


        // overriding the handle so that it can clean canvas and redraw boats and course marks
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            private long lastFpsUpdate = 0;
            private int lastFpsCount = 0;
            private int fpsCount = 0;

            @Override
            public void handle(long now) {
                if (true){ //if statement for limiting refresh rate if needed
                    gc.clearRect(0, 0, canvas.getWidth(),canvas.getHeight());
                    gc.setFill(Color.SKYBLUE);
                    gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
                    drawCourse();
                    drawBoats();
                    drawFps(lastFpsCount);

                    // If race has started, draw the boats and play the timeline
                    if (raceViewController.getRace().getRaceTime() > 1){
                        raceViewController.playTimelines();
                    }
                    // Race has not started, pause the timelines
                    else {
                        raceViewController.pauseTimelines();
                    }
                    lastUpdate = now;
                    fpsCount ++;
                    if (now - lastFpsUpdate >= 1000000000){
                        lastFpsCount = fpsCount;
                        fpsCount = 0;
                        lastFpsUpdate = now;
                    }
                }
            }
        };
        timer.start();
    }

    class ResizableCanvas extends Canvas {

        public ResizableCanvas() {
            // Redraw canvas when size changes.
            widthProperty().addListener(evt -> draw());
            heightProperty().addListener(evt -> draw());
        }

        private void draw() {
            double width = getWidth();
            double height = getHeight();

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
    }

    private void drawFps(int fps){
        if (raceViewController.isDisplayFps()){
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(14));
            gc.setLineWidth(3);
            gc.fillText(fps + " FPS", 5, 20);
        }
    }

    /**
     * Draws all the boats.
     */
    private void drawBoats() {
        Map<Boat, TimelineInfo> timelineInfos = raceViewController.getTimelineInfos();
        for (Boat boat : timelineInfos.keySet()) {
            TimelineInfo timelineInfo = timelineInfos.get(boat);

            boat.setLocation(timelineInfo.getY().doubleValue(), timelineInfo.getX().doubleValue());

            drawBoat(boat.getLongitude(), boat.getLatitude(), boat.getColor(), boat.getShortName(), boat.getSpeedInKnots(), boat.getHeading());
        }
    }

    /**
     * Draw the wake line behind a boat
     * @param gc The graphics context used for drawing the wake
     * @param x the x position of the boat
     * @param y the y position of the boat
     * @param speed the speed of the boat
     * @param color the color of the wake line
     * @param heading the heading of the boat
     */
    private void drawWake(GraphicsContext gc, double x, double y, double speed, Color color, double heading){
        double angle = Math.toRadians(heading);
        speed = speed * 2;
        Point newP = new Point(0, speed);
        newP.rotate(angle);

        gc.setStroke(color);
        gc.setLineWidth(1.0);
        gc.strokeLine(x, y, newP.x + x, newP.y + y);
    }

    /**
     * Draws a boat with given (x, y) position in the given color
     *
     * @param lat
     * @param lon
     * @param color
     * @param name
     * @param speed
     */
    private void drawBoat(double lat, double lon, Color color, String name, double speed, double heading) {
        // Latitude
        double x = (lon - ORIGIN_LON) * SCALE;
        double y = (ORIGIN_LAT - lat) * SCALE;

        gc.setFill(color);

        if (raceViewController.isDisplayAnnotations()) {
            // Set boat text
            gc.setFont(new Font(14));
            gc.setLineWidth(3);
            gc.fillText(name + ", " + speed + " knots", x + 15, y + 15);
        }
//        double diameter = 9;
//        gc.fillOval(x, y, diameter, diameter);
        double angle = Math.toRadians(heading);

        Point p1 = new Point(0, -15); // apex point
        Point p2 = new Point(7, 4); // base point
        Point p3 = new Point(-7, 4); // base point
        p1.rotate(angle);
        p2.rotate(angle);
        p3.rotate(angle);
        double[] xx = new double[] {p1.x + x, p2.x + x, x, p3.x + x};
        double[] yy = new double[] {p1.y + y, p2.y + y, y, p3.y + y};
        gc.fillPolygon(xx, yy, 4);

        if (raceViewController.isDisplayAnnotations()){
            drawWake(gc, x, y, speed, color, heading);
        }
    }

    /**
     * Inner class for creating point so that you can rotate it around origin point.
     */
    class Point {

        double x, y;

        Point (double x, double y) {
            this.x = x;
            this.y = y;
        }

        void rotate(double angle) {
            double oldX = x;
            double oldY = y;
            this.x = oldX * Math.cos(angle) - oldY * Math.sin(angle);
            this.y = oldX * Math.sin(angle) + oldY * Math.cos(angle);

        }
    }

    /**
     * Draws the course.
     */
    private void drawCourse() {
        for (Mark mark : raceViewController.getRace().getCourse()) {
            if (mark.getMarkType() == MarkType.SINGLE_MARK) {
                drawSingleMark((SingleMark) mark, Color.BLACK);
            } else if (mark.getMarkType() == MarkType.GATE_MARK) {
                drawGateMark((GateMark) mark);
            }
        }
    }

    /**
     * Draw a given mark on canvas
     *
     * @param singleMark
     */
    private void drawSingleMark(SingleMark singleMark, Color color) {
        double x = (singleMark.getLongitude() - ORIGIN_LON) * SCALE;
        double y = (ORIGIN_LAT - singleMark.getLatitude()) * SCALE;

        gc.setFill(color);
        gc.fillRect(x,y,5.5,5.5);
    }

    /**
     * Draw a gate mark which contains two single marks
     *
     * @param gateMark
     */
    private void drawGateMark(GateMark gateMark) {
        Color color = Color.BLUE;

        if (gateMark.getName().equals("Start")){
            color = Color.RED;
        }

        if (gateMark.getName().equals("Finish")){
            color = Color.GREEN;
        }

        drawSingleMark(gateMark.getSingleMark1(), color);
        drawSingleMark(gateMark.getSingleMark2(), color);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(color);

        // Convert lat/lon to x,y
        double x1 = (gateMark.getSingleMark1().getLongitude()- ORIGIN_LON) * SCALE;
        double y1 = (ORIGIN_LAT - gateMark.getSingleMark1().getLatitude()) * SCALE;

        double x2 = (gateMark.getSingleMark2().getLongitude() - ORIGIN_LON) * SCALE;
        double y2 = (ORIGIN_LAT - gateMark.getSingleMark2().getLatitude()) * SCALE;

        gc.setLineWidth(1);
        gc.strokeLine(x1, y1, x2, y2);
    }
}