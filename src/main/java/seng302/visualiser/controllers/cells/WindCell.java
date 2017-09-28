package seng302.visualiser.controllers.cells;

import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import seng302.model.ClientYacht;
import seng302.visualiser.cameras.ChaseCamera;
import seng302.visualiser.fxObjects.assets_3D.Model;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;

public class WindCell {

    //--------FXML BEGIN--------//
    @FXML
    private Pane windPane;
    //---------FXML END---------//

    private final double FOV = 60;
    private final double DEFAULT_CAMERA_X = 0;
    private final double DEFAULT_CAMERA_Y = 50;

    private Group root3D;
    private SubScene view;
    private Group gameObjects;

    private ChaseCamera chaseCam;

    private ClientYacht playerYacht;

    // Cameras
    private PerspectiveCamera camera = null;

    private Model windArrowModel;
    private Boolean isChaseCam;

    /**
     * Initialise WindCell fxml and load 3D wind arrow into a group.
     */
    public void init(ClientYacht playerYacht, ReadOnlyDoubleWrapper windDirection) {

        this.playerYacht = playerYacht;
        camera = new PerspectiveCamera();
        camera.setFarClip(1000);
        camera.setNearClip(0.1);
        camera.setFieldOfView(60);
        initialiseWindView();

        for (DoubleProperty o : Arrays.asList(playerYacht.getHeadingProperty(), windDirection)) {
            o.addListener((obs, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (isChaseCam) {
                        camera.getTransforms().clear();
                        for (Transform t : chaseCam.getTransforms()) {
                            if (t instanceof Rotate) {
                                camera.getTransforms().add(t);
                            }
                        }
                        this.camera.getTransforms().addAll(
                            new Translate(-55, -60, 0)
                        );
                    }

                    windArrowModel.getAssets().getTransforms().clear();
                    windArrowModel.getAssets().getTransforms().addAll(
                        new Rotate(windDirection.getValue(),
                            new Point3D(0, 0, 1))
                    );
                });
            });
        }
    }

    private void initialiseWindView() {
        gameObjects = new Group();
        windPane.getChildren().add(gameObjects);

        root3D = new Group(camera, gameObjects);
        view = new SubScene(
            root3D, 110, 120, true, SceneAntialiasing.BALANCED
        );
        view.setCamera(camera);

        windArrowModel = ModelFactory.makeWindArrow();

        gameObjects.getChildren().addAll(
            windArrowModel.getAssets()
        );
    }

    public Node getAssets() {
        return view;
    }


    public void updateCameraTransforms(Camera camera) {
        this.camera.getTransforms().clear();

        for (Transform transform : camera.getTransforms()) {
            if (!(transform instanceof Translate)) {
                this.camera.getTransforms().add(transform);
            }
        }
        this.camera.getTransforms().addAll(
            new Translate(-55, -60, 0)
        );
        windArrowModel.getAssets().getTransforms().clear();
    }

    public void setCamera(Camera camera) {
        isChaseCam = camera instanceof ChaseCamera;
        if (isChaseCam) {
            this.chaseCam = (ChaseCamera) camera;
        } else {
            this.chaseCam = null;
        }
        updateCameraTransforms(camera);
    }
}
