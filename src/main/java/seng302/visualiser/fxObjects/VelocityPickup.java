package seng302.visualiser.fxObjects;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

/**
 * Created by cir27 on 3/09/17.
 */
public class VelocityPickup extends MeshView {

    public double rotation = 0;
    public Rotate timerRotation = new Rotate(0, new Point3D(0,0,1));

    public VelocityPickup () {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(getClass().getResource("/velocity_pickup.stl").toString());
        this.setMesh(importer.getImport());
        this.setMaterial(new PhongMaterial(Color.YELLOW));
        this.getTransforms().add(new Scale(30,30,30));
        this.getTransforms().add(new Rotate(30, new Point3D(1,0, 0)));
        this.getTransforms().add(new Rotate(90, new Point3D(0,1, 0)));
        this.getTransforms().add(timerRotation);
        AnimationTimer at = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Platform.runLater(() -> timerRotation.setAngle(rotation++));
            }
        };
        at.start();
    }
}
