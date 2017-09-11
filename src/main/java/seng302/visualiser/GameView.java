package seng302.visualiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.ClientYacht;
import seng302.model.GeoPoint;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.model.token.Token;
import seng302.utilities.GeoUtility;
import seng302.visualiser.fxObjects.assets_2D.AnnotationBox;
import seng302.visualiser.fxObjects.assets_2D.BoatObject;
import seng302.visualiser.fxObjects.assets_2D.CourseBoundary;
import seng302.visualiser.fxObjects.assets_2D.Gate;
import seng302.visualiser.fxObjects.assets_2D.MarkArrowFactory;
import seng302.visualiser.fxObjects.assets_2D.Marker;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
import seng302.visualiser.fxObjects.assets_3D.ModelType;
import seng302.utilities.Sounds;
import seng302.visualiser.fxObjects.AnnotationBox;
import seng302.visualiser.fxObjects.BoatObject;
import seng302.visualiser.fxObjects.CourseBoundary;
import seng302.visualiser.fxObjects.Gate;
import seng302.visualiser.fxObjects.MarkArrowFactory;
import seng302.visualiser.fxObjects.Marker;
import seng302.visualiser.map.Boundary;
import seng302.visualiser.map.CanvasMap;

/**
 * Created by cir27 on 20/07/17.
 */
public class GameView extends Pane {

    private double bufferSize = 50;
    private double panelWidth = 1280;
    private double panelHeight = 960;
    private double canvasWidth = 1100;
    private double canvasHeight = 920;
    private boolean horizontalInversion = false;

    private double distanceScaleFactor;
    private ScaleDirection scaleDirection;
    private GeoPoint minLatPoint, minLonPoint, maxLatPoint, maxLonPoint;
    private double referencePointX, referencePointY;
    private double metersPerPixelX, metersPerPixelY;

    private boolean isZoom = false;

    private Text fpsDisplay = new Text();
    private Polygon raceBorder = new CourseBoundary();

    /* Note that if either of these is null then values for it have not been added and the other
       should be used as the limits of the map. */
    private List<Limit> borderPoints;
    private Map<Mark, Marker> markerObjects;

    private Map<ClientYacht, BoatObject> boatObjects = new HashMap<>();
    private Map<ClientYacht, AnnotationBox> annotations = new HashMap<>();
    private ObservableList<Node> gameObjects;
    private BoatObject selectedBoat = null;
    private Group annotationsGroup = new Group();
    private Group wakesGroup = new Group();
    private Group boatObjectGroup = new Group();
    private Group trails = new Group();
    private Group markers = new Group();
    private Group tokens = new Group();
    private List<CompoundMark> course = new ArrayList<>();
    private List<Node> mapTokens;

    private ImageView mapImage = new ImageView();
    private Camera camera;

    //FRAME RATE

    private AnimationTimer timer;
    private int NUM_SAMPLES = 10;
    private final long[] frameTimes = new long[NUM_SAMPLES];
    private Double frameRate = 60.0;
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;
    private ClientYacht playerYacht;
    private double windDir = 0.0;

    double scaleFactor = 1;

    public void setRes(Integer x, Integer y){
        this.panelHeight = y;
        this.panelWidth = x;
    }

    private void zoomOut() {
        scaleFactor = 0.1;
        if (this.getScaleX() > 0.5) {
            this.setScaleX(this.getScaleX() - scaleFactor);
            this.setScaleY(this.getScaleY() - scaleFactor);
        }
    }

    private void zoomIn() {
        scaleFactor = 0.10;
        if (this.getScaleX() < 2.5) {
            this.setScaleX(this.getScaleX() + scaleFactor);
            this.setScaleY(this.getScaleY() + scaleFactor);
        }
    }

    private enum ScaleDirection {
        HORIZONTAL,
        VERTICAL
    }


    private void trackBoat() {
        if (selectedBoat != null) {
            double x = selectedBoat.getBoatLayoutX();
            double y = selectedBoat.getBoatLayoutY();
            double displacementX = this.getWidth();
            double displacementY = this.getHeight();
            this.setLayoutX((-x + (displacementX / 2.0)) * this.getScaleX());
            this.setLayoutY((-y + (displacementY / 2.0)) * this.getScaleY());
        } else {
            this.setLayoutX(0);
            this.setLayoutY(0);
        }
    }

