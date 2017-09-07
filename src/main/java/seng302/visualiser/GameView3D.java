package seng302.visualiser;

import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;
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
    PerspectiveCamera camera;
    Group gameObjects;

    public GameView3D () {
        camera = new PerspectiveCamera(true);
        camera.getTransforms().addAll(
            new Translate(0,0, -DEFAULT_CAMERA_DEPTH)
        );
        camera.setFarClip(0);
        camera.setFieldOfView(FOV);
        gameObjects = new Group();
        root3D = new Group(camera, gameObjects);
        view = new SubScene(
            root3D, 1000, 1000, true, SceneAntialiasing.BALANCED
        );
        view.setCamera(camera);
        Sphere s = new Sphere(1);
        s.setMaterial(new PhongMaterial(Color.RED));
        Sphere left = new Sphere(1);
        left.setMaterial(new PhongMaterial(Color.LEMONCHIFFON));
        left.getTransforms().add(new Translate(-Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0, 0));
        Sphere right = new Sphere(1);
        right.setMaterial(new PhongMaterial(Color.ROSYBROWN));
        right.getTransforms().add(new Translate(Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0, 0));
        Sphere top = new Sphere(1);
        top.setMaterial(new PhongMaterial(Color.TEAL));
        top.getTransforms().add(new Translate(0,-Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0));
        Sphere bottom = new Sphere(1);
        bottom.setMaterial(new PhongMaterial(Color.BLANCHEDALMOND));
        bottom.getTransforms().add(new Translate(0, Math.tan(Math.toRadians(FOV / 2)) * DEFAULT_CAMERA_DEPTH, 0));
        
        Node boat = ModelFactory.boatGameView(BoatMeshType.DINGHY, Color.BLUE).getAssets();
        Node boat2 = ModelFactory.boatGameView(BoatMeshType.DINGHY, Color.BROWN).getAssets();
        boat2.getTransforms().add(new Translate(0,20, 0));
        Node boat3 = ModelFactory.boatGameView(BoatMeshType.DINGHY, Color.RED).getAssets();
        boat3.getTransforms().add(new Translate(0,-20, 0));

        Node sMarker = ModelFactory.importModel(ModelType.START_MARKER).getAssets();
        sMarker.getTransforms().add(0, new Translate(30, 30, 0));

        Node fMarker = ModelFactory.importModel(ModelType.FINISH_MARKER).getAssets();
        fMarker.getTransforms().add(0, new Translate(30, -30, 0));

        Node marker = ModelFactory.importModel(ModelType.PLAIN_MARKER).getAssets();
        marker.getTransforms().add(0, new Translate(30, 0, 0));

        Node coin = ModelFactory.importModel(ModelType.VELOCITY_COIN).getAssets();
        coin.setTranslateX(coin.getTranslateX() - 30);

        gameObjects.getChildren().addAll(s, left, right, top, bottom, boat, boat2, boat3, sMarker, fMarker, marker, coin);
        view.sceneProperty().addListener((obs, old, scene) -> {
            if (scene!=null)
                makeMovement(scene);
        });
    }

    public void makeMovement(Scene s) {
        s.setOnKeyPressed(event -> {
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
                case SPACE:
                    camera.getTransforms().addAll(new Translate(0, 0, 0.75));
                    break;
                case Z:
                    camera.getTransforms().addAll(new Translate(0, 0, -0.75));
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
        });
    }

    public Node getAssets () {
        return view;
    }
}
