package seng302.visualiser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import seng302.model.Colors;
import seng302.model.GeoPoint;
import seng302.model.Limit;
import seng302.model.Yacht;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.utilities.GeoUtility;
import seng302.visualiser.fxObjects.AnnotationBox;
import seng302.visualiser.fxObjects.BoatObject;
import seng302.visualiser.fxObjects.CourseBoundary;
import seng302.visualiser.fxObjects.Gate;
import seng302.visualiser.fxObjects.Marker;
import seng302.visualiser.map.Boundary;
import seng302.visualiser.map.CanvasMap;

/**
 * Created by cir27 on 20/07/17.
 */
public class GameView extends Pane {

    private double bufferSize   = 50;
    private double panelWidth   = 1260; // it should be 1280 but, minors 40 to cancel the bias.
    private double panelHeight  = 960;
    private double canvasWidth  = 1100;
    private double canvasHeight = 920;
    private boolean horizontalInversion = false;

    private double distanceScaleFactor;
    private ScaleDirection scaleDirection;
    private GeoPoint minLatPoint, minLonPoint, maxLatPoint, maxLonPoint;
    private double referencePointX, referencePointY;
    private double metersPerPixelX, metersPerPixelY;

    private Text fpsDisplay = new Text();
    private Polygon raceBorder = new CourseBoundary();

    /* Note that if either of these is null then values for it have not been added and the other
       should be used as the limits of the map. */
    private List<Limit> borderPoints;
    private List<CompoundMark> course;
    private Map<Mark, Marker> markerObjects;

    private Map<Yacht, BoatObject> boatObjects = new HashMap<>();
    private Map<Yacht, AnnotationBox> annotations = new HashMap<>();
    private List<Corner> markSequence;
    private ObservableList<Node> gameObjects;

    private ImageView mapImage = new ImageView();

    //FRAME RATE
    private Double frameRate = 60.0;
    private final long[] frameTimes = new long[30];
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;

    private AnimationTimer timer;

    private enum ScaleDirection {
        HORIZONTAL,
        VERTICAL
    }

    public GameView () {
        gameObjects = this.getChildren();
        // create image view for map, bind panel size to image
//        mapImage.fitWidthProperty().bind(this.widthProperty());
//        mapImage.fitHeightProperty().bind(this.heightProperty());
        gameObjects.add(mapImage);
        fpsDisplay.setLayoutX(5);
        fpsDisplay.setLayoutY(20);
        fpsDisplay.setStrokeWidth(2);
        gameObjects.add(fpsDisplay);
        gameObjects.add(raceBorder);
        initializeTimer();
//        this.widthProperty().addListener(resize -> {
//            canvasWidth = this.getWidth();
//            canvasHeight = this.getHeight();
//            if (borderPoints != null) {
//                updateBorder(borderPoints);
//            } else if (course != null) {
//                updateCourse(course, markSequence);
//            }
//        });
    }