    public GameView () {
        gameObjects = this.getChildren();
//        AmbientLight ambientLight = new AmbientLight(new Color(1,1,1,0.4));
//        ambientLight.setOpacity(0.5);
//        gameObjects.add(ambientLight);
        // create image view for map, bind panel size to image
        camera = new ParallelCamera();
        camera.setTranslateZ(-500);
        camera.setFarClip(Double.MAX_VALUE);
        camera.setNearClip(0.1);
        PointLight pl = new PointLight();
        pl.setLightOn(true);
        pl.layoutYProperty().bind(camera.layoutYProperty());
        pl.layoutXProperty().bind(camera.layoutXProperty());
//        gameObjects.add(camera);
        this.sceneProperty().addListener((obs, oldValue, scene) -> {
            if (scene != null) {
                scene.setCamera(camera);
            }
        });
        initializeTimer();
        gameObjects.addAll(mapImage, raceBorder, markers, tokens, pl);
        // TODO: 11/09/17 ajm412: do you even zoom bro?
//        this.sceneProperty().addListener(((observable, oldValue, scene) -> {
//            if (scene != null) {
//                setupZoom();
//            } else {
//                disableZoom();
//            }
//        }));
//
//        this.widthProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
//                Number newValue) {
//                scaleFactor = getWidth() / panelWidth;
//
//                if (panelHeight * scaleFactor < getHeight()) {
//                    Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
//                    getTransforms().remove(0, getTransforms().size());
//                    getTransforms().add(scale);
//
//                    setPrefWidth(getWidth() / scaleFactor);
//                    setPrefHeight(getHeight() / scaleFactor);
//                }
//            }
//        });
//
//        this.heightProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
//                Number newValue) {
//                scaleFactor = getHeight() / panelHeight;
//
//                if (panelWidth * scaleFactor < getWidth()) {
//                    Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
//                    getTransforms().remove(0, getTransforms().size());
//                    getTransforms().add(scale);
//
//                    setPrefWidth(getWidth() / scaleFactor);
//                    setPrefHeight(getHeight() / scaleFactor);
//                }
//            }
//        });
    }

    private void initializeTimer() {
        Arrays.fill(frameTimes, 1_000_000_000 / 60);
        timer = new AnimationTimer() {
            private long lastTime = 0;
            private int FPSCount = 30;
            private Double frameRate = 60.0;
            private int index = 0;
            private boolean arrayFilled = false;
            private long sum = 1_000_000_000 / 3;

            @Override
            public void handle(long now) {
                trackBoat();
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
                                drawFps(frameRate);
                            }
                        }
                        lastTime = now;
                    }
                }
