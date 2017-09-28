package seng302.visualiser.controllers.cells;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
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

    // Cameras
    private PerspectiveCamera camera = null;
    private ObservableList<Transform> cameraTransforms;

    /**
     * Initialise WindCell fxml and load 3D wind arrow into a group.
     */
    public void initialize() {
        camera = new PerspectiveCamera();
        camera.setFarClip(1000);
        camera.setNearClip(0.1);
        camera.setFieldOfView(60);
        this.cameraTransforms = camera.getTransforms();
        initialiseWindView();
    }

    private void initialiseWindView() {
        gameObjects = new Group();
        System.out.println(windPane);
        windPane.getChildren().add(gameObjects);

        root3D = new Group(camera, gameObjects);
        view = new SubScene(
            root3D, 110, 120, true, SceneAntialiasing.BALANCED
        );
        view.setCamera(camera);

        Model windArrowModel = ModelFactory.makeWindArrow();

        gameObjects.getChildren().addAll(
            windArrowModel.getAssets()
        );
    }

    public Node getAssets() {
        return view;
    }

    public void setCamera(Camera camera) {
        this.camera.getTransforms().clear();
        for (Transform t : camera.getTransforms()) {
            if (!(t instanceof Translate)) {
                this.camera.getTransforms().add(t);
            }
        }
    }
}
