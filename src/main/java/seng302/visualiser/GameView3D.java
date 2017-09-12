package seng302.visualiser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.ClientYacht;
import seng302.model.GeoPoint;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.model.token.Token;
import seng302.utilities.GeoUtility;
import seng302.visualiser.fxObjects.assets_2D.BoatObject;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
import seng302.visualiser.fxObjects.assets_3D.ModelType;

/**
 * Collection of animated3D assets that displays a race.
 */

public class GameView3D {

    private final double FOV = 60;
    private final double DEFAULT_CAMERA_DEPTH = 100;

    private Group root3D;
    private SubScene view;
//    ParallelCamera camera;
    private PerspectiveCamera camera;
    private Group gameObjects;

    private double bufferSize = 0;
    private double canvasWidth = 200;
    private double canvasHeight = 200;
    private boolean horizontalInversion = false;




    private double distanceScaleFactor;
    private ScaleDirection scaleDirection;
    private GeoPoint minLatPoint, minLonPoint, maxLatPoint, maxLonPoint;
    private double referencePointX, referencePointY;
    private double metersPerPixelX, metersPerPixelY;

    final double SCALE_DELTA = 1.1;

    private Text fpsDisplay = new Text();
    private Group raceBorder = new Group();

    /* Note that if either of these is null then values for it have not been added and the other
       should be used as the limits of the map. */
    private List<Limit> borderPoints;
    private Map<Mark, Group> markerObjects;

    private Map<ClientYacht, BoatObject> boatObjects = new HashMap<>();
    private BoatObject selectedBoat = null;
    private Group wakesGroup = new Group();
    private Group boatObjectGroup = new Group();
    private Group markers = new Group();
    private Group tokens = new Group();
    private Group playerAnnotation = new Group();
    private List<CompoundMark> course = new ArrayList<>();
    private List<Node> mapTokens;
    private Timer playerBoatAnimationTimer = new Timer();
    private Group trail = new Group();
    private ImageView mapImage = new ImageView();

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

    private enum ScaleDirection {
        HORIZONTAL,
        VERTICAL
    }




