package seng302.visualiser.controllers.cells;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.layout.Pane;
import seng302.visualiser.cameras.ChaseCamera;
import seng302.visualiser.cameras.IsometricCamera;
import seng302.visualiser.cameras.TopDownCamera;
import seng302.visualiser.fxObjects.assets_3D.Model;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;

public class WindCell {

    //--------FXML BEGIN--------//
    @FXML
    private Pane windPane;
    //---------FXML END---------//

    private final double FOV = 60;
    private final double DEFAULT_CAMERA_X = 0;
    private final double DEFAULT_CAMERA_Y = 155;

    // Cameras
    private PerspectiveCamera isometricCam;
    private PerspectiveCamera topDownCam;
    private PerspectiveCamera chaseCam;

    /**
     * Initialise WindCell fxml and load 3D wind arrow into a group.
     */
    public void initialize() {
        Group group = new Group();
        windPane.getChildren().add(group);
        Model windArrowModel = ModelFactory.makeWindArrow();
        group.getChildren().add(windArrowModel.getAssets());

        isometricCam = new IsometricCamera(DEFAULT_CAMERA_X, DEFAULT_CAMERA_Y);
        topDownCam = new TopDownCamera();
        chaseCam = new ChaseCamera();
    }


}
