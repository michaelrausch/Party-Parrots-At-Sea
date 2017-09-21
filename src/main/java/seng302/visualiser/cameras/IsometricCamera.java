package seng302.visualiser.cameras;

import javafx.collections.ObservableList;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class IsometricCamera extends PerspectiveCamera implements RaceCamera {

    ObservableList<Transform> transforms;

    public IsometricCamera(Double cameraStartX, Double cameraStartY, Double cameraDepth) {
        super(true);
        transforms = this.getTransforms();
        transforms.addAll(new Translate(cameraStartX, cameraStartY, cameraDepth));
    }

    @Override
    public void zoomIn() {
        transforms.addAll(new Translate(0, 0, 1.5));
    }

    @Override
    public void zoomOut() {
        transforms.addAll(new Translate(0, 0, -1.5));
    }

    @Override
    public void panLeft() {
        transforms.addAll(new Translate(-1, 0, 0));
    }

    @Override
    public void panRight() {
        transforms.addAll(new Translate(1, 0, 0));
    }

    @Override
    public void panUp() {
        transforms.addAll(new Translate(0, -1, 0));
    }

    @Override
    public void panDown() {
        transforms.addAll(new Translate(0, 1, 0));
    }
}
