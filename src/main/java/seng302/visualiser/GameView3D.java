package seng302.visualiser;

import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;

/**
 * Created by cir27 on 5/09/17.
 */
public class GameView3D {

    Group root3D;
    SubScene scene;
    Camera camera;
    Group gameObjects;

    public GameView3D () {
        camera = new PerspectiveCamera();
        gameObjects = new Group();
        root3D = new Group(camera, gameObjects);
        scene = new SubScene(
            root3D, 750, 750, true, SceneAntialiasing.BALANCED
        );
        scene.setCamera(camera);
    }

    public SubScene getScene () {
        return scene;
    }
}
