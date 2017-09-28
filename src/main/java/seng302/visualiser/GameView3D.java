package seng302.visualiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.fxyz3d.scene.Skybox;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.ClientYacht;
import seng302.model.GameKeyBind;
import seng302.model.KeyAction;
import seng302.model.Limit;
import seng302.model.ScaledPoint;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.model.token.Token;
import seng302.utilities.GeoUtility;
import seng302.visualiser.cameras.ChaseCamera;
import seng302.visualiser.cameras.IsometricCamera;
import seng302.visualiser.cameras.RaceCamera;
import seng302.visualiser.cameras.TopDownCamera;
import seng302.visualiser.controllers.ViewManager;
import seng302.visualiser.fxObjects.MarkArrowFactory;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;
import seng302.visualiser.fxObjects.assets_3D.Marker3D;
import seng302.visualiser.fxObjects.assets_3D.Model;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
import seng302.visualiser.fxObjects.assets_3D.ModelType;

/**
 * Collection of animated3D assets that displays a race.
 */

public class GameView3D extends GameView {

    private final double FOV = 60;
    private final double DEFAULT_CAMERA_X = 0;
    private final double DEFAULT_CAMERA_Y = 160;

    private Group root3D;
    private SubScene view;
    private Group gameObjects;

    private Group raceBorder = new Group();
    // Cameras
    private PerspectiveCamera isometricCam;
    private PerspectiveCamera topDownCam;
    private PerspectiveCamera chaseCam;
    private BoatObject playerBoat;
    private Map<ClientYacht, BoatObject> boatObjects = new HashMap<>();
    private Group wakesGroup = new Group();
    private Group boatObjectGroup = new Group();
    private List<Node> mapTokens;
    private AnimationTimer playerBoatAnimationTimer;
    private Group trail = new Group();
    private Double windDir;
    private Skybox skybox;

