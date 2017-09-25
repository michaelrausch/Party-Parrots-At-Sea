package seng302.visualiser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.ClientYacht;
import seng302.model.Limit;
import seng302.model.ScaledPoint;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.model.token.Token;
import seng302.utilities.GeoUtility;
import seng302.utilities.Sounds;
import seng302.visualiser.fxObjects.MarkArrowFactory;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;
import seng302.visualiser.fxObjects.assets_3D.Marker3D;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
import seng302.visualiser.fxObjects.assets_3D.ModelType;

/**
 * Collection of animated3D assets that displays a race.
 */

public class GameView3D extends GameView{

    private final double FOV = 60;
    private final double DEFAULT_CAMERA_DEPTH = -125;
    private final double DEFAULT_CAMERA_X = 0;
    private final double DEFAULT_CAMERA_Y = 100;

    private Group root3D;
    private SubScene view;
    private PerspectiveCamera camera;
    private Group raceBorder = new Group();

    /* Note that if either of these is null then values for it have not been added and the other
       should be used as the limits of the map. */
    private Map<Mark, Marker3D> markerObjects;
    private Map<ClientYacht, BoatObject> boatObjects = new HashMap<>();
    private BoatObject selectedBoat = null;
    private Group wakesGroup = new Group();
    private Group boatObjectGroup = new Group();
    private List<Node> mapTokens;
    private AnimationTimer playerBoatAnimationTimer;
    private Group trail = new Group();
    private Double windDir;

    public GameView3D () {
        canvasWidth = canvasHeight = 220;
        camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
            new Translate(DEFAULT_CAMERA_X, DEFAULT_CAMERA_Y, DEFAULT_CAMERA_DEPTH)
        );
        camera.setFarClip(600);
        camera.setNearClip(0.1);
        camera.setFieldOfView(FOV);
        gameObjects = new Group();
        root3D = new Group(camera, gameObjects);
        view = new SubScene(
            root3D, 1000, 1000, true, SceneAntialiasing.BALANCED
        );
        view.setCamera(camera);
        camera.getTransforms().add(new Rotate(30, new Point3D(1,0,0)));

