package seng302.controllers;

import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import seng302.models.Boat;
import seng302.models.BoatGroup;
import seng302.models.Colors;
import seng302.models.RaceObject;
import seng302.models.mark.*;
import seng302.models.parsers.StreamPacket;
import seng302.models.parsers.StreamParser;
import seng302.models.parsers.packets.BoatPositionPacket;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by ptg19 on 15/03/17.
 * Modified by Haoming Yin (hyi25) on 20/3/2017.
 */
public class CanvasController {

    @FXML
    private AnchorPane canvasPane;

    private RaceViewController raceViewController;
    private ResizableCanvas canvas;
    private Group group;
    private GraphicsContext gc;

    private final int MARK_SIZE     = 10;
    private final int BUFFER_SIZE   = 150;
    private final int CANVAS_WIDTH  = 1000;
    private final int CANVAS_HEIGHT = 1000;
    private final int LHS_BUFFER    = BUFFER_SIZE;
    private final int RHS_BUFFER    = BUFFER_SIZE + MARK_SIZE / 2;
    private final int TOP_BUFFER    = BUFFER_SIZE;
    private final int BOT_BUFFER    = TOP_BUFFER + MARK_SIZE / 2;

    private double distanceScaleFactor;
    private ScaleDirection scaleDirection;
    private Mark minLatPoint;
    private Mark minLonPoint;
    private Mark maxLatPoint;
    private Mark maxLonPoint;
    private double referencePointX;
    private double referencePointY;
    private double metersToPixels;
    private List<RaceObject> raceObjects = new ArrayList<>();

    //FRAME RATE
    private static final double UPDATE_TIME = 0.016666;     // 1 / 60 ie 60fps
    private final long[] frameTimes = new long[30];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;
    private DecimalFormat decimalFormat2dp = new DecimalFormat("0.00");

    public AnimationTimer timer;

    private enum ScaleDirection {
        HORIZONTAL,
        VERTICAL
    }

    public void setup(RaceViewController raceViewController){
        this.raceViewController = raceViewController;
    }

    public void initialize() {
        raceViewController = new RaceViewController();
        canvas = new ResizableCanvas();
        group = new Group();

        canvasPane.getChildren().add(canvas);
        canvasPane.getChildren().add(group);
        // Bind canvas size to stack pane size.
        canvas.widthProperty().bind(new SimpleDoubleProperty(CANVAS_WIDTH));
        canvas.heightProperty().bind(new SimpleDoubleProperty(CANVAS_HEIGHT));
        //group.minWidth(CANVAS_WIDTH);
        //group.minHeight(CANVAS_HEIGHT);
    }

    public void initializeCanvas (){

        gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setFill(Color.SKYBLUE);
        gc.fillRect(0,0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.restore();
        fitMarksToCanvas();
        drawBoats();
        timer = new AnimationTimer() {
            private int countdown = 60;
            private int[] currentRaceMarker = {1, 1, 1, 1, 1, 1};
            List<Mark> marks = raceViewController.getRace().getCourse();

            @Override
            public void handle(long now) {


                long oldFrameTime = frameTimes[frameTimeIndex] ;
                frameTimes[frameTimeIndex] = now ;
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
                if (frameTimeIndex == 0) {
                    arrayFilled = true ;
                }
                if (arrayFilled) {
                    long elapsedNanos = now - oldFrameTime ;
                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
                    Double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame ;
                    drawFps(frameRate.intValue());
                }
                for (RaceObject raceObject : raceObjects) {
                    raceObject.updatePosition(1000 / 60);
                    for (long id : raceObject.getRaceIds()) {
                        if (StreamParser.boatPositions.containsKey(id)) {

                            PriorityBlockingQueue<BoatPositionPacket> movementQueue = StreamParser.boatPositions.get(id);
                            if (movementQueue.size() > 0){
                                BoatPositionPacket positionPacket = movementQueue.peek();

                                int delayTime = 5000;
                                int loopTime = delayTime + 1000;
                                //System.out.println("id = " + id);
                                long timeDiff = (System.currentTimeMillis()%loopTime - positionPacket.getTimeValid()%loopTime);
                                if (timeDiff < 0){
                                    timeDiff = loopTime + timeDiff;
                                }
                                if (id == 106) {
                                    //System.out.println("timeDiff = " + timeDiff);
                                    //System.out.println("movementQueue.size() = " + movementQueue.size());
                                }
                                //System.out.println("timeDiff = " + timeDiff);
                                //System.out.println("movementQueue.size() = " + movementQueue.size());
                                if (timeDiff > delayTime) {
                                    if (id == 106){
                                        //System.out.println("processing");
                                    }
                                    try {
                                        positionPacket = movementQueue.take();

                                        Point2D p2d = latLonToXY(positionPacket.getLat(), positionPacket.getLon());
                                        double heading = 360.0 / 0xffff * positionPacket.getHeading();
                                        raceObject.setDestination(p2d.getX(), p2d.getY(), heading, (int) id);

                                    } catch (InterruptedException e){
                                        e.printStackTrace();
                                    }
                                }


                            }
                        }
                        //StreamParser.boatPositions.remove((long) id);
                    }
                }
            }
        };
        for (Mark m : raceViewController.getRace().getCourse()) {
//            System.out.println(m.getName());
        }
        //timer.start();
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
            gc.clearRect(5,5,50,20);
            gc.setFill(Color.BLACK);
            gc.setFont(new Font(14));
            gc.setLineWidth(3);
            gc.fillText(fps + " FPS", 5, 20);
        } else {
            gc.clearRect(5,5,50,20);
            gc.setFill(Color.SKYBLUE);
            gc.fillRect(4,4,51,21);
        }
    }

