package seng302.controllers;

import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import seng302.models.*;
import seng302.models.mark.*;
import seng302.models.stream.StreamParser;
import seng302.models.stream.packets.BoatPositionPacket;
import seng302.models.stream.XMLParser;
import seng302.models.stream.XMLParser.RaceXMLObject.Limit;
import seng302.models.mark.Mark;
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
    private final int BUFFER_SIZE   = 50;
    private final int CANVAS_WIDTH  = 720;
    private final int CANVAS_HEIGHT = 720;
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

    private List<MarkGroup> markGroups = new ArrayList<>();
    private List<BoatGroup> boatGroups = new ArrayList<>();

    //FRAME RATE
    private Double frameRate = 60.0;
    private final long[] frameTimes = new long[30];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

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
    }

    public void initializeCanvas (){

        gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setFill(Color.SKYBLUE);
        gc.fillRect(0,0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gc.restore();
        fitMarksToCanvas();


        // TODO: 1/05/17 wmu16 - Change this call to now draw the marks as from the xml
        initializeBoats();
        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                //fps stuff
                long oldFrameTime = frameTimes[frameTimeIndex] ;
                frameTimes[frameTimeIndex] = now ;
                frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length ;
                if (frameTimeIndex == 0) {
                    arrayFilled = true ;
                }
                long elapsedNanos;
                if (arrayFilled) {
                    elapsedNanos = now - oldFrameTime ;
                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length ;
                    frameRate = 1_000_000_000.0 / elapsedNanosPerFrame ;
                    drawFps(frameRate.intValue());
                }

                // TODO: 1/05/17 cir27 - Make the RaceObjects update on the actual delay.
                elapsedNanos = 1000 / 60;
                updateGroups();
                if (StreamParser.isRaceFinished()) {
                    this.stop();
                }
            }
        };
    }


    /**
     * Adds border marks to the canvas, taken from the XML file
     *
     * NOTE: This is quite confusing as objects are grabbed from the XMLParser such as Mark and CompoundMark which are
     * named the same as those in the model package but are, however not the same, so they do not have things such as
     * a type and must be derived from the number of marks in a compound mark etc..
     */
    private void addRaceBorder() {
        XMLParser.RaceXMLObject raceXMLObject = StreamParser.getXmlObject().getRaceXML();
        ArrayList<Limit> courseLimits = raceXMLObject.getCourseLimit();
        gc.setStroke(Color.DARKBLUE);
        gc.setLineWidth(3);
        double[] xBoundaryPoints = new double[courseLimits.size()];
        double[] yBoundaryPoints = new double[courseLimits.size()];
        for (int i = 0; i < courseLimits.size() - 1; i++) {
            Limit thisPoint1 = courseLimits.get(i);
            SingleMark thisMark1 = new SingleMark("", thisPoint1.getLat(), thisPoint1.getLng(), thisPoint1.getSeqID());
            Limit thisPoint2 = courseLimits.get(i+1);
            SingleMark thisMark2 = new SingleMark("", thisPoint2.getLat(), thisPoint2.getLng(), thisPoint2.getSeqID());
            Point2D borderPoint1 = findScaledXY(thisMark1);
            Point2D borderPoint2 = findScaledXY(thisMark2);
            gc.strokeLine(borderPoint1.getX(), borderPoint1.getY(),
                borderPoint2.getX(), borderPoint2.getY());
            xBoundaryPoints[i] = borderPoint1.getX();
            yBoundaryPoints[i] = borderPoint1.getY();
        }
        Limit thisPoint1 = courseLimits.get(courseLimits.size()-1);
        SingleMark thisMark1 = new SingleMark("", thisPoint1.getLat(), thisPoint1.getLng(), thisPoint1.getSeqID());
        Limit thisPoint2 = courseLimits.get(0);
        SingleMark thisMark2 = new SingleMark("", thisPoint2.getLat(), thisPoint2.getLng(), thisPoint2.getSeqID());
        Point2D borderPoint1 = findScaledXY(thisMark1);
        Point2D borderPoint2 = findScaledXY(thisMark2);
        gc.strokeLine(borderPoint1.getX(), borderPoint1.getY(),
            borderPoint2.getX(), borderPoint2.getY());
        xBoundaryPoints[courseLimits.size()-1] = borderPoint1.getX();
        yBoundaryPoints[courseLimits.size()-1] = borderPoint1.getY();
        gc.setFill(Color.LIGHTBLUE);
        gc.fillPolygon(xBoundaryPoints,yBoundaryPoints,yBoundaryPoints.length);
    }

    private void updateGroups(){
        for (BoatGroup boatGroup : boatGroups) {
            // some raceObjects will have multiple ID's (for instance gate marks)
            //checking if the current "ID" has any updates associated with it
            if (StreamParser.boatPositions.containsKey(boatGroup.getRaceId())) {
                if (boatGroup.isStopped()) {
                    updateBoatGroup(boatGroup);
                }
            }
            boatGroup.move();
        }
        for (MarkGroup markGroup : markGroups) {
            for (int id : markGroup.getRaceIds()) {
                if (StreamParser.boatPositions.containsKey(id)) {
                    UpdateMarkGroup(id, markGroup);
                }
            }
        }
    }

    private void updateBoatGroup(BoatGroup boatGroup) {
        PriorityBlockingQueue<BoatPositionPacket> movementQueue = StreamParser.boatPositions.get(boatGroup.getRaceId());
        // giving the movementQueue a 5 packet buffer to account for slightly out of order packets
        if (movementQueue.size() > 5){
            try {
                BoatPositionPacket positionPacket = movementQueue.take();
                Point2D p2d = findScaledXY(positionPacket.getLat(), positionPacket.getLon());
                if (boatGroup.getRaceId() == 106){
//                    System.out.println("p2d.getX() = " + p2d.getX());
//                    System.out.println("p2d.getY() = " + p2d.getY());
//                    System.out.println("positionPacket.getTimeValid() = " + positionPacket.getTimeValid());
                }
                double heading = 360.0 / 0xffff * positionPacket.getHeading();
                boatGroup.setDestination(p2d.getX(), p2d.getY(), heading, positionPacket.getGroundSpeed(), positionPacket.getTimeValid(), frameRate, boatGroup.getRaceId());
            } catch (InterruptedException e){
                e.printStackTrace();
            }
//            }
        }
    }

    void UpdateMarkGroup (int raceId, MarkGroup markGroup) {
        PriorityBlockingQueue<BoatPositionPacket> movementQueue = StreamParser.boatPositions.get(raceId);
        if (movementQueue.size() > 0){
            try {
                BoatPositionPacket positionPacket = movementQueue.take();
                Point2D p2d = findScaledXY(positionPacket.getLat(), positionPacket.getLon());
                markGroup.moveMarkTo(p2d.getX(), p2d.getY(), raceId);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Draws all the boats.
     */
    private void initializeBoats() {
        Map<Integer, Yacht> boats = StreamParser.getBoats();
        Group boatAnnotations = new Group();

        for (Yacht boat : boats.values()) {
            boat.setColour(Colors.getColor());
            BoatGroup boatGroup = new BoatGroup(boat, boat.getColour());
            boatGroups.add(boatGroup);
            boatAnnotations.getChildren().add(boatGroup.getLowPriorityAnnotations());
        }
        group.getChildren().add(boatAnnotations);
        group.getChildren().addAll(boatGroups);
    }

    class ResizableCanvas extends Canvas {

        ResizableCanvas() {
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
            gc.setFill(Color.SKYBLUE);
            gc.fillRect(4,4,51,21);
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
     * Calculates x and y location for every marker that fits it to the canvas the race will be drawn on.
     */
    private void fitMarksToCanvas() {
        findMinMaxPoint();
        double minLonToMaxLon = scaleRaceExtremities();
        calculateReferencePointLocation(minLonToMaxLon);
        //givePointsXY();
        addRaceBorder();
    }


    /**
     * Sets the class variables minLatPoint, maxLatPoint, minLonPoint, maxLonPoint to the marker with the leftmost
     * marker, rightmost marker, southern most marker and northern most marker respectively.
     */
    private void findMinMaxPoint() {
        List<Limit> sortedPoints = new ArrayList<>();
        for (Limit limit : StreamParser.getXmlObject().getRaceXML().getCourseLimit()) {
            sortedPoints.add(limit);
        }
        sortedPoints.sort(Comparator.comparingDouble(Limit::getLat));
        Limit minLatMark = sortedPoints.get(0);
        Limit maxLatMark = sortedPoints.get(sortedPoints.size()-1);
        minLatPoint = new SingleMark(minLatMark.toString(), minLatMark.getLat(), minLatMark.getLng(), minLatMark.getSeqID());
        maxLatPoint = new SingleMark(maxLatMark.toString(), maxLatMark.getLat(), maxLatMark.getLng(), maxLatMark.getSeqID());

        sortedPoints.sort(Comparator.comparingDouble(Limit::getLng));
        //If the course is on a point on the earth where longitudes wrap around.
        Limit minLonMark = sortedPoints.get(0);
        Limit maxLonMark = sortedPoints.get(sortedPoints.size()-1);
        SingleMark thisMinLon = new SingleMark(minLonMark.toString(), minLonMark.getLat(), minLonMark.getLng(), minLonMark.getSeqID());
        SingleMark thisMaxLon = new SingleMark(maxLonMark.toString(), maxLonMark.getLat(), maxLonMark.getLng(), maxLonMark.getSeqID());
        // TODO: 30/03/17 cir27 - Correctly account for longitude wrapping around.
        if (thisMaxLon.getLongitude() - thisMinLon.getLongitude() > 180) {
            SingleMark temp = thisMinLon;
            thisMinLon = thisMaxLon;
            thisMaxLon = temp;
        }
        minLonPoint = thisMinLon;
        maxLonPoint = thisMaxLon;
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
    // DEPRECATED create an initialize marks method like the initialize boats method
    private void givePointsXY() {
//        List<XMLParser.RaceXMLObject.CompoundMark> allPoints = StreamParser.getXmlObject().getRaceXML().getCompoundMarks();
//        List<XMLParser.RaceXMLObject.CompoundMark> processed = new ArrayList<>();
//        MarkGroup markGroup;
//
//        for (XMLParser.RaceXMLObject.CompoundMark mark : allPoints) {
//            if (!processed.contains(mark)) {
//                if (mark.getMarkType() != MarkType.SINGLE_MARK) {
//                    markGroup = new MarkGroup(mark, findScaledXY(mark.getMarks().get(0)), findScaledXY(mark.getMarks().get(1)));
//                    markGroups.add(markGroup);
//                } else {
//                    markGroup = new MarkGroup(mark, findScaledXY(mark.getMarks().get(0)));
//                    markGroups.add(markGroup);
//                }
//                processed.add(mark);
//            }
//        }
        group.getChildren().addAll(boatGroups);
    }

    private Point2D findScaledXY (Mark unscaled) {
        return findScaledXY (unscaled.getLatitude(), unscaled.getLongitude());
    }

    private Point2D findScaledXY (double unscaledLat, double unscaledLon) {
        double distanceFromReference;
        double angleFromReference;
        int xAxisLocation = (int) referencePointX;
        int yAxisLocation = (int) referencePointY;

        angleFromReference = Mark.calculateHeadingRad(minLatPoint.getLatitude(), minLatPoint.getLongitude(), unscaledLat, unscaledLon);
        distanceFromReference = Mark.calculateDistance(minLatPoint.getLatitude(), minLatPoint.getLongitude(), unscaledLat, unscaledLon);
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

    List<BoatGroup> getBoatGroups() {
        return boatGroups;
    }
}