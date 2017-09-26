package seng302.visualiser.cameras;

import java.util.Arrays;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;


public class ChaseCamera extends PerspectiveCamera implements RaceCamera {

    private final Double VERTICAL_PAN_LIMIT = 20.0;
    private final Double NEAR_ZOOM_LIMIT = -15.0;
    private final Double FAR_ZOOM_LIMIT = -125.0;

    private final Double ZOOM_STEP = 2.5;
    private final Double PAN_STEP = 2.5;

    private ObservableList<Transform> transforms;
    private BoatObject playerBoat;

    private Double zoomFactor;
    private Double horizontalPan;
    private Double verticalPan;


    public ChaseCamera() {
        super(true);
        transforms = this.getTransforms();

        zoomFactor = (FAR_ZOOM_LIMIT + NEAR_ZOOM_LIMIT) / 2.0;
        this.horizontalPan = 0.0;
        this.verticalPan = 0.0;
    }

    public void setPlayerBoat(BoatObject playerBoat) {
        this.playerBoat = playerBoat;

        for (DoubleProperty o : Arrays
            .asList(playerBoat.getRotationProperty(), playerBoat.layoutYProperty(),
                playerBoat.layoutXProperty())) {
            o.addListener((obs, oldVal, newVal) -> repositionCamera());
        }
    }

    private void repositionCamera() {
        transforms.clear();
        transforms.addAll(
            new Translate(playerBoat.getLayoutX(), playerBoat.getLayoutY(), 0),
            new Rotate(playerBoat.getRotationProperty().getValue() + horizontalPan,
                new Point3D(0, 0, 1)),
            new Rotate(60 + verticalPan, new Point3D(1, 0, 0)),
            new Translate(0, 0, zoomFactor)
        );
    }

    private void adjustZoomFactor(Double adjustment) {
        if (zoomFactor + adjustment < NEAR_ZOOM_LIMIT && zoomFactor + adjustment > FAR_ZOOM_LIMIT) {
            zoomFactor = zoomFactor + adjustment;
            repositionCamera();
        }
    }

    private void adjustVerticalPan(Double adjustment) {
        if (verticalPan + adjustment >= -VERTICAL_PAN_LIMIT
            && verticalPan + adjustment <= VERTICAL_PAN_LIMIT) {
            verticalPan += adjustment;
            repositionCamera();
        }
    }

    private void adjustHorizontalPan(Double adjustment) {
        this.horizontalPan += adjustment;
        repositionCamera();
    }

    @Override
    public void zoomIn() {
        adjustZoomFactor(ZOOM_STEP);
    }

    @Override
    public void zoomOut() {
        adjustZoomFactor(-ZOOM_STEP);
    }

    @Override
    public void panLeft() {
        adjustHorizontalPan(-PAN_STEP);
    }

    @Override
    public void panRight() {
        adjustHorizontalPan(PAN_STEP);
    }

    @Override
    public void panUp() {
        adjustVerticalPan(-PAN_STEP);
    }

    @Override
    public void panDown() {
        adjustVerticalPan(PAN_STEP);
    }
}
