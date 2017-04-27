package seng302.controllers;

import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;
import seng302.models.Boat;
import seng302.models.BoatGroup;
import seng302.models.Colors;
import seng302.models.RaceObject;
import seng302.models.mark.*;
import seng302.models.parsers.StreamParser;
import seng302.models.parsers.StreamReceiver;

import java.sql.Time;
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
    private Group group;
    private GraphicsContext gc;

    private final int MARK_SIZE     = 10;
    private final int BUFFER_SIZE   = 25;
    private final int CANVAS_WIDTH  = 1000;
    private final int CANVAS_HEIGHT = 1000;
    private final int LHS_BUFFER    = BUFFER_SIZE;
    private final int RHS_BUFFER    = BUFFER_SIZE + MARK_SIZE / 2;
    private final int TOP_BUFFER    = BUFFER_SIZE;
    private final int BOT_BUFFER    = TOP_BUFFER + MARK_SIZE / 2;
    private final int FRAME_RATE    = 60;

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
        group.minWidth(CANVAS_WIDTH);
        group.minHeight(CANVAS_HEIGHT);
    }

    public void initializeCanvas (){

        gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setFill(Color.SKYBLUE);
        gc.fillRect(0,0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.restore();
        drawCourse();
        drawBoats();
//        drawFps(12);
//        // overriding the handle so that it can clean canvas and redraw boats and course marks
//        AnimationTimer timer = new AnimationTimer() {
//            private long lastUpdate = 0;
//            private long lastFpsUpdate = 0;
//            private int lastFpsCount = 0;
//            private int fpsCount = 0;
//            boolean done = true;
//
//            @Override
//            public void handle(long now) {
//                if (true){ //if statement for limiting refresh rate if needed
////                    gc.clearRect(0, 0, canvas.getWidth(),canvas.getHeight());
////                    gc.setFill(Color.SKYBLUE);
////                    gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
//
//
//                    // If race has started, draw the boats and play the timeline
//                    if (raceViewController.getRace().getRaceTime() > 1) {
//                        raceViewController.playTimelines();
//                    }
//                    // Race has not started, pause the timelines
//                    else {
//                        raceViewController.pauseTimelines();
//                    }
//                    lastUpdate = now;
//                    fpsCount ++;
//                    if (now - lastFpsUpdate >= 1000000000){
//                        lastFpsCount = fpsCount;
//                        fpsCount = 0;
//                        lastFpsUpdate = now;
//                    }
//                }
//            }
//        };
//        timer.start();
        //try {
        //    Thread.sleep(10000);
        //}catch (Exception e) {
        //    e.printStackTrace();
        //}

        StreamReceiver sr = new StreamReceiver("csse-s302staff.canterbury.ac.nz", 4941,"TestThread1");
//        StreamReceiver sr = new StreamReceiver("livedata.americascup.com", 4941, "TestThread1");
        sr.start();

        timer = new AnimationTimer() {
            private int countdown = 60;
            private int[] currentRaceMarker = {1, 1, 1, 1, 1, 1};
            List<Mark> marks = raceViewController.getRace().getCourse();

            @Override
            public void handle(long now) {
                boolean raceFinished = true;
                boolean descending;
                boolean leftToRight;
                int boatIndex = 0;

                Mark nextMark;
                //if (countdown == 0) {
                //System.out.println("called the at");
                    for (RaceObject raceObject : raceObjects) {
                        //if (currentRaceMarker[boatIndex] < marks.size()) {
                            //if (currentRaceMarker[boatIndex] == 6) {
                            //    int debugLine = 4;
                            //}
                            //double xb4 = boatGroup.getLayoutX();
                            //double yb4 = boatGroup.getLayoutY();
                            //nextMark = marks.get(currentRaceMarker[boatIndex]);

                            //descending = nextMark.getY() > boatGroup.getLayoutY();
                            //leftToRight = nextMark.getX() < boatGroup.getLayoutX();


                            raceObject.updatePosition(1000 / 60);
                            for (int id : raceObject.getRaceIds()) {
                                //System.out.println("id = " + id);
                                if (id != 0 && StreamParser.boatPositions.size() > 0) {
                                    boolean test = StreamParser.boatPositions.containsKey(id);
                                    if (StreamParser.boatPositions.containsKey((long) id)) {
                                        Point3D p = StreamParser.boatPositions.get((long) id);
                                        Point2D p2d = latLonToXY(p.getX(), p.getY());
                                        //System.out.println("p2d = " + p2d);
                                        //System.out.println("p.toString() = " + p.toString());
                                        double heading = 360.0 / 0xffff * p.getZ();
                                        //System.out.println("heading = " + heading);

                                        raceObject.setDestination(p2d.getX(), p2d.getY(), heading, id);

                                        //raceObject.setDestination(p2d.getX(), p2d.getY(), id);
                                    }
                                    StreamParser.boatPositions.remove((long) id);
                                }
                            }
                            //Point3D p = StreamParser.boatPositions.get((long) raceObject.getRaceIds()[0]);
                            //System.out.println("boatGroup = " + boatGroup.getBoat().getId());
                            //System.out.println("StreamParser.boatPositions.toString() = " + StreamParser.boatPositions.toString());
//                            if (p != null) {
//                                Point2D p2d = latLonToXY(p.getX(), p.getY());
//                                //System.out.println("p2d = " + p2d);
//                                if (!boatGroup.isSamePos(p2d)) {
//                                    //System.out.println("p.toString() = " + p.toString());
//                                    double heading = 360.0 / 0xffff * p.getZ();
//                                    //System.out.println("heading = " + heading);
//
//
//
//                                    boatGroup.setDestination(p2d.getX(), p2d.getY(), heading, boatGroup.getRaceIds()[0]);



                                    //boatGroup.setDestination(p2d.getX(), p2d.getY());
                              //  }
                            //}

//                            if (descending && nextMark.getY() < boatGroup.getLayoutY()) {
//                                currentRaceMarker[boatIndex]++;
//                                boatGroup.setDestination(
//                                        marks.get(currentRaceMarker[boatIndex]).getX(), marks.get(currentRaceMarker[boatIndex]).getY()
//                                );
//                            } else if (!descending && nextMark.getY() > boatGroup.getLayoutY()) {
//                                currentRaceMarker[boatIndex]++;
//                                boatGroup.setDestination(
//                                        marks.get(currentRaceMarker[boatIndex]).getX(), marks.get(currentRaceMarker[boatIndex]).getY()
//                                );
//                            } else if (leftToRight && nextMark.getX() > boatGroup.getLayoutX()) {
//                                currentRaceMarker[boatIndex]++;
//                                boatGroup.setDestination(
//                                        marks.get(currentRaceMarker[boatIndex]).getX(), marks.get(currentRaceMarker[boatIndex]).getY()
//                                );
//                            } else if (!leftToRight && nextMark.getX() < boatGroup.getLayoutX()) {
//                                currentRaceMarker[boatIndex]++;
//                                boatGroup.setDestination(
//                                        marks.get(currentRaceMarker[boatIndex]).getX(), marks.get(currentRaceMarker[boatIndex]).getY()
//                                );


//                            double xnew = boatGroup.getLayoutX();
//                            double ynew = boatGroup.getLayoutY();
//                            double dx = xnew - xb4;
//                            double dy = ynew -yb4;
//                            raceFinished = false;
//                            boatIndex++;
                        }
                    //}
                    //if (raceFinished) {
                    //    System.out.println("DONZEO LADS");
                    //    this.stop();
                    //}
                //} else {
                //    countdown--;
                //}
            }
        };
        for (Mark m : raceViewController.getRace().getCourse()) {
            System.out.println(m.getName());
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
//        Map<Boat, TimelineInfo> timelineInfos = raceViewController.getTimelineInfos();
        List<Boat> boats  = raceViewController.getStartingBoats();
        System.out.println("raceObjects " + raceObjects);
        Double startingX  = raceObjects.get(0).getLayoutX();
        Double startingY  = raceObjects.get(0).getLayoutY();
        Double firstMarkX = raceObjects.get(1).getLayoutX();
        Double firstMarkY = raceObjects.get(1).getLayoutY();

        for (Boat boat : boats) {
            BoatGroup boatGroup = new BoatGroup(boat, Colors.getColor());
            boatGroup.moveTo(startingX, startingY, 0d);
            boatGroup.setDestination(firstMarkX, firstMarkY);
            boatGroup.forceRotation();
            group.getChildren().add(boatGroup);
            raceObjects.add(boatGroup);
//            drawBoat(boat.getLongitude(), boat.getLatitude(), boat.getColor(), boat.getShortName(), boat.getSpeedInKnots(), boat.getHeading());
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
        fitToCanvas();
//        for (Mark mark : raceViewController.getRace().getCourse()) {
//            if (mark.getMarkType() == MarkType.SINGLE_MARK) {
//                drawSingleMark((SingleMark) mark, Color.BLACK);
//            } else {
//                drawGateMark((GateMark) mark);
//            }
//        }
//        System.out.println("MIN/MAX POINTS");
//        System.out.println(minLatPoint.getName() + " " +  minLatPoint.getX() + " " + minLatPoint.getY());
//        System.out.println(maxLatPoint.getName() + " " +  maxLatPoint.getX() + " " + maxLatPoint.getY());
//        System.out.println(minLonPoint.getName() + " " +  minLonPoint.getX() + " " + minLonPoint.getY());
//        System.out.println(maxLonPoint.getName() + " " +  maxLonPoint.getX() + " " + maxLonPoint.getY());
//        System.out.println(referencePointX);
//        System.out.println(referencePointY);
    }

//    /**
//     * Draw a given mark on canvas
//     *
//     * @param singleMark
//     */
//    private void drawSingleMark(SingleMark singleMark, Color color) {
//        gc.setFill(color);
//        System.out.println("DRAWING " + singleMark.getName() + " at "  + singleMark.getX() + ", " + singleMark.getY());
//        gc.fillOval(singleMark.getX(), singleMark.getY(),MARK_SIZE,MARK_SIZE);
//    }
//
//    /**
//     * Draw a gate mark which contains two single marks
//     *
//     * @param gateMark
//     */
//    private void drawGateMark(GateMark gateMark) {
//        Color color = Color.BLUE;
//
//        if (gateMark.getName().equals("Start")){
//            color = Color.GREEN;
//        }
//
//        if (gateMark.getName().equals("Finish")){
//            color = Color.RED;
//        }
//
//        drawSingleMark(gateMark.getSingleMark1(), color);
//        drawSingleMark(gateMark.getSingleMark2(), color);
//
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        gc.save();
//        gc.setStroke(color);
//        if (gateMark.getMarkType() == MarkType.OPEN_GATE)
//            gc.setLineDashes(3, 5);
//
//        gc.setLineWidth(2);
//        gc.strokeLine(
//                gateMark.getSingleMark1().getX() + MARK_SIZE / 2,
//                gateMark.getSingleMark1().getY() + MARK_SIZE / 2,
//                gateMark.getSingleMark2().getX() + MARK_SIZE / 2,
//                gateMark.getSingleMark2().getY() + MARK_SIZE / 2
//        );
//        gc.restore();
//    }

    /**
     * Calculates x and y location for every marker that fits it to the canvas the race will be drawn on.
     */
    private void fitToCanvas() {
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
        System.out.println("ALL POINTS");
        for (Mark m : sortedPoints)
        {
            System.out.println(m.getName() + " " +  m.getLatitude() + " " + m.getLongitude());
        }
        System.out.println("MIN/MAX POINTS");
        System.out.println(minLatPoint.getName() + " " +  minLatPoint.getLatitude() + " " + minLatPoint.getLongitude());
        System.out.println(maxLatPoint.getName() + " " +  maxLatPoint.getLatitude() + " " + maxLatPoint.getLongitude());
        System.out.println(minLonPoint.getName() + " " +  minLonPoint.getLatitude() + " " + minLonPoint.getLongitude());
        System.out.println(maxLonPoint.getName() + " " +  maxLonPoint.getLatitude() + " " + maxLonPoint.getLongitude());
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
        //double mapWidth = canvas.getWidth();
        //double mapHeight = canvas.getHeight();

        if (scaleDirection == ScaleDirection.HORIZONTAL) {
            System.out.println("HORIZONTAL");
            System.out.println("ref angle " + Mark.calculateHeadingRad(referencePoint, minLonPoint));
            //referenceAngle = Mark.calculateHeadingRad(referencePoint, minLonPoint) - (Math.PI * (3/4));
            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, minLonPoint));
            referencePointX = LHS_BUFFER + distanceScaleFactor * Math.sin(referenceAngle) * Mark.calculateDistance(referencePoint, minLonPoint);

            //referenceAngle = Mark.calculateHeadingRad(referencePoint, maxLatPoint);
            //if (referenceAngle > Math.PI) {
            //    referenceAngle = (Math.PI * 2) - referenceAngle;
            //}
            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, maxLatPoint));
            referencePointY  = CANVAS_HEIGHT - (TOP_BUFFER + BOT_BUFFER);
            referencePointY -= distanceScaleFactor * Math.cos(referenceAngle) * Mark.calculateDistance(referencePoint, maxLatPoint);
            referencePointY  = referencePointY / 2;
            referencePointY += TOP_BUFFER;
            referencePointY += distanceScaleFactor * Math.cos(referenceAngle) * Mark.calculateDistance(referencePoint, maxLatPoint);
        } else {
            System.out.println("VERTICAL");
            referencePointY = CANVAS_HEIGHT - BOT_BUFFER;

            //referenceAngle = (Math.PI * 2) - Mark.calculateHeadingRad(referencePoint, minLonPoint);
            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, minLonPoint));

            referencePointX  = LHS_BUFFER;
            referencePointX += distanceScaleFactor * Math.sin(referenceAngle) * Mark.calculateDistance(referencePoint, minLonPoint);
            referencePointX += ((CANVAS_WIDTH - (LHS_BUFFER + RHS_BUFFER)) - (minLonToMaxLon * distanceScaleFactor)) / 2;
        }
        referencePointX = Math.round(referencePointX);
        referencePointY = Math.round(referencePointY);