    private void initializeTimer () {
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
                        }
                        boatObjects.forEach((boat, boatObject) -> boatObject.updateLocation());
                        lastTime = now;
                    }
                }
            }
        };
    }

    /**
     * First find the top right and bottom left points' geo locations, then retrieve
     * map from google to display on image view.  - Haoming 22/5/2017
     */
    private void drawGoogleMap() {
        findMetersPerPixel();
        Point2D topLeftPoint = findScaledXY(maxLatPoint.getLat(), minLonPoint.getLng());
        // distance from top left extreme to panel origin (top left corner)
        double distanceFromTopLeftToOrigin = Math.sqrt(
            Math.pow(topLeftPoint.getX() * metersPerPixelX, 2) + Math
                .pow(topLeftPoint.getY() * metersPerPixelY, 2));
        // angle from top left extreme to panel origin
        double bearingFromTopLeftToOrigin = Math
            .toDegrees(Math.atan2(-topLeftPoint.getX(), topLeftPoint.getY()));
        // the top left extreme
        GeoPoint topLeftPos = new GeoPoint(maxLatPoint.getLat(), minLonPoint.getLng());
        GeoPoint originPos = GeoUtility
            .getGeoCoordinate(topLeftPos, bearingFromTopLeftToOrigin, distanceFromTopLeftToOrigin);

        // distance from origin corner to bottom right corner of the panel
        double distanceFromOriginToBottomRight = Math.sqrt(
            Math.pow(panelHeight * metersPerPixelY, 2) + Math
                .pow(panelWidth * metersPerPixelX, 2));
        double bearingFromOriginToBottomRight = Math
            .toDegrees(Math.atan2(panelWidth, -panelHeight));
        GeoPoint bottomRightPos = GeoUtility
            .getGeoCoordinate(originPos, bearingFromOriginToBottomRight,
                distanceFromOriginToBottomRight);

        Boundary boundary = new Boundary(originPos.getLat(), bottomRightPos.getLng(),
            bottomRightPos.getLat(), originPos.getLng());
        CanvasMap canvasMap = new CanvasMap(boundary);
        mapImage.setImage(canvasMap.getMapImage());
    }

    /**
     * Adds a course to the GameView. The view is scaled accordingly unless a border is set in which
     * case the course is added relative ot the border.
     *
     * @param newCourse the mark objects that make up the course.
     */
    public void updateCourse(List<CompoundMark> newCourse, List<Corner> sequence) {
        course = newCourse;
        this.markSequence = sequence;
        markerObjects = new HashMap<>();
        Paint colour = Color.BLACK;
        //Creates new markers
        for (CompoundMark cMark : course) {
            //Set start and end colour
            if (cMark.getId() == sequence.get(0).getCompoundMarkID()) {
                colour = Color.GREEN;
            } else if (cMark.getId() == sequence.get(sequence.size() - 1).getCompoundMarkID()) {
                colour = Color.RED;
            }
            //Create mark dots
            for (Mark mark : cMark.getMarks()) {
                makeAndBindMarker(mark, colour);
            }
            //Create gate line
            if (cMark.isGate()) {
                for (int i = 1; i < cMark.getMarks().size(); i++) {
                    makeAndBindGate(
                        markerObjects.get(cMark.getSubMark(i)),
                        markerObjects.get(cMark.getSubMark(i+1)),
                        colour
                    );
                }
            }
            colour = Color.BLACK;
        }
        //Set X,Y co-ordinates
        if (borderPoints == null) {
            rescaleRace(new ArrayList<>(markerObjects.keySet()));
        } else {
            rescaleRace(new ArrayList<>(borderPoints));
        }
        //Move the Markers to initial position.
        markerObjects.forEach(((mark, marker) -> {
            Point2D p2d = findScaledXY(mark.getLat(), mark.getLng());
            marker.setCenterX(p2d.getX());
            marker.setCenterY(p2d.getY());
        }));
    }

    /**
     * Creates a new Marker and binds it's position to the given Mark.
     *
     * @param observableMark The mark to bind the marker to.
     * @param colour The desired colour of the mark
     */
    private void makeAndBindMarker(Mark observableMark, Paint colour) {
        Marker marker = new Marker(colour);
        markerObjects.put(observableMark, marker);
        observableMark.addPositionListener((mark, lat, lon) -> {
            Point2D p2d = findScaledXY(lat, lon);
            markerObjects.get(mark).setCenterX(p2d.getX());
            markerObjects.get(mark).setCenterY(p2d.getY());
        });
    }

    /**
     * Creates a new gate connecting the given marks.
     *
     * @param m1 The first Mark of the gate.
     * @param m2 The second Mark of the gate.
     * @param colour The desired colour of the gate.
     */
    private void makeAndBindGate(Marker m1, Marker m2, Paint colour) {
        Gate gate = new Gate(colour);
        gate.startXProperty().bind(
            m1.centerXProperty()
        );
        gate.startYProperty().bind(
            m1.centerYProperty()
        );
        gate.endXProperty().bind(
            m2.centerXProperty()
        );
        gate.endYProperty().bind(
            m2.centerYProperty()
        );
    }

    /**
     * Adds a border to the GameView and rescales to the size of the border, does not rescale if a
     * border already exists. Assumes the border is larger than the course.
     *
     * @param border the race border to be drawn.
     */
    public void updateBorder(List<Limit> border) {
        if (borderPoints == null) {
            borderPoints = border;
            rescaleRace(new ArrayList<>(borderPoints));
        }
        List<Double> boundaryPoints = new ArrayList<>();
        for (Limit limit : border) {
            Point2D location = findScaledXY(limit.getLat(), limit.getLng());
            boundaryPoints.add(location.getX());
            boundaryPoints.add(location.getY());
        }
        raceBorder.getPoints().setAll(boundaryPoints);
    }

    /**
     * Rescales the race to the size of the window.
     *
     * @param limitingCoordinates the set of geo points that contains the extremities of the race.
     */
    private void rescaleRace(List<GeoPoint> limitingCoordinates) {
        //Check is called once to avoid unnecessarily change the course limits once the race is running
        findMinMaxPoint(limitingCoordinates);
        double minLonToMaxLon = scaleRaceExtremities();
        calculateReferencePointLocation(minLonToMaxLon);
        drawGoogleMap();
    }

    /**
     * Draws all the boats.
     */
    public void setBoats(List<Yacht> yachts) {
        Group annotationsGroup = new Group();
        Group wakesGroup = new Group();
        Group boatObjectGroup = new Group();
        Group trails = new Group();

        BoatObject newBoat;
        for (Yacht yacht : yachts) {
            newBoat = new BoatObject();
            newBoat.setFill(Colors.getColor());
            boatObjects.put(yacht, newBoat);
            yacht.addLocationListener((boat, lat, lon, heading, velocity) ->{
                BoatObject bo = boatObjects.get(boat);
                Point2D p2d = findScaledXY(lat, lon);
                bo.moveTo(p2d.getX(), p2d.getY(), heading);
                bo.setTrajectory(
                    heading,
                    velocity,
                    metersPerPixelX,
                    metersPerPixelY);
            });
            createAndBindAnnotationBox(yacht);
            wakesGroup.getChildren().add(newBoat.getWake());
            boatObjectGroup.getChildren().add(newBoat);
            trails.getChildren().add(newBoat.getTrail());
        }
        annotationsGroup.getChildren().addAll(annotations.values());
        gameObjects.addAll(trails);
        gameObjects.addAll(annotationsGroup);
        gameObjects.addAll(boatObjectGroup);
    }

    private void createAndBindAnnotationBox (Yacht yacht) {
        AnnotationBox newAnnotation;
        newAnnotation = new AnnotationBox();
        newAnnotation.addAnnotation("name", yacht.getShortName());
//        newAnnotation.addAnnotation("country", boat.getCountry());
        newAnnotation.addAnnotation(
            "velocity",
            yacht.getVelocityProperty(),
            (velocity) -> String.format("%.2f ms", velocity.doubleValue())
        );
        newAnnotation.addAnnotation(
            "nextMark",
            yacht.timeTillNextProperty(),
            (time) -> {
                DateFormat format = new SimpleDateFormat("mm:ss");
                return format.format(time);
            }
        );
        newAnnotation.addAnnotation(
            "lastMark",
            yacht.timeTillNextProperty(),
            (time) -> {
                DateFormat format = new SimpleDateFormat("mm:ss");
                return format.format(time);
            }
        );
        annotations.put(yacht, newAnnotation);
    }

    private void drawFps(int fps){
        fpsDisplay.setText(String.format("%d FPS", fps));
    }

    /**
     * Sets the class variables minLatPoint, maxLatPoint, minLonPoint, maxLonPoint to the point
     * with the leftmost point, rightmost point, southern most point and northern most point
     * respectively.
     */
    private void findMinMaxPoint(List<GeoPoint> points) {
        List<GeoPoint> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort(Comparator.comparingDouble(GeoPoint::getLat));
        for (GeoPoint gp : sortedPoints) {
            System.out.println(gp.getLat());
        }
        minLatPoint = new GeoPoint(sortedPoints.get(0).getLat(), sortedPoints.get(0).getLng());
        GeoPoint maxLat = sortedPoints.get(sortedPoints.size()-1);
        maxLatPoint = new GeoPoint(maxLat.getLat(), maxLat.getLng());

        sortedPoints.sort(Comparator.comparingDouble(GeoPoint::getLng));
        minLonPoint = new GeoPoint(sortedPoints.get(0).getLat(), sortedPoints.get(0).getLng());
        GeoPoint maxLon = sortedPoints.get(sortedPoints.size()-1);
        maxLonPoint = new GeoPoint(maxLon.getLat(), maxLon.getLng());
        if (maxLonPoint.getLng() - minLonPoint.getLng() > 180) {
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
        GeoPoint referencePoint = minLatPoint;
        double referenceAngle;

        if (scaleDirection == ScaleDirection.HORIZONTAL) {
            referenceAngle = Math.abs(
                    GeoUtility.getBearingRad(referencePoint, minLonPoint)
            );
            referencePointX = bufferSize + distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility.getDistance(referencePoint, minLonPoint);
            referenceAngle = Math.abs(GeoUtility.getDistance(referencePoint, maxLatPoint));
            referencePointY  = canvasHeight - (bufferSize + bufferSize);
            referencePointY -= distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility.getDistance(referencePoint, maxLatPoint);
            referencePointY  = referencePointY / 2;
            referencePointY += bufferSize;
            referencePointY += distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility.getDistance(referencePoint, maxLatPoint);
        } else {
            referencePointY = canvasHeight - bufferSize;
            referenceAngle = Math.abs(
                Math.toRadians(
                    GeoUtility.getDistance(referencePoint, minLonPoint)
                )
            );
            referencePointX  = bufferSize;
            referencePointX += distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility.getDistance(referencePoint, minLonPoint);
            referencePointX += ((canvasWidth - (bufferSize + bufferSize)) - (minLonToMaxLon * distanceScaleFactor)) / 2;
        }
        if(horizontalInversion) {
            referencePointX = canvasWidth - bufferSize - (referencePointX - bufferSize);
        }
    }


    /**
     * Finds the scale factor necessary to fit all race markers within the onscreen map and assigns
     * it to distanceScaleFactor Returns the max horizontal distance of the map.
     */
    private double scaleRaceExtremities() {

        double vertAngle = Math.abs(
                GeoUtility.getBearingRad(minLatPoint, maxLatPoint)
        );
        double vertDistance =
            Math.cos(vertAngle) * GeoUtility.getDistance(minLatPoint, maxLatPoint);
        double horiAngle = Math.abs(
                GeoUtility.getBearingRad(minLonPoint, maxLonPoint)
        );
        if (horiAngle <= (Math.PI / 2)) {
            horiAngle = (Math.PI / 2) - horiAngle;
        } else {
            horiAngle = horiAngle - (Math.PI / 2);
        }
        double horiDistance =
            Math.cos(horiAngle) * GeoUtility.getDistance(minLonPoint, maxLonPoint);

        double vertScale = (canvasHeight - (bufferSize + bufferSize)) / vertDistance;

        if ((horiDistance * vertScale) > (canvasWidth - (bufferSize + bufferSize))) {
            distanceScaleFactor = (canvasWidth - (bufferSize + bufferSize)) / horiDistance;
            scaleDirection = ScaleDirection.HORIZONTAL;
        } else {
            distanceScaleFactor = vertScale;
            scaleDirection = ScaleDirection.VERTICAL;
        }
        return horiDistance;
    }

    private Point2D findScaledXY(GeoPoint unscaled) {
        return findScaledXY(unscaled.getLat(), unscaled.getLng());
    }

    private Point2D findScaledXY (double unscaledLat, double unscaledLon) {
        double distanceFromReference;
        double angleFromReference;
        double xAxisLocation = referencePointX;
        double yAxisLocation = referencePointY;

        angleFromReference = GeoUtility.getBearingRad(
                minLatPoint, new GeoPoint(unscaledLat, unscaledLon)
        );
        distanceFromReference = GeoUtility.getDistance(
            minLatPoint, new GeoPoint(unscaledLat, unscaledLon)
        );
        System.out.println("distanceFromReference = " + distanceFromReference);
        if (angleFromReference >= 0 && angleFromReference <= Math.PI / 2) {
            xAxisLocation += Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else if (angleFromReference >= 0) {
            angleFromReference = angleFromReference - Math.PI / 2;
            xAxisLocation += Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            angleFromReference = Math.abs(angleFromReference);
            xAxisLocation -= Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else {
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xAxisLocation -= Math.round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += Math.round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        }
        if(horizontalInversion) {
            xAxisLocation = canvasWidth - bufferSize - (xAxisLocation - bufferSize);
        }
        System.out.println("yAxisLocation = " + yAxisLocation + " " + unscaledLat);
        System.out.println("xAxisLocation = " + xAxisLocation + " " + unscaledLon);
        return new Point2D(xAxisLocation, yAxisLocation);
    }

    /**
     * Find the number of meters per pixel.
     */
    private void findMetersPerPixel() {
        Point2D p1, p2;
        GeoPoint g1, g2;
        double theta, distance, dx, dy, dHorizontal, dVertical;
        g1 = new GeoPoint(maxLatPoint.getLat(), minLonPoint.getLng());
        g2 = new GeoPoint(minLatPoint.getLat(), maxLatPoint.getLng());
        p1 = findScaledXY(new GeoPoint(maxLatPoint.getLat(), minLonPoint.getLng()));
        p2 = findScaledXY(new GeoPoint(minLatPoint.getLat(), maxLatPoint.getLng()));
        theta = GeoUtility.getBearingRad(g1, g2);
        distance = GeoUtility.getDistance(g1, g2);
        dHorizontal = Math.abs(Math.sin(theta) * distance);
        dVertical = Math.abs(Math.cos(theta) * distance);
        dx = Math.abs(p1.getX() - p2.getX());
        dy = Math.abs(p1.getY() - p2.getY());
        metersPerPixelX = dHorizontal / dx;
        metersPerPixelY = dVertical / dy;
    }

    public void setAnnotationVisibilities (boolean teamName, boolean velocity, boolean estTime,
        boolean legTime, boolean trail, boolean wake) {
        for (BoatObject boatObject : boatObjects.values()) {
            boatObject.setVisibility(teamName, velocity, estTime, legTime, trail, wake);
        }
        for (AnnotationBox ag : annotations.values()) {
            ag.setAnnotationVisibility("name", teamName);
            ag.setAnnotationVisibility("velocity", velocity);
            ag.setAnnotationVisibility("nextMark", estTime);
            ag.setAnnotationVisibility("lastMark", legTime);
        }
    }

    public void setFPSVisibility (boolean visibility) {
        fpsDisplay.setVisible(visibility);
    }

    public ReadOnlyBooleanProperty getFPSVisibilityProperty () {
        return fpsDisplay.visibleProperty();
    }

    public void selectBoat (Yacht selectedYacht) {
        boatObjects.forEach((boat, group) ->
            group.setIsSelected(boat == selectedYacht)
        );
    }

    public void pauseRace () {
        timer.stop();
    }

    public void startRace () {
        timer.start();
    }

    public void setBoatAsPlayer (Yacht playerYacht) {
        boatObjects.get(playerYacht).setAsPlayer();
//        annotations.get(playerYacht).setAsPlayer();
    }
}