//                boatObjects.forEach((boat, boatObject) -> boatObject.updateLocation());
            }
        };
    }

    /**
     * First find the top right and bottom left points' geo locations, then retrieve map from google
     * to display on image view.  - Haoming 22/5/2017
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
        mapImage.fitWidthProperty().bind(((AnchorPane) this.getParent()).heightProperty());
        mapImage.fitHeightProperty().bind(((AnchorPane) this.getParent()).heightProperty());
    }

    // TODO: 16/08/17 Break up this function
    /**
     * Adds a course to the GameView. The view is scaled accordingly unless a border is set in which
     * case the course is added relative ot the border.
     *
     * @param newCourse the mark objects that make up the course.
     * @param sequence The sequence the marks travel through
     */
    public void updateCourse(List<CompoundMark> newCourse, List<Corner> sequence) {
        markerObjects = new HashMap<>();

        for (Corner corner : sequence) { //Makes course out of all compound marks.
            for (CompoundMark compoundMark : newCourse) {
                if (corner.getCompoundMarkID() == compoundMark.getId()) {
                    course.add(compoundMark);
                }
            }
        }

        // TODO: 16/08/17 Updating mark roundings here. It should not happen here. Nor should it be done this way.
        for (Corner corner : sequence){
            CompoundMark compoundMark = course.get(corner.getSeqID() - 1);
            compoundMark.setRoundingSide(
                RoundingSide.getRoundingSide(corner.getRounding())
            );
        }

        final List<Gate> gates = new ArrayList<>();
        Paint colour = Color.BLACK;
        //Creates new markers
        for (CompoundMark cMark : newCourse) {
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
                    gates.add(
                        makeAndBindGate(
                            markerObjects.get(cMark.getSubMark(i)),
                            markerObjects.get(cMark.getSubMark(i + 1)),
                            colour
                        )
                    );
                }
            }
            colour = Color.BLACK;
        }

        createMarkArrows(sequence);

        //Scale race to markers if there is no border.
        if (borderPoints == null) {
            rescaleRace(new ArrayList<>(markerObjects.keySet()));
        }
        //Move the Markers to initial position.
        markerObjects.forEach(((mark, marker) -> {
            Point2D p2d = findScaledXY(mark.getLat(), mark.getLng());
            marker.setLayoutX(p2d.getX());
            marker.setLayoutY(p2d.getY());
        }));
        Platform.runLater(() -> {
            markers.getChildren().clear();
            markers.getChildren().addAll(gates);
            markers.getChildren().addAll(markerObjects.values());
        });
    }

    /**
     * Calculates all the data needed for to create mark arrows. Requires that a course has been
     * added to the gameview.
     * @param sequence The order in which marks are traversed.
     */
    private void createMarkArrows (List<Corner> sequence) {
        for (int i=1; i < sequence.size()-1; i++) { //General case.
            double averageLat = 0;
            double averageLng = 0;
            int numMarks = course.get(i-1).getMarks().size();
            for (Mark mark : course.get(i-1).getMarks()) {
                averageLat += mark.getLat();
                averageLng += mark.getLng();
            }
            GeoPoint lastMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
            numMarks = course.get(i+1).getMarks().size();
            averageLat = 0;
            averageLng = 0;
            for (Mark mark : course.get(i+1).getMarks()) {
                averageLat += mark.getLat();
                averageLng += mark.getLng();
            }
            GeoPoint nextMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
            // TODO: 16/08/17 This comparison doesn't need to exist but the alternative is to user server enum client side.
            for (Mark mark : course.get(i).getMarks()) {
                markerObjects.get(mark).addArrows(
                    mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                    GeoUtility.getBearing(lastMarkAv, mark),
                    GeoUtility.getBearing(mark, nextMarkAv)
                );
            }
        }
        createStartLineArrows();
        createFinishLineArrows();
    }

    private void createStartLineArrows () {
        double averageLat = 0;
        double averageLng = 0;
        int numMarks = 0;
        for (Mark mark : course.get(1).getMarks()) {
            numMarks += 1;
            averageLat += mark.getLat();
            averageLng += mark.getLng();
        }
        GeoPoint firstMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
        for (Mark mark : course.get(0).getMarks()) {
            markerObjects.get(mark).addArrows(
                mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                0d, //90
                GeoUtility.getBearing(mark, firstMarkAv)
            );
        }
    }

    private void createFinishLineArrows () {
        double numMarks = 0;
        double averageLat = 0;
        double averageLng = 0;
        for (Mark mark : course.get(course.size()-2).getMarks()) {
            numMarks += 1;
            averageLat += mark.getLat();
            averageLng += mark.getLng();
        }
        GeoPoint secondToLastMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
        for (Mark mark : course.get(course.size()-1).getMarks()) {
            markerObjects.get(mark).addArrows(
                mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                GeoUtility.getBearing(secondToLastMarkAv, mark),
                GeoUtility.getBearing(mark, mark)
            );
        }
    }

    /**
     * Creates a new Marker and binds it's position to the given Mark.
     *
     * @param observableMark The mark to bind the marker to.
     * @param colour The desired colour of the mark
     */
    private void makeAndBindMarker(Mark observableMark, Paint colour) {
        Marker marker = new Marker(colour);
//        marker.addArrows(MarkArrowFactory.RoundingSide.PORT, ThreadLocalRandom.current().nextDouble(91, 180), ThreadLocalRandom.current().nextDouble(1, 90));
        markerObjects.put(observableMark, marker);
        observableMark.addPositionListener((mark, lat, lon) -> {
            Point2D p2d = findScaledXY(lat, lon);
            markerObjects.get(mark).setLayoutX(p2d.getX());
            markerObjects.get(mark).setLayoutY(p2d.getY());
        });
    }

    /**
     * Creates a new gate connecting the given marks.
     *
     * @param m1 The first Mark of the gate.
     * @param m2 The second Mark of the gate.
     * @param colour The desired colour of the gate.
     * @return the new gate.
     */
    private Gate makeAndBindGate(Marker m1, Marker m2, Paint colour) {
        Gate gate = new Gate(colour);
        gate.startXProperty().bind(
            m1.layoutXProperty()
        );
        gate.startYProperty().bind(
            m1.layoutYProperty()
        );
        gate.endXProperty().bind(
            m2.layoutXProperty()
        );
        gate.endYProperty().bind(
            m2.layoutYProperty()
        );
        return gate;
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

//    /**
//     * Rescales the race to the size of the window.
//     *
//     * @param limitingCoordinates the set of geo points that contains the extremities of the race.
//     */
//    private void rescaleRace(List<GeoPoint> limitingCoordinates) {
//        //Check is called once to avoid unnecessarily change the course limits once the race is running
//        findMinMaxPoint(limitingCoordinates);
//        double minLonToMaxLon = scaleRaceExtremities();
//        calculateReferencePointLocation(minLonToMaxLon);
////        drawGoogleMap();
//    }

    /**
     * Replaces all tokens in the course with those passed in
     *
     * @param newTokens the tokens to be put on the course.
     */
    public void updateTokens(List<Token> newTokens) {
        mapTokens = new ArrayList<>();
        for (Token token : newTokens) {
            Point2D location = findScaledXY(token.getLat(), token.getLng());
            Node tokenObject = ModelFactory.importModel(ModelType.VELOCITY_PICKUP).getAssets();
            tokenObject.setLayoutX(location.getX());
            tokenObject.setLayoutY(location.getY());
            mapTokens.add(tokenObject);
        }
        Platform.runLater(() -> {
            tokens.getChildren().clear();
            tokens.getChildren().addAll(mapTokens);
        });
    }

//    // TODO: 16/08/17 initialize zooming internal to GameView only
//    /**
//     * Enables zoom. Has to be called after this is added to a scene.
//     */
//    private void setupZoom() {
//        this.getScene().addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
//            if (event.getCode() == KeyCode.Z) {
//                zoomIn();
//            } else if (event.getCode() == KeyCode.X) {
//                zoomOut();
//            }
//        });
//        enableZoom();
//    }
////
//    public void enableZoom() {
//        isZoom = true;
//    }
//
//    public void disableZoom() {
//        isZoom = false;
//    }

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
//        drawGoogleMap();
    }

    private void setSelectedBoat(BoatObject bo, Boolean isSelected) {
        if (this.selectedBoat == bo && !isSelected) {
            this.selectedBoat = null;
            boatObjects.forEach((boat, group) ->
                group.setIsSelected(false)
            );
        } else if (isSelected) {
            this.selectedBoat = bo;
            for (BoatObject group : boatObjects.values()) {
                if (group != bo) {
                    group.setIsSelected(false);
                }
            }
        }
    }

    /**
     * Draws all the boats.
     * @param  yachts The yachts to set in the race
     */
    public void setBoats(List<ClientYacht> yachts) {
        BoatObject newBoat;
        final List<Group> wakes = new ArrayList<>();
        for (ClientYacht clientYacht : yachts) {
            Color colour = clientYacht.getColour();
            newBoat = new BoatObject();
            newBoat.addSelectedBoatListener(this::setSelectedBoat);
            newBoat.setFill(colour);
            boatObjects.put(clientYacht, newBoat);
            createAndBindAnnotationBox(clientYacht, colour);
//            wakesGroup.getChildren().add(newBoat.getWake());
            wakes.add(newBoat.getWake());
            boatObjectGroup.getChildren().add(newBoat);
            trails.getChildren().add(newBoat.getTrail());

            clientYacht.addLocationListener((boat, lat, lon, heading, sailIn, velocity) -> {
                BoatObject bo = boatObjects.get(boat);
                Point2D p2d = findScaledXY(lat, lon);
                bo.moveTo(p2d.getX(), p2d.getY(), heading, velocity, sailIn, windDir);
                annotations.get(boat).setLocation(p2d.getX(), p2d.getY());
                bo.setTrajectory(
                    heading,
                    velocity,
                    metersPerPixelX,
                    metersPerPixelY);
            });
        }
        annotationsGroup.getChildren().addAll(annotations.values());
        Platform.runLater(() -> {
            gameObjects.addAll(trails);
            gameObjects.addAll(wakes);
            gameObjects.addAll(annotationsGroup);
            gameObjects.addAll(boatObjectGroup);
        });
    }

    private void createAndBindAnnotationBox(ClientYacht clientYacht, Paint colour) {
        AnnotationBox newAnnotation = new AnnotationBox();
        newAnnotation.setFill(colour);
        newAnnotation.addAnnotation(
            "name", "Player: " + clientYacht.getShortName()
        );
//        newAnnotation.addAnnotation(
//            "velocity",
//            yacht.getVelocityProperty(),
//            (velocity) -> String.format("Speed: %.2f ms", velocity.doubleValue())
//        );
//        newAnnotation.addAnnotation(
//            "nextMark",
//            yacht.timeTillNextProperty(),
//            (time) -> {
//                DateFormat format = new SimpleDateFormat("mm:ss");
//                return format.format(time);
//            }
//        );
//        newAnnotation.addAnnotation(
//            "lastMark",
//            yacht.timeTillNextProperty(),
//            (time) -> {
//                DateFormat format = new SimpleDateFormat("mm:ss");
//                return format.format(time);
//            }
//        );
        annotations.put(clientYacht, newAnnotation);
    }

    private void drawFps(Double fps) {
        //Platform.runLater(() -> fpsDisplay.setText(String.format("%d FPS", Math.round(fps))));
    }

    /**
     * Sets the class variables minLatPoint, maxLatPoint, minLonPoint, maxLonPoint to the point with
     * the leftmost point, rightmost point, southern most point and northern most point
     * respectively.
     */
    private void findMinMaxPoint(List<GeoPoint> points) {
        List<GeoPoint> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort(Comparator.comparingDouble(GeoPoint::getLat));
        minLatPoint = new GeoPoint(sortedPoints.get(0).getLat(), sortedPoints.get(0).getLng());
        GeoPoint maxLat = sortedPoints.get(sortedPoints.size() - 1);
        maxLatPoint = new GeoPoint(maxLat.getLat(), maxLat.getLng());

        sortedPoints.sort(Comparator.comparingDouble(GeoPoint::getLng));
        minLonPoint = new GeoPoint(sortedPoints.get(0).getLat(), sortedPoints.get(0).getLng());
        GeoPoint maxLon = sortedPoints.get(sortedPoints.size() - 1);
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
            referencePointX =
                bufferSize + distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility
                    .getDistance(referencePoint, minLonPoint);
            referenceAngle = Math.abs(GeoUtility.getDistance(referencePoint, maxLatPoint));
            referencePointY = canvasHeight - (bufferSize + bufferSize);
            referencePointY -= distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility
                .getDistance(referencePoint, maxLatPoint);
            referencePointY = referencePointY / 2;
            referencePointY += bufferSize;
            referencePointY += distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility
                .getDistance(referencePoint, maxLatPoint);
        } else {
            referencePointY = canvasHeight - bufferSize;
            referenceAngle = Math.abs(
                Math.toRadians(
                    GeoUtility.getDistance(referencePoint, minLonPoint)
                )
            );
            referencePointX = bufferSize;
            referencePointX += distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility
                .getDistance(referencePoint, minLonPoint);
            referencePointX +=
                ((canvasWidth - (bufferSize + bufferSize)) - (minLonToMaxLon * distanceScaleFactor))
                    / 2;
        }
        if (horizontalInversion) {
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

    private Point2D findScaledXY(double unscaledLat, double unscaledLon) {
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
        if (angleFromReference >= 0 && angleFromReference <= Math.PI / 2) {
            xAxisLocation += Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else if (angleFromReference >= 0) {
            angleFromReference = angleFromReference - Math.PI / 2;
            xAxisLocation += Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            angleFromReference = Math.abs(angleFromReference);
            xAxisLocation -= Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else {
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xAxisLocation -= Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        }
        if (horizontalInversion) {
            xAxisLocation = canvasWidth - bufferSize - (xAxisLocation - bufferSize);
        }
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

    public void setAnnotationVisibilities(boolean teamName, boolean velocity, boolean estTime,
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

    public void setFPSVisibility(boolean visibility) {
        fpsDisplay.setVisible(visibility);
    }

    public void selectBoat(ClientYacht selectedClientYacht) {
        boatObjects.forEach((boat, group) ->
            group.setIsSelected(boat == selectedClientYacht)
        );
    }

    public void pauseRace() {
        timer.stop();
    }

    public void setWindDir(double windDir) {
        this.windDir = windDir;
    }

    public void startRace() {
        timer.start();
    }

    public ClientYacht getPlayerYacht() {
        return playerYacht;
    }

    public void setBoatAsPlayer (ClientYacht playerYacht) {
        this.playerYacht = playerYacht;
        playerYacht.toggleSail();
        boatObjects.get(playerYacht).setAsPlayer();
        CompoundMark currentMark = course.get(playerYacht.getLegNumber());
        for (Mark mark : currentMark.getMarks()) {
            markerObjects.get(mark).showNextExitArrow();
        }
        annotations.get(playerYacht).addAnnotation(
            "velocity",
            playerYacht.getVelocityProperty(),
            (velocity) -> String.format("Speed: %.2f ms", velocity.doubleValue())
        );
        Platform.runLater(() -> {
            boatObjectGroup.getChildren().remove(boatObjects.get(playerYacht));
            gameObjects.add(boatObjects.get(playerYacht));
            annotationsGroup.getChildren().remove(annotations.get(playerYacht));
            gameObjects.add(annotations.get(playerYacht));
        });
        playerYacht.addMarkRoundingListener(this::updateMarkArrows);
    }

    private void updateMarkArrows (ClientYacht yacht, int legNumber) {
        //Only show arrows for this and next leg.
        CompoundMark nextMark = null;
        if (legNumber < course.size() - 1) {
            Sounds.playMarkRoundingSound();
            nextMark = course.get(legNumber);
            for (Mark mark : nextMark.getMarks()) {
                markerObjects.get(mark).showNextEnterArrow();
            }
        }
        if (legNumber - 2 >= 0) {
            CompoundMark lastMark = course.get(Math.max(0, legNumber - 2));
            if (lastMark != nextMark) {
                for (Mark mark : lastMark.getMarks()) {
                    markerObjects.get(mark).hideAllArrows();
                }
            }
        }
        if (legNumber - 1 >= 0) {
            CompoundMark thisMark = course.get(Math.max(0, legNumber - 1));
            if (thisMark != nextMark) {
                for (Mark mark : thisMark.getMarks()) {
                    markerObjects.get(mark).showNextExitArrow();
                }
            }
        }
    }

    /**
     * Given yacht geopoint by race view controller, drawCollision will calculate canvas X and Y and
     * display a flashing red circle on collision point.
     *
     * @param collisionPoint yacht collision point
     */
    public void drawCollision(GeoPoint collisionPoint) {
        Point2D point = findScaledXY(collisionPoint);
        double circleRadius = 0.0;
        Circle circle = new Circle(point.getX(), point.getY(), circleRadius, Color.RED);

        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.RED);
        circle.setStrokeWidth(3);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);

        KeyFrame keyframe1 = new KeyFrame(Duration.ZERO,
            new KeyValue(circle.radiusProperty(), 0),
            new KeyValue(circle.strokeProperty(), Color.TRANSPARENT));
        KeyFrame keyFrame2 = new KeyFrame(new Duration(1000),
            new KeyValue(circle.radiusProperty(), 50),
            new KeyValue(circle.strokeProperty(), Color.RED));
        KeyFrame keyFrame3 = new KeyFrame(new Duration(1500),
            new KeyValue(circle.strokeProperty(), Color.TRANSPARENT));

        timeline.getKeyFrames().addAll(keyframe1, keyFrame2, keyFrame3);

        Platform.runLater(() -> gameObjects.add(circle));
        timeline.setOnFinished(event -> Platform.runLater(() -> gameObjects.remove(circle)));
        timeline.play();
    }

    public void setFrameRateFXText(Text fpsDisplay) {
        this.fpsDisplay = null;
        this.fpsDisplay = fpsDisplay;
    }
}
