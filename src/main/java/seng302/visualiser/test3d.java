package seng302.visualiser;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * Created by cir27 on 7/09/17.
 */
public class test3d extends Application {

    Group root3D;
    Scene scene;
    Camera camera;
    Group gameObjects;

    @Override
    public void start(Stage primaryStage) throws Exception {
        camera = new PerspectiveCamera();
        gameObjects = new Group();
        root3D = new Group(camera, gameObjects);
        scene = new Scene(
            root3D, 750, 750, true, SceneAntialiasing.BALANCED
        );
        scene.setCamera(camera);
        primaryStage.setScene(scene);
        primaryStage.show();

//        StlMeshImporter importer = new StlMeshImporter();
//        importer.read(getClass().getResource("/meshes/boat_hull.stl"));
//        MeshView boat = new MeshView(importer.getImport());
//        boat.setMaterial(new PhongMaterial(Color.BEIGE));
//
//        importer = new StlMeshImporter();
//        importer.read(getClass().getResource("/meshes/boat_mast.stl").toString());
//        MeshView mast = new MeshView(importer.getImport());
//        mast.setMaterial(new PhongMaterial(Color.BEIGE));
//
//        importer = new StlMeshImporter();
//        importer.read(getClass().getResource("/meshes/boat_sail.stl").toString());
//        MeshView sail = new MeshView(importer.getImport());
//        sail.setMaterial(new PhongMaterial(Color.WHITE));

//        gameObjects.getChildren().addAll(boat, mast, sail);

        gameObjects.getTransforms().add(new Scale(40, 40,40));
        gameObjects.getTransforms().add(new Translate(10, 10,0));
        gameObjects.getTransforms().addAll(
            new Rotate(90, new Point3D(0,0,1)),
            new Rotate(90, new Point3D(0, 1, 0))
        );

        PointLight light = new PointLight();
        light.setLightOn(true);
        light.getTransforms().add(new Translate(10, 10, -50));
        root3D.getChildren().add(light);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    gameObjects.getTransforms().add(new Rotate(5, new Point3D(0,0,1)));
                    break;
                case DOWN:
                    gameObjects.getTransforms().add(new Rotate(-5, new Point3D(0,0,1)));
                    break;
                case LEFT:
                    gameObjects.getTransforms().add(new Rotate(-5, new Point3D(0,1,0)));
                    break;
                case RIGHT:
                    gameObjects.getTransforms().add(new Rotate(5, new Point3D(0,1,0)));
                    break;
            }
        });
    }
}