    /**
     * Draws all the boats.
     */
    private void drawBoats() {
//        Map<Boat, TimelineInfo> timelineInfos = raceViewController.getTimelineInfos();
        List<Boat> boats  = raceViewController.getStartingBoats();
        Double startingX  = raceObjects.get(0).getLayoutX();
        Double startingY  = raceObjects.get(0).getLayoutY();
        Group boatAnnotations = new Group();

        for (Boat boat : boats) {
            BoatGroup boatGroup = new BoatGroup(boat, Colors.getColor());
            boatGroup.moveTo(startingX, startingY, 0d);
//            boatGroup.setDestination(firstMarkX, firstMarkY);
            boatGroup.forceRotation();
            //group.getChildren().add(boatGroup);
            raceObjects.add(boatGroup);
            boatAnnotations.getChildren().add(boatGroup.getLowPriorityAnnotations());
//            drawBoat(boat.getLongitude(), boat.getLatitude(), boat.getColor(), boat.getShortName(), boat.getSpeedInKnots(), boat.getHeading());
        }
        group.getChildren().add(boatAnnotations);
        group.getChildren().addAll(raceObjects);
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
     * Calculates x and y location for every marker that fits it to the canvas the race will be drawn on.
     */
    private void fitMarksToCanvas() {
        findMinMaxPoint();
        double minLonToMaxLon = scaleRaceExtremities();
        calculateReferencePointLocation(minLonToMaxLon);
        givePointsXY();
        findMetersToPixels();
    }


    /**
     * Sets the class variables minLatPoint, maxLatPoint, minLonPoint, maxLonPoint to the marker with the leftmost
     * marker, rightmost marker, southern most marker and northern most marker respectively.
     */
    private void findMinMaxPoint() {
        List<Mark> sortedPoints = new ArrayList<>();
        for (Mark mark : raceViewController.getRace().getCourse())
        {
            if (mark.getMarkType() == MarkType.SINGLE_MARK)
                sortedPoints.add(mark);
            else {
                sortedPoints.add(((GateMark) mark).getSingleMark1());
                sortedPoints.add(((GateMark) mark).getSingleMark2());
            }
        }
        sortedPoints.sort(Comparator.comparingDouble(Mark::getLatitude));
        minLatPoint = sortedPoints.get(0);
        maxLatPoint = sortedPoints.get(sortedPoints.size()-1);

        sortedPoints.sort(Comparator.comparingDouble(Mark::getLongitude));
        //If the course is on a point on the earth where longitudes wrap around.
        // TODO: 30/03/17 cir27 - Correctly account for longitude wrapping around.
        if (sortedPoints.get(sortedPoints.size()-1).getLongitude() - sortedPoints.get(0).getLongitude() > 180)
            Collections.reverse(sortedPoints);
        minLonPoint = sortedPoints.get(0);
        maxLonPoint = sortedPoints.get(sortedPoints.size()-1);
    }

    /**
     * Calculates the location of a reference point, this is always the point with minimum latitude, in relation to the
     * canvas.
     *
     * @param minLonToMaxLon The horizontal distance between the point of minimum longitude to maximum longitude.
     */
    private void calculateReferencePointLocation (double minLonToMaxLon) {
        Mark referencePoint = minLatPoint;
        double referenceAngle;

        if (scaleDirection == ScaleDirection.HORIZONTAL) {
            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, minLonPoint));
            referencePointX = LHS_BUFFER + distanceScaleFactor * Math.sin(referenceAngle) * Mark.calculateDistance(referencePoint, minLonPoint);

            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, maxLatPoint));
            referencePointY  = CANVAS_HEIGHT - (TOP_BUFFER + BOT_BUFFER);
            referencePointY -= distanceScaleFactor * Math.cos(referenceAngle) * Mark.calculateDistance(referencePoint, maxLatPoint);
            referencePointY  = referencePointY / 2;
            referencePointY += TOP_BUFFER;
            referencePointY += distanceScaleFactor * Math.cos(referenceAngle) * Mark.calculateDistance(referencePoint, maxLatPoint);
        } else {
            referencePointY = CANVAS_HEIGHT - BOT_BUFFER;

            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, minLonPoint));
            referencePointX  = LHS_BUFFER;
            referencePointX += distanceScaleFactor * Math.sin(referenceAngle) * Mark.calculateDistance(referencePoint, minLonPoint);
            referencePointX += ((CANVAS_WIDTH - (LHS_BUFFER + RHS_BUFFER)) - (minLonToMaxLon * distanceScaleFactor)) / 2;
        }
    }

    /**
     * Finds the scale factor necessary to fit all race markers within the onscreen map and assigns it to distanceScaleFactor
     * Returns the max horizontal distance of the map.
     */
    private double scaleRaceExtremities () {

        double vertAngle = Math.abs(Mark.calculateHeadingRad(minLatPoint, maxLatPoint));
        double vertDistance = Math.cos(vertAngle) * Mark.calculateDistance(minLatPoint, maxLatPoint);
        double horiAngle = Mark.calculateHeadingRad(minLonPoint, maxLonPoint);

        if (horiAngle <= (Math.PI / 2))
            horiAngle = (Math.PI / 2) - horiAngle;
        else
            horiAngle = horiAngle - (Math.PI / 2);
        double horiDistance = Math.cos(horiAngle) * Mark.calculateDistance(minLonPoint, maxLonPoint);

        double vertScale = (CANVAS_HEIGHT - (TOP_BUFFER + BOT_BUFFER)) / vertDistance;

        if ((horiDistance * vertScale) > (CANVAS_WIDTH - (RHS_BUFFER + LHS_BUFFER))) {
            distanceScaleFactor = (CANVAS_WIDTH - (RHS_BUFFER + LHS_BUFFER)) / horiDistance;
            scaleDirection = ScaleDirection.HORIZONTAL;
        } else {
            distanceScaleFactor = vertScale;
            scaleDirection = ScaleDirection.VERTICAL;
        }
        return horiDistance;
    }

    /**
     * Give all markers in the course an x,y location relative to a given reference with a known x,y location. Distances
     * are scaled according to the distanceScaleFactor variable.
     */
    private void givePointsXY() {
        List<Mark> allPoints = new ArrayList<>(raceViewController.getRace().getCourse());
        List<Mark> processed = new ArrayList<>();
        RaceObject markGroup;

        for (Mark mark : allPoints) {
            if (!processed.contains(mark)) {
                if (mark.getMarkType() != MarkType.SINGLE_MARK) {
                    GateMark gateMark = (GateMark) mark;
                    markGroup = new MarkGroup(mark, findScaledXY(gateMark.getSingleMark1()), findScaledXY(gateMark.getSingleMark2()));
                    raceObjects.add(markGroup);
                } else {
                    markGroup = new MarkGroup(mark, findScaledXY(mark));
                    raceObjects.add(markGroup);
                }
                processed.add(mark);
            }
        }
    }

    private Point2D findScaledXY (Mark unscaled) {
        return findScaledXY (minLatPoint.getLatitude(), minLatPoint.getLongitude(),
                             unscaled.getLatitude(), unscaled.getLongitude());
    }

    private Point2D findScaledXY (double latA, double lonA, double latB, double lonB) {
        double distanceFromReference;
        double angleFromReference;
        int xAxisLocation = (int) referencePointX;
        int yAxisLocation = (int) referencePointY;

        angleFromReference = Mark.calculateHeadingRad(latA, lonA, latB, lonB);
        distanceFromReference = Mark.calculateDistance(latA, lonA, latB, lonB);
        if (angleFromReference >= 0 && angleFromReference <= Math.PI / 2) {
            xAxisLocation += (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else if (angleFromReference >= 0) {
            angleFromReference = angleFromReference - Math.PI / 2;
            xAxisLocation += (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            angleFromReference = Math.abs(angleFromReference);
            xAxisLocation -= (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else {
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xAxisLocation -= (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        }
        return new Point2D(xAxisLocation, yAxisLocation);
    }



    /**
     * Find the number of meters per pixel.
     */
    private void findMetersToPixels () {
        Double angularDistance;
        Double angle;
        Double straightLineDistance;
        if (scaleDirection == ScaleDirection.HORIZONTAL) {
            angularDistance = Mark.calculateDistance(minLonPoint, maxLonPoint);
            angle = Mark.calculateHeadingRad(minLonPoint, maxLonPoint);
            if (angle > Math.PI / 2) {
                straightLineDistance = Math.cos(angle - Math.PI) * angularDistance;
            } else {
                straightLineDistance = Math.cos(angle) * angularDistance;
            }
            metersToPixels = (CANVAS_WIDTH - RHS_BUFFER - LHS_BUFFER) / straightLineDistance;
        } else {
            angularDistance = Mark.calculateDistance(minLatPoint, maxLatPoint);
            angle = Mark.calculateHeadingRad(minLatPoint, maxLatPoint);
            if (angle < Math.PI / 2) {
                straightLineDistance = Math.cos(angle) * angularDistance;
            } else {
                straightLineDistance = Math.cos(-angle + Math.PI * 2) * angularDistance;
            }
            metersToPixels = (CANVAS_HEIGHT - TOP_BUFFER - BOT_BUFFER) / straightLineDistance;
        }
    }

    private Point2D latLonToXY (double latitude, double longitude) {
        return findScaledXY(minLatPoint.getLatitude(), minLatPoint.getLongitude(), latitude, longitude);
    }

    List<RaceObject> getRaceObjects() {
        return raceObjects;
    }
}