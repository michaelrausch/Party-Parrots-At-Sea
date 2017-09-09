package seng302.visualiser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
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
import seng302.utilities.GeoUtility;
import seng302.visualiser.fxObjects.assets_2D.AnnotationBox;
import seng302.visualiser.fxObjects.assets_2D.BoatObject;
import seng302.visualiser.fxObjects.assets_2D.CourseBoundary;
import seng302.visualiser.fxObjects.assets_2D.Gate;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
import seng302.visualiser.fxObjects.assets_3D.ModelType;

/**
 * Collection of animated3D assets that displays a race.
 */

public class GameView3D {

    private final double FOV = 60;
    private final double DEFAULT_CAMERA_DEPTH = 100;

    Group root3D;
    SubScene view;
    ParallelCamera camera;
    Group gameObjects;

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
    private Polygon raceBorder = new CourseBoundary();

    /* Note that if either of these is null then values for it have not been added and the other
       should be used as the limits of the map. */
    private List<Limit> borderPoints;
    private Map<Mark, Group> markerObjects;

    private Map<ClientYacht, BoatObject> boatObjects = new HashMap<>();
    private Map<ClientYacht, AnnotationBox> annotations = new HashMap<>();
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
//        camera = new PerspectiveCamera(true);
        camera = new ParallelCamera();
        camera.getTransforms().addAll(
            new Translate(0,0, -DEFAULT_CAMERA_DEPTH)
        );
        camera.setFarClip(Double.MAX_VALUE);
        camera.setNearClip(0.1);
//        camera.setFieldOfView(FOV);
        gameObjects = new Group();
        gameObjects.getTransforms().add(new Scale(4,4,4));
        root3D = new Group(camera, gameObjects);
        view = new SubScene(
            root3D, 1000, 1000, true, SceneAntialiasing.BALANCED
        );
        view.setCamera(camera);
        view.setFill(Color.SKYBLUE);
        camera.getTransforms().add(new Rotate(30, new Point3D(1,0,0)));
        camera.setLayoutX(camera.getLayoutX()-400);
        camera.setLayoutY(camera.getLayoutX()-600);
//        gameObjects.getChildren().addAll(raceBorder, markers, tokens);
        gameObjects.getChildren().addAll(
            ModelFactory.importModel(ModelType.OCEAN).getAssets(), markers
        );

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
//        Node coin = ModelFactory.importModel(ModelType.VELOCITY_COIN).getAssets();
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

        final List<Gate> gates = new ArrayList<>();
        Paint colour = Color.BLACK;
        //Creates new markers
        for (CompoundMark cMark : newCourse) {
            //Set start and end colour
//            if (cMark.getId() == sequence.get(0).getCompoundMarkID()) {
//                colour = Color.GREEN;
//            } else if (cMark.getId() == sequence.get(sequence.size() - 1).getCompoundMarkID()) {
//                colour = Color.RED;
//            }
            //Create mark dots
            for (Mark mark : cMark.getMarks()) {
                makeAndBindMarker(mark);
            }
//            //Create gate line
//            if (cMark.isGate()) {
//                for (int i = 1; i < cMark.getMarks().size(); i++) {
//                    gates.add(
//                        makeAndBindGate(
//                            markerObjects.get(cMark.getSubMark(i)),
//                            markerObjects.get(cMark.getSubMark(i + 1)),
//                            colour
//                        )
//                    );
//                }
//            }
//            colour = Color.BLACK;
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
     */
    private void makeAndBindMarker(Mark observableMark) {

        Group marker = ModelFactory.importModel(ModelType.PLAIN_MARKER).getAssets();

        markerObjects.put(observableMark, marker);
        observableMark.addPositionListener((mark, lat, lon) -> {
            Point2D p2d = findScaledXY(lat, lon);
            markerObjects.get(mark).setLayoutX(p2d.getX());
            markerObjects.get(mark).setLayoutY(p2d.getY());
        });
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

    public void cameraMovement(KeyEvent event) {
        switch (event.getCode()) {
            case UP:
                camera.getTransforms().addAll(new Rotate(0.5, new Point3D(1,0,0)));
                break;
            case DOWN:
                camera.getTransforms().addAll(new Rotate(-0.5, new Point3D(1,0,0)));
                break;
            case LEFT:
                camera.getTransforms().addAll(new Rotate(-0.5, new Point3D(0,1,0)));
                break;
            case RIGHT:
                camera.getTransforms().addAll(new Rotate(0.5, new Point3D(0,1,0)));
                break;
            case X:
                camera.getTransforms().addAll(new Translate(0, 0, 1.5));
                break;
            case Z:
                camera.getTransforms().addAll(new Translate(0, 0, -1.5));
                break;
            case W:
                camera.getTransforms().addAll(new Translate(0, 1, 0));
                break;
            case S:
                camera.getTransforms().addAll(new Translate(0, -1, 0));
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
//            wakesGroup.getChildren().add(newBoat.getWake());
//            wakes.add(newBoat.getWake());
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
            gameObjects.getChildren().addAll(boatObjectGroup);
//            gameObjects.addAll(trails);
//            gameObjects.addAll(wakes);
//            gameObjects.addAll(annotationsGroup);
//            gameObjects.addAll(boatObjectGroup);
        });
    }

    public Node getAssets () {
        return view;
    }
}