    public GameView3D () {
        camera = new PerspectiveCamera(true);
//        camera = new ParallelCamera();
//        gameObjects.getTransforms().add(new Scale(4,4,4));
//        camera.setLayoutX(camera.getLayoutX()-400);
//        camera.setLayoutY(camera.getLayoutY()-400);
        camera.getTransforms().addAll(
            new Translate(0,0, -DEFAULT_CAMERA_DEPTH)
        );
        camera.setFarClip(Double.MAX_VALUE);
        camera.setNearClip(0.1);
        camera.setFieldOfView(FOV);
        gameObjects = new Group();
        PointLight pl = new PointLight(Color.DARKGRAY);
        pl.setLightOn(true);
        pl.setBlendMode(BlendMode.ADD);
        pl.setOpacity(0.5);
        pl.getTransforms().add(new Translate(0,0,-500));
        AmbientLight al = new AmbientLight();
        al.setLightOn(true);
        al.setBlendMode(BlendMode.SOFT_LIGHT);
        al.getTransforms().add(new Translate(0, 0, -100));
        root3D = new Group(camera, gameObjects);
        view = new SubScene(
            root3D, 1000, 1000, true, SceneAntialiasing.BALANCED
        );
        view.setCamera(camera);
//        view.setFill(Color.LIGHTBLUE);
        camera.getTransforms().add(new Rotate(30, new Point3D(1,0,0)));
//        gameObjects.getChildren().addAll(raceBorder, markers, tokens);
        System.out.println(camera.getLayoutX());
        System.out.println(camera.getTranslateX());
        System.out.println(camera.getLayoutY());
        System.out.println(camera.getTranslateY());
        System.out.println(camera.getTranslateZ());
        camera.setTranslateZ(-80);
        camera.setTranslateY(150);
        Sphere red = new Sphere(1);
        red.setMaterial(new PhongMaterial(Color.RED));
        red.setLayoutX(0);
        red.setLayoutY(0);

        Sphere blue = new Sphere(1);
        blue.setMaterial(new PhongMaterial(Color.BLUE));
        blue.setLayoutX(1);
        blue.setLayoutY(0);

        Sphere green = new Sphere(1);
        green.setMaterial(new PhongMaterial(Color.GREEN));
        green.setLayoutX(-.5);
        green.setLayoutY(0);

        Sphere white = new Sphere(1);
        white.setMaterial(new PhongMaterial(Color.WHITE));
        white.setLayoutX(-.25);
        white.setLayoutY(0);

        Sphere black = new Sphere(1);
        black.setMaterial(new PhongMaterial(Color.BLACK));
        black.setLayoutX(-.125);
        black.setLayoutY(0);

//        ImagePattern oceanImage = new ImagePattern(
//            new Image(
//                GameView3D.class.getResourceAsStream(
//                    "/pics/water.gif")
//            ), 0, 0, 1000, 1000, false
//        );
//
//        Circle ocean = new Circle(0, 0, 5000);
//        ocean.setFill(oceanImage);
//        ocean.getTransforms().add(new Scale(0.1, 0.1));

        gameObjects.getChildren().addAll(
//            ocean,
//            ModelFactory.importModel(ModelType.OCEAN).getAssets(),
            raceBorder, trail, markers, tokens, playerAnnotation,
            white, blue, green, black, red
        );

        System.out.println(camera.getLayoutX());
        System.out.println(camera.getTranslateX());
        System.out.println(camera.getLayoutY());
        System.out.println(camera.getTranslateY());
        System.out.println(camera.getTranslateZ());
//        Sphere s = new Sphere(1);
//        s.setMaterial(new PhongMaterial(Color.RED));
//        Sphere left = new Sphere(1);
//        left.setMaterial(new PhongMaterial(Color.LEMONCHIFFON));
//        left.getTransforms().add(new Translate(-Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0, 0));
//        Sphere right = new Sphere(1);
//        right.setMaterial(new PhongMaterial(Color.ROSYBROWN));
//        right.getTransforms().add(new Translate(Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0, 0));
//        Sphere top = new Sphere(1);
//        top.setMaterial(new PhongMaterial(Color.TEAL));
//        top.getTransforms().add(new Translate(0,-Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0));
//        Sphere bottom = new Sphere(1);
//        bottom.setMaterial(new PhongMaterial(Color.BLANCHEDALMOND));
//        bottom.getTransforms().add(new Translate(0, Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0));
//
//        Node boat = ModelFactory.boatGameView(BoatMeshType.DINGHY, Color.BLUE).getAssets();
//        Node boat2 = ModelFactory.boatGameView(BoatMeshType.DINGHY, Color.BROWN).getAssets();
//        boat2.getTransforms().add(new Translate(0,20, 0));
//        Node boat3 = ModelFactory.boatGameView(BoatMeshType.DINGHY, Color.RED).getAssets();
//        boat3.getTransforms().add(new Translate(0,-20, 0));
//
//        Node sMarker = ModelFactory.importModel(ModelType.START_MARKER).getAssets();
//        sMarker.getTransforms().add(0, new Translate(30, 30, 0));
//
//        Node fMarker = ModelFactory.importModel(ModelType.FINISH_MARKER).getAssets();
//        fMarker.getTransforms().add(0, new Translate(30, -30, 0));
//
//        Node marker = ModelFactory.importModel(ModelType.PLAIN_MARKER).getAssets();
//        marker.getTransforms().add(0, new Translate(30, 0, 0));
//
//        Node coin = ModelFactory.importModel(ModelType.VELOCITY_PICKUP).getAssets();
//        coin.setTranslateX(coin.getTranslateX() - 30);
//
//        gameObjects.getChildren().addAll(
//            ModelFactory.importModel(ModelType.OCEAN).getAssets(),
//            s, left, right, top, bottom,
//            boat, boat2, boat3,
//            sMarker, fMarker, marker,
//            coin
//        );

        view.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.addEventHandler(KeyEvent.KEY_PRESSED, this::cameraMovement);
            }
        });
    }

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

        final List<Group> gates = new ArrayList<>();

        //Creates new markers
        for (CompoundMark cMark : newCourse) {
            for (Mark mark : cMark.getMarks()) {
                if (cMark.getId() == sequence.get(0).getCompoundMarkID()) {
                    makeAndBindMarker(mark, ModelType.START_MARKER);
                } else if (cMark.getId() == sequence.get(sequence.size() - 1).getCompoundMarkID()) {
                    makeAndBindMarker(mark, ModelType.FINISH_MARKER);
                } else {
                    makeAndBindMarker(mark, ModelType.PLAIN_MARKER);
                }
            }
            //Create gate line
            if (cMark.isGate()) {
                ModelType gateType;
                if (cMark.getId() == sequence.get(0).getCompoundMarkID()) {
                    gateType = ModelType.START_LINE;
                } else if (cMark.getId() == sequence.get(sequence.size() - 1).getCompoundMarkID()) {
                    gateType = ModelType.FINISH_LINE;
                } else {
                    gateType = ModelType.GATE_LINE;
                }
                gates.add(makeGate(
                    cMark.getSubMark(1), cMark.getSubMark(2), gateType
                ));
            }
        }

        //Scale race to markers if there is no border.
        if (borderPoints == null) {
            rescaleRace(new ArrayList<>(markerObjects.keySet()));
        }
        //Move the Markers to initial position.
        markerObjects.forEach(((mark, marker) -> {
            Point2D p2d = findScaledXY(mark.getLat(), mark.getLng());
            System.out.println(mark.toString() + "  " + p2d.toString());
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
     * Creates a new Marker and binds it's position to the given Mark.
     *
     * @param observableMark The mark to bind the marker to.
     * @param markerType the type of marker as a ModelType. Should be PLAIN_MARKER, START_MARKER or END_MARKER
     */
    private void makeAndBindMarker(Mark observableMark, ModelType markerType) {

        Group marker = ModelFactory.importModel(markerType).getAssets();

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
     * @param gateType The type of model for the gate.
     * @return the new gate.
     */
    private Group makeGate(Mark m1, Mark m2, ModelType gateType) {
        Point2D m1Location = findScaledXY(m1);
        Point2D m2Location = findScaledXY(m2);

        Group barrier = ModelFactory.importModel(gateType).getAssets();
        barrier.getTransforms().addAll(
            new Rotate(
                Math.toDegrees(
                    Math.atan2(m2Location.getY() - m1Location.getY(), m2Location.getX() - m1Location.getX())
                ) + 90,
                new Point3D(0,0,1)
            ),
            new Scale(1, m1Location.distance(m2Location) / 10, 1)
        );

        Point2D midPoint = m2Location.midpoint(m1Location);
        barrier.setLayoutX(midPoint.getX());
        barrier.setLayoutY(midPoint.getY());
        return barrier;
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
                -100 + distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility
                    .getDistance(referencePoint, minLonPoint);
            referenceAngle = Math.abs(GeoUtility.getDistance(referencePoint, maxLatPoint));
            referencePointY = -100 + canvasHeight - (bufferSize + bufferSize);
            referencePointY -= distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility
                .getDistance(referencePoint, maxLatPoint);
            referencePointY = referencePointY / 2;
            referencePointY += bufferSize;
            referencePointY += distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility
                .getDistance(referencePoint, maxLatPoint);
        } else {
            referencePointY = -100 + canvasHeight - bufferSize;
            referenceAngle = Math.abs(
                Math.toRadians(
                    GeoUtility.getDistance(referencePoint, minLonPoint)
                )
            );
            referencePointX = -100 + bufferSize;
            referencePointX += distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility
                .getDistance(referencePoint, minLonPoint);
            referencePointX +=
                ((canvasWidth - (bufferSize + bufferSize)) - (minLonToMaxLon * distanceScaleFactor))
                    / 2;
        }
        if (horizontalInversion) {
            referencePointX = -100 + canvasWidth - bufferSize - (referencePointX - bufferSize);
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
            xAxisLocation += distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference;
            yAxisLocation -= distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference;
        } else if (angleFromReference >= 0) {
            angleFromReference = angleFromReference - Math.PI / 2;
            xAxisLocation += distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference;
            yAxisLocation += distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference;
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            angleFromReference = Math.abs(angleFromReference);
            xAxisLocation -= distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference;
            yAxisLocation -= distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference;
        } else {
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xAxisLocation -= distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference;
            yAxisLocation += distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference;
        }
        if (horizontalInversion) {
            xAxisLocation = canvasWidth - bufferSize - (xAxisLocation - bufferSize);
        }
        return new Point2D(xAxisLocation, yAxisLocation);
    }

    public void cameraMovement(KeyEvent event) {
        switch (event.getCode()) {
            case NUMPAD8:
                camera.getTransforms().addAll(new Rotate(0.5, new Point3D(1,0,0)));
                break;
            case NUMPAD2:
                camera.getTransforms().addAll(new Rotate(-0.5, new Point3D(1,0,0)));
                break;
            case NUMPAD4:
                camera.getTransforms().addAll(new Rotate(-0.5, new Point3D(0,1,0)));
                break;
            case NUMPAD6:
                camera.getTransforms().addAll(new Rotate(0.5, new Point3D(0,1,0)));
                break;
            case X:
                camera.getTransforms().addAll(new Translate(0, 0, 1.5));
                break;
            case Z:
                camera.getTransforms().addAll(new Translate(0, 0, -1.5));
                break;
            case W:
                camera.getTransforms().addAll(new Translate(0, -1, 0));
                break;
            case S:
                camera.getTransforms().addAll(new Translate(0, 1, 0));
                break;
            case A:
                camera.getTransforms().addAll(new Translate(-1, 0, 0));
                break;
            case D:
                camera.getTransforms().addAll(new Translate(1, 0, 0));
                break;
        }
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
//        drawGoogleMap();
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
//            newBoat.addSelectedBoatListener(this::setSelectedBoat);
            newBoat.setFill(colour);
            boatObjects.put(clientYacht, newBoat);
//            createAndBindAnnotationBox(clientYacht, colour);
            wakesGroup.getChildren().add(newBoat.getWake());
            wakes.add(newBoat.getWake());
            boatObjectGroup.getChildren().add(newBoat);
//            trails.getChildren().add(newBoat.getTrail());

            clientYacht.addLocationListener((boat, lat, lon, heading, sailIn, velocity) -> {
                BoatObject bo = boatObjects.get(boat);
                Point2D p2d = findScaledXY(lat, lon);
                bo.moveTo(p2d.getX(), p2d.getY(), heading, velocity, sailIn, windDir);
//                annotations.get(boat).setLocation(p2d.getX(), p2d.getY());
                bo.setTrajectory(
                    heading,
                    velocity,
                    metersPerPixelX,
                    metersPerPixelY);
            });
        }
//        annotationsGroup.getChildren().addAll(annotations.values());
        Platform.runLater(() -> {

//            gameObjects.addAll(trails);
            gameObjects.getChildren().addAll(wakes);
            gameObjects.getChildren().addAll(boatObjectGroup);
//            gameObjects.addAll(annotationsGroup);
//            gameObjects.addAll(boatObjectGroup);
        });
    }

    public Node getAssets () {
        return view;
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
        List<Node> boundaryAssets = new ArrayList<>();

        Point2D lastLocation = findScaledXY(border.get(0).getLat(), border.get(0).getLng());
        Group pylon = ModelFactory.importModel(ModelType.BORDER_PYLON).getAssets();
        pylon.setLayoutX(lastLocation.getX());
        pylon.setLayoutY(lastLocation.getY());
        boundaryAssets.add(pylon);

        for (int i=1; i<border.size(); i++) {
            Point2D location = findScaledXY(border.get(i).getLat(), border.get(i).getLng());
            pylon = ModelFactory.importModel(ModelType.BORDER_PYLON).getAssets();
            pylon.setLayoutX(location.getX());
            pylon.setLayoutY(location.getY());

            Group barrier = ModelFactory.importModel(ModelType.BORDER_BARRIER).getAssets();
            barrier.getTransforms().addAll(
                new Rotate(
                    Math.toDegrees(
                        Math.atan2(location.getY() - lastLocation.getY(), location.getX() - lastLocation.getX())
                    ),
                    new Point3D(0,0,1)
                ),
                new Scale((lastLocation.distance(location) / 15)-0.2, 1, 1)
            );

            Point2D midPoint = location.midpoint(lastLocation);
            barrier.setLayoutX(midPoint.getX());
            barrier.setLayoutY(midPoint.getY());

            lastLocation = location;

            boundaryAssets.add(barrier);
            boundaryAssets.add(pylon);
        }

        Point2D firstLocation = findScaledXY(border.get(0).getLat(), border.get(0).getLng());
        Group barrier = ModelFactory.importModel(ModelType.BORDER_BARRIER).getAssets();
        barrier.getTransforms().addAll(
            new Rotate(
                Math.toDegrees(
                    Math.atan2(lastLocation.getY() - firstLocation.getY(), lastLocation.getX() - firstLocation.getX())
                ),
                new Point3D(0,0,1)
            ),
            new Scale((firstLocation.distance(lastLocation) / 15)-0.2, 1, 1)
        );

        Point2D midPoint = lastLocation.midpoint(firstLocation);
        barrier.setLayoutX(midPoint.getX());
        barrier.setLayoutY(midPoint.getY());
        boundaryAssets.add(barrier);

        Platform.runLater(() -> raceBorder.getChildren().setAll(boundaryAssets));
    }

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
            tokens.getChildren().setAll(mapTokens);
        });
    }

    public void setBoatAsPlayer (ClientYacht playerYacht) {
        this.playerYacht = playerYacht;

        Platform.runLater(() ->
            playerAnnotation.getChildren().setAll(ModelFactory.importModel(ModelType.PLAYER_IDENTIFIER).getAssets())
        );
        BoatObject playerAssets = boatObjects.get(playerYacht);
        playerAnnotation.layoutXProperty().bind(playerAssets.layoutXProperty());
        playerAnnotation.layoutYProperty().bind(playerAssets.layoutYProperty());

        playerBoatAnimationTimer.scheduleAtFixedRate(new TimerTask() {

            private Point2D lastLocation = findScaledXY(playerYacht.getLocation());

            @Override
            public void run() {
                Node segment = ModelFactory.importModel(ModelType.TRAIL_SEGMENT).getAssets();
                Point2D location = findScaledXY(playerYacht.getLocation());
                segment.getTransforms().addAll(
                    new Translate(location.getX(), location.getY()),
                    new Rotate(playerYacht.getHeading(), new Point3D(0,0,1)),
                    new Scale(1, lastLocation.distance(location) / 5)
                );
                Platform.runLater(() -> {
                    trail.getChildren().add(segment);
                    if (trail.getChildren().size() > 100) {
                        trail.getChildren().remove(0);
                    }
                });
                lastLocation = location;
                // TODO: 11/09/2017 ROTATE PLAYER ICON
//                double leg = playerYacht.getLegNumber();
//                if (compoundMark != null) {
//                    for (Mark mark : compoundMark.getMarks()) {
////                System.out.println("markerObjects.get(mark) = " + markerObjects.get(mark));
//                        markerObjects.get(mark).showNextExitArrow();
//                    }
//                }
//                CompoundMark nextMark = null;
//                if (legNumber < course.size() - 1) {
//                    nextMark = course.get(legNumber);
//                    for (Mark mark : nextMark.getMarks()) {
//                        markerObjects.get(mark).showNextEnterArrow();
//                    }
//                }
            }
        }, 0L, 500L);

//        playerYacht.toggleSail();
//        boatObjects.get(playerYacht).setAsPlayer();
//        CompoundMark currentMark = course.get(playerYacht.getLegNumber());
//        for (Mark mark : currentMark.getMarks()) {
//            markerObjects.get(mark).showNextExitArrow();
//        }
//        annotations.get(playerYacht).addAnnotation(
//            "velocity",
//            playerYacht.getVelocityProperty(),
//            (velocity) -> String.format("Speed: %.2f ms", velocity.doubleValue())
//        );
//        Platform.runLater(() -> {
//            boatObjectGroup.getChildren().remove(boatObjects.get(playerYacht));
//            gameObjects.add(boatObjects.get(playerYacht));
//            annotationsGroup.getChildren().remove(annotations.get(playerYacht));
//            gameObjects.add(annotations.get(playerYacht));
//        });
//        playerYacht.addMarkRoundingListener(this::updateMarkArrows);
    }

    public void setWindDir(double windDir) {
        this.windDir = windDir;
    }
}