    public GameView3D () {
        isometricCam = new IsometricCamera(DEFAULT_CAMERA_X, DEFAULT_CAMERA_Y);
        topDownCam = new TopDownCamera();
        chaseCam = new ChaseCamera();

        canvasWidth = canvasHeight = 300;

        for (PerspectiveCamera pc : Arrays.asList(isometricCam, topDownCam, chaseCam)) {
            pc.setFarClip(100000);
            pc.setNearClip(0.1);
            pc.setFieldOfView(FOV);
        }

        gameObjects = new Group();
        root3D = new Group(chaseCam, gameObjects);
        view = new SubScene(
            root3D, 5000, 3000, true, SceneAntialiasing.BALANCED
        );
        view.setCamera(chaseCam);

        skybox = new Skybox(new Image(getClass().getResourceAsStream("/images/skybox.jpg")), 100000, isometricCam);
        skybox.getTransforms().addAll(new Rotate(90, Rotate.X_AXIS));

        Model land = ModelFactory.importModel(ModelType.LAND_SMOOTH);
        land.getAssets().getTransforms().add(new Rotate(90, Rotate.X_AXIS));

        gameObjects.getChildren().addAll(
            raceBorder, trail, markers, tokens, skybox, land.getAssets()
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
        GameKeyBind keyBinds = GameKeyBind.getInstance();
        KeyAction keyPressed = keyBinds.getKeyAction(event.getCode());
        if (keyPressed != null) {
            switch (keyPressed) {
                case ZOOM_IN:
                    ((RaceCamera) view.getCamera()).zoomIn();
                    break;
                case ZOOM_OUT:
                    ((RaceCamera) view.getCamera()).zoomOut();
                    break;
                case FORWARD:
                    ((RaceCamera) view.getCamera()).panUp();
                    break;
                case BACKWARD:
                    ((RaceCamera) view.getCamera()).panDown();
                    break;
                case LEFT:
                    ((RaceCamera) view.getCamera()).panLeft();
                    break;
                case RIGHT:
                    ((RaceCamera) view.getCamera()).panRight();
                    break;
                case VIEW:
                    toggleCamera();
                    break;
            }
        }
    }

    private void toggleCamera() {
        Camera currCamera = view.getCamera();

        if (currCamera.equals(isometricCam)) {
            view.setCamera(topDownCam);
        } else if (currCamera.equals(topDownCam)) {
            view.setCamera(chaseCam);
        } else {
            view.setCamera(isometricCam);
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
            clientYacht.addLocationListener(this::updateBoatLocation);
            clientYacht.addColorChangeListener(this::updateBoatColor);

            if (clientYacht.getSourceId().equals(
                ViewManager.getInstance().getGameClient().getServerThread().getClientId())) {
                ((ChaseCamera) chaseCam).setPlayerBoat(newBoat);
                ((TopDownCamera) topDownCam).setPlayerBoat(newBoat);

                newBoat.setMarkIndicator(ModelFactory.importSTL("mark_pointer.stl"));
                playerBoat = newBoat;

            }
        }
        Platform.runLater(() -> {
            ClientYacht playerYacht = ViewManager.getInstance().getGameClient().getAllBoatsMap()
                .get(ViewManager.getInstance().getGameClient().getServerThread().getClientId());

            for (ObservableValue o : Arrays
                .asList(playerBoat.layoutXProperty(), playerBoat.layoutXProperty())) {
                o.addListener((obs, oldVal, newVal) -> {
                    if (playerYacht.getLegNumber() < course.size()) {
                        List<Mark> marks = course.get(playerYacht.getLegNumber()).getMarks();
                        Point2D midPoint = new Point2D(0, 0);
                        if (marks.size() == 1) {
                            midPoint = scaledPoint.findScaledXY(marks.get(0));
                        } else if (marks.size() == 2) {
                            midPoint = (scaledPoint.findScaledXY(marks.get(0)))
                                .midpoint(scaledPoint.findScaledXY(marks.get(1)));
                        }

                        if (midPoint != null) {
                            playerBoat.updateMarkIndicator(midPoint);
                        }
                    }

                });
            }
            gameObjects.getChildren().addAll(wakes);
            gameObjects.getChildren().addAll(boatObjectGroup);
        });
    }

    public Node getAssets () {
        return view;
    }

    /**
     * Updates the boatObjects color with that of the clientYachts object. Used in notification from
     * a listener on this attribute in clientYacht to re paint the boat mesh
     *
     * @param clientYacht The yacht to update the colour for
     */
    private void updateBoatColor(ClientYacht clientYacht) {
        boatObjects.get(clientYacht).setFill(clientYacht.getColour());
    }

    private void updateBoatLocation(ClientYacht boat, Double lat, Double lon, Double heading,
        Boolean sailIn, Double velocity) {
        BoatObject bo = boatObjects.get(boat);
        Point2D p2d = scaledPoint.findScaledXY(lat, lon);
        bo.moveTo(p2d.getX(), p2d.getY(), heading, velocity, sailIn, windDir);
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

            ModelType modelType = null;
            switch (token.getTokenType()) {
                case BOOST:
                    modelType = ModelType.VELOCITY_PICKUP;
                    break;
                case HANDLING:
                    modelType = ModelType.HANDLING_PICKUP;
                    break;
                case BUMPER:
                    modelType = ModelType.BUMPER_PICKUP;
                    break;
                case RANDOM:
                    modelType = ModelType.RANDOM_PICKUP;
                    break;
                case WIND_WALKER:
                    modelType = ModelType.WIND_WALKER_PICKUP;
                    break;
            }

            Node tokenObject = ModelFactory.importModel(modelType).getAssets();
            tokenObject.setLayoutX(location.getX());
            tokenObject.setLayoutY(location.getY());
            mapTokens.add(tokenObject);
        }

        Platform.runLater(() -> {
            tokens.getChildren().setAll(mapTokens);
        });
    }

    public void setBoatAsPlayer (ClientYacht playerYacht) {
        playerBoat.updateMarkIndicator(scaledPoint.findScaledXY(course.get(0).getMidPoint()));
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
}