//        referencePoint.setX((int) referencePointX);
//        referencePoint.setY((int) referencePointY);
    }

    /**
     * Finds the scale factor necessary to fit all race markers within the onscreen map and assigns it to distanceScaleFactor
     * Returns the max horizontal distance of the map.
     */
    private double scaleRaceExtremities () {
        //double vertAngle = Mark.calculateHeadingRad(minLatPoint, maxLatPoint);
        double vertAngle = Math.abs(Mark.calculateHeadingRad(minLatPoint, maxLatPoint));
//        if (vertAngle > Math.PI)
//            vertAngle = (2 * Math.PI) - vertAngle;
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
        //Point2D canvasLocation;
        List<Mark> allPoints = new ArrayList<>(raceViewController.getRace().getCourse());
        List<Mark> processed = new ArrayList<>();
        //Set<Mark> unqiuePoints = new HashSet<>(raceViewController.getRace().getCourse());
        //System.out.println("unqiuePoints = " + unqiuePoints);
        RaceObject markGroup;

        for (Mark mark : allPoints) {
            if (!processed.contains(mark)) {
                if (mark.getMarkType() != MarkType.SINGLE_MARK) {
                    GateMark gateMark = (GateMark) mark;
//                canvasLocation = findScaledXY(gateMark.getSingleMark1());
//                gateMark.getSingleMark1().setX((int) canvasLocation.getX());
//                gateMark.getSingleMark1().setY((int) canvasLocation.getY());
//
//                canvasLocation = findScaledXY(gateMark.getSingleMark2());
//                gateMark.getSingleMark2().setX((int) canvasLocation.getX());
//                gateMark.getSingleMark2().setY((int) canvasLocation.getY());

                    markGroup = new MarkGroup(mark, findScaledXY(gateMark.getSingleMark1()), findScaledXY(gateMark.getSingleMark2()));
                    group.getChildren().add(markGroup);
                    raceObjects.add(markGroup);
                } else {
//                canvasLocation = findScaledXY(mark);
//                mark.setX((int) canvasLocation.getX());
//                mark.setY((int) canvasLocation.getY());
                    markGroup = new MarkGroup(mark, findScaledXY(mark));
                    group.getChildren().add(markGroup);
                }
                processed.add(mark);
            }
        }
    }

    private Point2D findScaledXY (Mark unscaled) {
        System.out.println("unscaled.getName() = " + unscaled.getName());
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
            //System.out.println("1");
            xAxisLocation += (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else if (angleFromReference >= 0) {
            //System.out.println("2");
            angleFromReference = angleFromReference - Math.PI / 2;
            xAxisLocation += (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            //System.out.println("3");
            System.out.println(distanceFromReference);
            angleFromReference = Math.abs(angleFromReference);
            System.out.println(Math.cos(angleFromReference) * distanceFromReference);
            xAxisLocation -= (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else {
            //System.out.println("4");
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xAxisLocation -= (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        }
//        if (angleFromReference > (Math.PI / 2)) {
//            angleFromReference = (Math.PI * 2) - angleFromReference;
//            xAxisLocation -= (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
//        } else {
//            xAxisLocation += (int) Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
//        }
//        yAxisLocation -= (int) Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        //System.out.println(xAxisLocation + " *** " + yAxisLocation);
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
}