        gameObjects.getChildren().addAll(
            ModelFactory.importModel(ModelType.OCEAN).getAssets(),
            raceBorder, trail, markers, tokens
        );
        view.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.addEventHandler(KeyEvent.KEY_PRESSED, this::cameraMovement);
            }
        });
    }

    @Override
    public void updateCourse(List<CompoundMark> newCourse, List<Corner> sequence) {
        markerObjects = new HashMap<>();
        compoundMarks = newCourse;

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

        createMarkArrows();

        //Scale race to markers if there is no border.
        if (borderPoints == null) {
            scaledPoint = ScaledPoint.makeScaledPoint(
                canvasWidth, canvasHeight, new ArrayList<>(markerObjects.keySet()), true
            );
        }
        //Move the Markers to initial position.
        markerObjects.forEach(((mark, marker) -> {
            Point2D p2d = scaledPoint.findScaledXY(mark.getLat(), mark.getLng());
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
        markerObjects.put(observableMark, new Marker3D(markerType));
        observableMark.addPositionListener((mark, lat, lon) -> {
            Point2D p2d = scaledPoint.findScaledXY(lat, lon);
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
        Point2D m1Location = scaledPoint.findScaledXY(m1);
        Point2D m2Location = scaledPoint.findScaledXY(m2);

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
     * Calculates all the data needed for to create mark arrows. Requires that a course has been
     * added to the gameview.
     */
    private void createMarkArrows () {
        for (int i=1; i < course.size()-1; i++) { //General case.
            for (Mark mark : course.get(i).getMarks()) {
                markerObjects.get(mark).addArrows(
                    mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                    GeoUtility.getBearing(course.get(i-1).getMidPoint(), mark),
                    GeoUtility.getBearing(mark, course.get(i+1).getMidPoint())
                );
            }
        }
        createStartLineArrows();
        createFinishLineArrows();
    }

    private void createStartLineArrows () {
        for (Mark mark : course.get(0).getMarks()) {
            markerObjects.get(mark).addArrows(
                mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                0d, //90
                GeoUtility.getBearing(mark, course.get(1).getMidPoint())
            );
        }
    }

    private void createFinishLineArrows () {
        for (Mark mark : course.get(course.size()-1).getMarks()) {
            markerObjects.get(mark).addArrows(
                mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                GeoUtility.getBearing(course.get(course.size()-2).getMidPoint(), mark),
                GeoUtility.getBearing(mark, mark)
            );
        }
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
            case Z:
                camera.getTransforms().addAll(new Translate(0, 0, 1.5));
                break;
            case X:
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
     * Draws all the boats.
     * @param  yachts The yachts to set in the race
     */
    public void setBoats(List<ClientYacht> yachts) {
        BoatObject newBoat;
        final List<Group> wakes = new ArrayList<>();
        for (ClientYacht clientYacht : yachts) {
            Color colour = clientYacht.getColour();
            newBoat = new BoatObject(clientYacht.getBoatType());
            newBoat.setFill(colour);
            boatObjects.put(clientYacht, newBoat);
            wakesGroup.getChildren().add(newBoat.getWake());
            wakes.add(newBoat.getWake());
            boatObjectGroup.getChildren().add(newBoat);
            clientYacht.addLocationListener((boat, lat, lon, heading, sailIn, velocity) -> {
                BoatObject bo = boatObjects.get(boat);
                Point2D p2d = scaledPoint.findScaledXY(lat, lon);
                bo.moveTo(p2d.getX(), p2d.getY(), heading, velocity, sailIn, windDir);
            });
        }
        Platform.runLater(() -> {
            gameObjects.getChildren().addAll(wakes);
            gameObjects.getChildren().addAll(boatObjectGroup);
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
            scaledPoint = ScaledPoint.makeScaledPoint(
                canvasWidth, canvasHeight, new ArrayList<>(borderPoints), true
            );
        }
        List<Node> boundaryAssets = new ArrayList<>();

        Point2D lastLocation = scaledPoint.findScaledXY(border.get(0).getLat(), border.get(0).getLng());
        Group pylon = ModelFactory.importModel(ModelType.BORDER_PYLON).getAssets();
        pylon.setLayoutX(lastLocation.getX());
        pylon.setLayoutY(lastLocation.getY());
        boundaryAssets.add(pylon);

        for (int i=1; i<border.size(); i++) {
            Point2D location = scaledPoint.findScaledXY(border.get(i).getLat(), border.get(i).getLng());
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
                new Scale((lastLocation.distance(location) / 10)-0.2, 1, 1)
            );

            Point2D midPoint = location.midpoint(lastLocation);
            barrier.setLayoutX(midPoint.getX());
            barrier.setLayoutY(midPoint.getY());

            lastLocation = location;

            boundaryAssets.add(barrier);
            boundaryAssets.add(pylon);
        }

        Point2D firstLocation = scaledPoint.findScaledXY(border.get(0).getLat(), border.get(0).getLng());
        Group barrier = ModelFactory.importModel(ModelType.BORDER_BARRIER).getAssets();
        barrier.getTransforms().addAll(
            new Rotate(
                Math.toDegrees(
                    Math.atan2(lastLocation.getY() - firstLocation.getY(), lastLocation.getX() - firstLocation.getX())
                ),
                new Point3D(0,0,1)
            ),
            new Scale((firstLocation.distance(lastLocation) / 10)-0.2, 1, 1)
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
            Point2D location = scaledPoint.findScaledXY(token.getLat(), token.getLng());
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
        playerYacht.toggleSail();
        playerBoatAnimationTimer = new AnimationTimer() {

            Point2D lastLocation = scaledPoint.findScaledXY(playerYacht.getLocation());

            @Override
            public void handle(long now) {
                Point2D location = scaledPoint.findScaledXY(playerYacht.getLocation());
                if (Math.abs(lastLocation.distance(location)) > 2) {
                    Node segment = ModelFactory.importModel(ModelType.TRAIL_SEGMENT).getAssets();
                    location = scaledPoint.findScaledXY(playerYacht.getLocation());
                    segment.getTransforms().addAll(
                        new Translate(location.getX(), location.getY(), 0),
                        new Rotate(playerYacht.getHeading(), new Point3D(0,0,1))
                    );
                    trail.getChildren().add(segment);
                    if (trail.getChildren().size() > 50) {
                        trail.getChildren().remove(0);
                    }
                    lastLocation = location;
                }
            }
        };
        playerBoatAnimationTimer.start();
        playerYacht.addMarkRoundingListener(this::updateMarkArrows);
        boatObjects.get(playerYacht).addSelectedBoatListener((boatObject, isSelected) -> {
            System.out.println("IS SELECTED " + isSelected);
        });
    }

    public void setWindDir(double windDir) {
        this.windDir = windDir;
    }

    private void updateMarkArrows (ClientYacht yacht, int legNumber) {
        CompoundMark compoundMark;
        if (legNumber - 1 >= 0) {
            Sounds.playMarkRoundingSound();
            compoundMark = course.get(legNumber-1);
            for (Mark mark : compoundMark.getMarks()) {
                markerObjects.get(mark).showNextExitArrow();
            }
        }
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
    }
}
