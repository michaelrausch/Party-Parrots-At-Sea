package seng302.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import seng302.fxObjects.BoatAnnotations;
import seng302.fxObjects.BoatGroup;
import seng302.fxObjects.Wake;
import seng302.models.Colors;
import seng302.models.Yacht;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.fxObjects.MarkGroup;
import seng302.models.mark.MarkType;
import seng302.models.mark.SingleMark;
import seng302.models.map.Boundary;
import seng302.models.map.CanvasMap;
import seng302.models.stream.StreamParser;
import seng302.models.stream.XMLParser;
import seng302.models.stream.XMLParser.RaceXMLObject.Limit;
import seng302.models.stream.XMLParser.RaceXMLObject.Participant;
import seng302.models.stream.packets.BoatPositionPacket;
import seng302.server.simulator.GeoUtility;
import seng302.server.simulator.mark.Position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private ImageView mapImage;

    private final int BUFFER_SIZE   = 50;
    private final int PANEL_WIDTH   = 1260; // it should be 1280 but, minors 40 to cancel the bias.
    private final int PANEL_HEIGHT  = 960;
    private final int CANVAS_WIDTH  = 720;
    private final int CANVAS_HEIGHT = 720;
    private boolean horizontalInversion = false;

    private double distanceScaleFactor;
    private ScaleDirection scaleDirection;
    private Mark minLatPoint;
    private Mark minLonPoint;
    private Mark maxLatPoint;
    private Mark maxLonPoint;
    private double referencePointX;
    private double referencePointY;
    private double metersPerPixelX;
    private double metersPerPixelY;

    private List<MarkGroup> markGroups = new ArrayList<>();
    private List<BoatGroup> boatGroups = new ArrayList<>();
    private Text FPSdisplay = new Text();
    private Polygon raceBorder = new Polygon();

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

    public void setup(RaceViewController raceViewController) {
        this.raceViewController = raceViewController;
    }

    public void initialize() {
        raceViewController = new RaceViewController();
        canvas = new ResizableCanvas();
        group = new Group();

        // create image view for map, bind panel size to image
        mapImage = new ImageView();
        canvasPane.getChildren().add(mapImage);
        mapImage.fitWidthProperty().bind(canvasPane.widthProperty());
        mapImage.fitHeightProperty().bind(canvasPane.heightProperty());

        canvasPane.getChildren().add(canvas);
        canvasPane.getChildren().add(group);
        // Bind canvas size to stack pane size.
        canvas.widthProperty().bind(new SimpleDoubleProperty(CANVAS_WIDTH));
        canvas.heightProperty().bind(new SimpleDoubleProperty(CANVAS_HEIGHT));
    }

    public void initializeCanvas() {

        gc = canvas.getGraphicsContext2D();
        gc.setGlobalAlpha(0.5);
        fitMarksToCanvas();
        drawGoogleMap();
        FPSdisplay.setLayoutX(5);
        FPSdisplay.setLayoutY(20);
        FPSdisplay.setStrokeWidth(2);
        group.getChildren().add(FPSdisplay);
        group.getChildren().add(raceBorder);
        initializeMarks();
        initializeBoats();

        timer = new AnimationTimer() {
            private long lastTime = 0;
            private int FPSCount = 30;

            @Override
            public void handle(long now) {
                    if (lastTime == 0) {
                        lastTime = now;
                    } else {
                        if (now - lastTime >= (1e8 / 60)) { //Fix for framerate going above 60 when minimized
                            long oldFrameTime = frameTimes[frameTimeIndex];
                            frameTimes[frameTimeIndex] = now;
                            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
                            if (frameTimeIndex == 0) {
                                arrayFilled = true;
                            }
                            long elapsedNanos;
                            if (arrayFilled) {
                                elapsedNanos = now - oldFrameTime;
                                long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                                frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
                                if (FPSCount-- == 0) {
                                    FPSCount = 30;
                                    drawFps(frameRate.intValue());
                                }
                                raceViewController.updateSparkLine();
                            }
                            updateGroups();
                            if (StreamParser.isRaceFinished()) {
                                this.stop();
                            }
                            lastTime = now;
                        }
                    }
                if (StreamParser.isRaceFinished()) {
                    this.stop();
                    switchToFinishScreen();
                }
            }
        };
    }

    private void switchToFinishScreen() {
        try {
            // canvas view -> anchor pane -> grid pane -> main view
            GridPane gridPane = (GridPane) canvasPane.getParent().getParent();
            AnchorPane contentPane = (AnchorPane) gridPane.getParent();
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            contentPane.getChildren().addAll(
                (Pane) FXMLLoader.load(getClass().getResource("/views/FinishScreenView.fxml")));
        } catch (javafx.fxml.LoadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * First find the top right and bottom left points' geo locations, then retrieve
     * map from google to display on image view.  - Haoming 22/5/2017
     */
    private void drawGoogleMap() {
        findMetersPerPixel();
        Point2D topLeftPoint = findScaledXY(maxLatPoint.getLatitude(), minLonPoint.getLongitude());
        // distance from top left extreme to panel origin (top left corner)
        double distanceFromTopLeftToOrigin = Math.sqrt(
            Math.pow(topLeftPoint.getX() * metersPerPixelX, 2) + Math
                .pow(topLeftPoint.getY() * metersPerPixelY, 2));
        // angle from top left extreme to panel origin
        double bearingFromTopLeftToOrigin = Math
            .toDegrees(Math.atan2(-topLeftPoint.getX(), topLeftPoint.getY()));
        // the top left extreme
        Position topLeftPos = new Position(maxLatPoint.getLatitude(), minLonPoint.getLongitude());
        Position originPos = GeoUtility
            .getGeoCoordinate(topLeftPos, bearingFromTopLeftToOrigin, distanceFromTopLeftToOrigin);

        // distance from origin corner to bottom right corner of the panel
        double distanceFromOriginToBottomRight = Math.sqrt(
            Math.pow(PANEL_HEIGHT * metersPerPixelY, 2) + Math
                .pow(PANEL_WIDTH * metersPerPixelX, 2));
        double bearingFromOriginToBottomRight = Math
            .toDegrees(Math.atan2(PANEL_WIDTH, -PANEL_HEIGHT));
        Position bottomRightPos = GeoUtility
            .getGeoCoordinate(originPos, bearingFromOriginToBottomRight,
                distanceFromOriginToBottomRight);

        Boundary boundary = new Boundary(originPos.getLat(), bottomRightPos.getLng(),
            bottomRightPos.getLat(), originPos.getLng());
        CanvasMap canvasMap = new CanvasMap(boundary);
        mapImage.setImage(canvasMap.getMapImage());
    }

    /**
     * Adds border marks to the canvas, taken from the XML file
     *
     * NOTE: This is quite confusing as objects are grabbed from the XMLParser such as Mark and
     * CompoundMark which are named the same as those in the model package but are, however not the
     * same, so they do not have things such as a type and must be derived from the number of marks
     * in a compound mark etc..
     */
    private void addRaceBorder() {
        XMLParser.RaceXMLObject raceXMLObject = StreamParser.getXmlObject().getRaceXML();
        ArrayList<Limit> courseLimits = raceXMLObject.getCourseLimit();
        raceBorder.setStroke(new Color(0.0f, 0.0f, 0.74509807f, 1));
        raceBorder.setStrokeWidth(3);
        raceBorder.setFill(new Color(0,0,0,0));
        List<Double> boundaryPoints = new ArrayList<>();
        for (Limit limit : courseLimits) {
            Point2D location = findScaledXY(limit.getLat(), limit.getLng());
            boundaryPoints.add(location.getX());
            boundaryPoints.add(location.getY());
        }
        raceBorder.getPoints().setAll(boundaryPoints);
    }

    private void updateGroups() {
        for (BoatGroup boatGroup : boatGroups) {
            // some raceObjects will have multiple ID's (for instance gate marks)
            //checking if the current "ID" has any updates associated with it
            if (StreamParser.boatLocations.containsKey(boatGroup.getRaceId())) {
                if (boatGroup.isStopped()) {
                    updateBoatGroup(boatGroup);
                }
            }
            boatGroup.move();
        }
        for (MarkGroup markGroup : markGroups) {
            for (Long id : markGroup.getRaceIds()) {
                if (StreamParser.markLocations.containsKey(id)) {
                    updateMarkGroup(id, markGroup);
                }
            }
        }
        checkForCourseChanges();
    }

    private void checkForCourseChanges() {
        if (StreamParser.isNewRaceXmlReceived()){
            addRaceBorder();
        }
    }

    private void updateBoatGroup(BoatGroup boatGroup) {
        PriorityBlockingQueue<BoatPositionPacket> movementQueue = StreamParser.boatLocations.get(boatGroup.getRaceId());
        // giving the movementQueue a 5 packet buffer to account for slightly out of order packets
        if (movementQueue.size() > 0) {
            try {
                BoatPositionPacket positionPacket = movementQueue.take();
                Point2D p2d = findScaledXY(positionPacket.getLat(), positionPacket.getLon());
                double heading = 360.0 / 0xffff * positionPacket.getHeading();
                boatGroup.setDestination(
                    p2d.getX(), p2d.getY(), heading, positionPacket.getGroundSpeed(),
                    positionPacket.getTimeValid(), frameRate);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
//            }
        }
    }

    void updateMarkGroup (long raceId, MarkGroup markGroup) {
        PriorityBlockingQueue<BoatPositionPacket> movementQueue = StreamParser.markLocations.get(raceId);
        if (movementQueue.size() > 0){
            try {
                BoatPositionPacket positionPacket = movementQueue.take();
                Point2D p2d = findScaledXY(positionPacket.getLat(), positionPacket.getLon());
                markGroup.moveMarkTo(p2d.getX(), p2d.getY(), raceId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Draws all the boats.
     */
    private void initializeBoats() {
        Map<Integer, Yacht> boats = StreamParser.getBoats();
        Group wakes = new Group();
        Group trails = new Group();
        Group annotations = new Group();

        ArrayList<Participant> participants = StreamParser.getXmlObject().getRaceXML()
            .getParticipants();
        ArrayList<Integer> participantIDs = new ArrayList<>();
        for (Participant p : participants) {
            participantIDs.add(p.getsourceID());
        }

        for (Yacht boat : boats.values()) {
            if (participantIDs.contains(boat.getSourceID())) {
                boat.setColour(Colors.getColor());
                BoatGroup boatGroup = new BoatGroup(boat, boat.getColour());
                boatGroups.add(boatGroup);
                trails.getChildren().add(boatGroup.getTrail());
                wakes.getChildren().add(boatGroup.getWake());
                annotations.getChildren().add(boatGroup.getAnnotations());
            }
        }
        group.getChildren().addAll(trails);
        group.getChildren().addAll(wakes);
        group.getChildren().addAll(annotations);
        group.getChildren().addAll(boatGroups);
    }

    private void initializeMarks() {
        List<Mark> allMarks = StreamParser.getXmlObject().getRaceXML().getNonDupCompoundMarks();
        for (Mark mark : allMarks) {
            if (mark.getMarkType() == MarkType.SINGLE_MARK) {
                SingleMark sMark = (SingleMark) mark;

                MarkGroup markGroup = new MarkGroup(sMark, findScaledXY(sMark));
                markGroups.add(markGroup);
            } else {
                GateMark gMark = (GateMark) mark;

                MarkGroup markGroup = new MarkGroup(gMark, findScaledXY(gMark.getSingleMark1()),
                    findScaledXY(gMark.getSingleMark2())); //should be 2 objects in the list.
                markGroups.add(markGroup);
            }
        }
        group.getChildren().addAll(markGroups);
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
            FPSdisplay.setVisible(true);
            FPSdisplay.setText(String.format("%d FPS", fps));
        } else {
            FPSdisplay.setVisible(false);
        }
    }

    /**
     * Calculates x and y location for every marker that fits it to the canvas the race will be
     * drawn on.
     */
    private void fitMarksToCanvas() {
        //Check is called once to avoid unnecessarily change the course limits once the race is running
        StreamParser.isNewRaceXmlReceived();
        findMinMaxPoint();
        double minLonToMaxLon = scaleRaceExtremities();
        calculateReferencePointLocation(minLonToMaxLon);
        //givePointsXY();
        addRaceBorder();
    }


    /**
     * Sets the class variables minLatPoint, maxLatPoint, minLonPoint, maxLonPoint to the marker
     * with the leftmost marker, rightmost marker, southern most marker and northern most marker
     * respectively.
     */
    private void findMinMaxPoint() {
        List<Limit> sortedPoints = new ArrayList<>();
        for (Limit limit : StreamParser.getXmlObject().getRaceXML().getCourseLimit()) {
            sortedPoints.add(limit);
        }
        sortedPoints.sort(Comparator.comparingDouble(Limit::getLat));
        Limit minLatMark = sortedPoints.get(0);
        Limit maxLatMark = sortedPoints.get(sortedPoints.size()-1);
        minLatPoint = new SingleMark(minLatMark.toString(), minLatMark.getLat(), minLatMark.getLng(), minLatMark.getSeqID(), minLatMark.getSeqID());
        maxLatPoint = new SingleMark(maxLatMark.toString(), maxLatMark.getLat(), maxLatMark.getLng(), maxLatMark.getSeqID(), minLatMark.getSeqID());

        sortedPoints.sort(Comparator.comparingDouble(Limit::getLng));
        //If the course is on a point on the earth where longitudes wrap around.
        Limit minLonMark = sortedPoints.get(0);
        Limit maxLonMark = sortedPoints.get(sortedPoints.size()-1);
        minLonPoint = new SingleMark(minLonMark.toString(), minLonMark.getLat(), minLonMark.getLng(), minLonMark.getSeqID(), minLonMark.getSeqID());
        maxLonPoint = new SingleMark(maxLonMark.toString(), maxLonMark.getLat(), maxLonMark.getLng(), maxLonMark.getSeqID(), minLonMark.getSeqID());
        if (maxLonPoint.getLongitude() - minLonPoint.getLongitude() > 180) {
            horizontalInversion = true;
        }
    }

    /**
     * Calculates the location of a reference point, this is always the point with minimum latitude,
     * in relation to the canvas.
     *
     * @param minLonToMaxLon The horizontal distance between the point of minimum longitude to
     * maximum longitude.
     */
    private void calculateReferencePointLocation(double minLonToMaxLon) {
        Mark referencePoint = minLatPoint;
        double referenceAngle;

        if (scaleDirection == ScaleDirection.HORIZONTAL) {
            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, minLonPoint));
            referencePointX = BUFFER_SIZE + distanceScaleFactor * Math.sin(referenceAngle) * Mark.calculateDistance(referencePoint, minLonPoint);

            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, maxLatPoint));
            referencePointY  = CANVAS_HEIGHT - (BUFFER_SIZE + BUFFER_SIZE);
            referencePointY -= distanceScaleFactor * Math.cos(referenceAngle) * Mark.calculateDistance(referencePoint, maxLatPoint);
            referencePointY  = referencePointY / 2;
            referencePointY += BUFFER_SIZE;
            referencePointY += distanceScaleFactor * Math.cos(referenceAngle) * Mark.calculateDistance(referencePoint, maxLatPoint);
        } else {
            referencePointY = CANVAS_HEIGHT - BUFFER_SIZE;

            referenceAngle = Math.abs(Mark.calculateHeadingRad(referencePoint, minLonPoint));
            referencePointX  = BUFFER_SIZE;
            referencePointX += distanceScaleFactor * Math.sin(referenceAngle) * Mark.calculateDistance(referencePoint, minLonPoint);
            referencePointX += ((CANVAS_WIDTH - (BUFFER_SIZE + BUFFER_SIZE)) - (minLonToMaxLon * distanceScaleFactor)) / 2;
        }
        if(horizontalInversion) {
            referencePointX = CANVAS_WIDTH - BUFFER_SIZE - (referencePointX - BUFFER_SIZE);
        }
    }


    /**
     * Finds the scale factor necessary to fit all race markers within the onscreen map and assigns
     * it to distanceScaleFactor Returns the max horizontal distance of the map.
     */
    private double scaleRaceExtremities() {

        double vertAngle = Math.abs(Mark.calculateHeadingRad(minLatPoint, maxLatPoint));
        double vertDistance =
            Math.cos(vertAngle) * Mark.calculateDistance(minLatPoint, maxLatPoint);
        double horiAngle = Mark.calculateHeadingRad(minLonPoint, maxLonPoint);

        if (horiAngle <= (Math.PI / 2)) {
            horiAngle = (Math.PI / 2) - horiAngle;
        } else {
            horiAngle = horiAngle - (Math.PI / 2);
        }
        double horiDistance =
            Math.cos(horiAngle) * Mark.calculateDistance(minLonPoint, maxLonPoint);

        double vertScale = (CANVAS_HEIGHT - (BUFFER_SIZE + BUFFER_SIZE)) / vertDistance;

        if ((horiDistance * vertScale) > (CANVAS_WIDTH - (BUFFER_SIZE + BUFFER_SIZE))) {
            distanceScaleFactor = (CANVAS_WIDTH - (BUFFER_SIZE + BUFFER_SIZE)) / horiDistance;
            scaleDirection = ScaleDirection.HORIZONTAL;
        } else {
            distanceScaleFactor = vertScale;
            scaleDirection = ScaleDirection.VERTICAL;
        }
        return horiDistance;
    }

    private Point2D findScaledXY(Mark unscaled) {
        return findScaledXY(unscaled.getLatitude(), unscaled.getLongitude());
    }

    public Point2D findScaledXY (double unscaledLat, double unscaledLon) {
        double distanceFromReference;
        double angleFromReference;
        int xAxisLocation = (int) referencePointX;
        int yAxisLocation = (int) referencePointY;

        angleFromReference = Mark
            .calculateHeadingRad(minLatPoint.getLatitude(), minLatPoint.getLongitude(), unscaledLat,
                unscaledLon);
        distanceFromReference = Mark
            .calculateDistance(minLatPoint.getLatitude(), minLatPoint.getLongitude(), unscaledLat,
                unscaledLon);
        if (angleFromReference >= 0 && angleFromReference <= Math.PI / 2) {
            xAxisLocation += (int) Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= (int) Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else if (angleFromReference >= 0) {
            angleFromReference = angleFromReference - Math.PI / 2;
            xAxisLocation += (int) Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += (int) Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            angleFromReference = Math.abs(angleFromReference);
            xAxisLocation -= (int) Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= (int) Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else {
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xAxisLocation -= (int) Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += (int) Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        }
        if(horizontalInversion) {
            xAxisLocation = CANVAS_WIDTH - BUFFER_SIZE - (xAxisLocation - BUFFER_SIZE);
        }
        return new Point2D(xAxisLocation, yAxisLocation);
    }

    /**
     * Find the number of meters per pixel.
     */
    private void findMetersPerPixel() {
        Point2D p1, p2;
        Mark m1, m2;
        double theta, distance, dx, dy, dHorizontal, dVertical;
        m1 = new SingleMark("m1", maxLatPoint.getLatitude(), minLonPoint.getLongitude(), 1, 0);
        m2 = new SingleMark("m2", minLatPoint.getLatitude(), maxLonPoint.getLongitude(), 2, 0);
        p1 = findScaledXY(m1);
        p2 = findScaledXY(m2);
        theta = Mark.calculateHeadingRad(m1, m2);
        distance = Mark.calculateDistance(m1, m2);
        dHorizontal = Math.abs(Math.sin(theta) * distance);
        dVertical = Math.abs(Math.cos(theta) * distance);
        dx = Math.abs(p1.getX() - p2.getX());
        dy = Math.abs(p1.getY() - p2.getY());
        metersPerPixelX = dHorizontal / dx;
        metersPerPixelY = dVertical / dy;
    }

    List<BoatGroup> getBoatGroups() {
        return boatGroups;
    }

    List<MarkGroup> getMarkGroups() {
        return  markGroups;
    }
}