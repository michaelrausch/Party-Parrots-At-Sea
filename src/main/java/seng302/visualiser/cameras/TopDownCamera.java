package seng302.visualiser.cameras;


import java.util.Arrays;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;

public class TopDownCamera extends PerspectiveCamera implements RaceCamera {

    private final Double PAN_LIMIT = 30.0;
    private final Double NEAR_ZOOM_LIMIT = -30.0;
    private final Double FAR_ZOOM_LIMIT = -130.0;
    private final Double ZOOM_STEP = 2.5;

    private ObservableList<Transform> transforms;
    private BoatObject playerBoat;

    private Double zoomFactor;
    private Double horizontalPan;
    private Double verticalPan;

    public TopDownCamera() {
        super(true);
        transforms = this.getTransforms();

        zoomFactor = (FAR_ZOOM_LIMIT + NEAR_ZOOM_LIMIT) / 2.0;
        horizontalPan = 0.0;
        verticalPan = 0.0;
    }

    public void setPlayerBoat(BoatObject playerBoat) {
        this.playerBoat = playerBoat;

        for (DoubleProperty o : Arrays
            .asList(playerBoat.layoutXProperty(), playerBoat.layoutYProperty())) {
            o.addListener((obs, oldVal, newVal) -> updateCamera());
        }
    }

    private void updateCamera() {
        transforms.clear();
        transforms.addAll(
            new Translate(playerBoat.getLayoutX() + horizontalPan,
                playerBoat.getLayoutY() + verticalPan, zoomFactor)
        );
    }

    private void adjustZoomFactor(Double adjustment) {
        if (zoomFactor + adjustment < NEAR_ZOOM_LIMIT && zoomFactor + adjustment > FAR_ZOOM_LIMIT) {
            zoomFactor = zoomFactor + adjustment;
            updateCamera();
        }
    }

    private void adjustVerticalPan(Double adjustment) {
        if (verticalPan + adjustment >= -PAN_LIMIT && verticalPan + adjustment <= PAN_LIMIT) {
            verticalPan += adjustment;
            updateCamera();
        }
    }

    private void adjustHorizontalPan(Double adjustment) {
        if (horizontalPan + adjustment >= -PAN_LIMIT && horizontalPan + adjustment <= PAN_LIMIT) {
            horizontalPan += adjustment;
            updateCamera();
        }
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
        adjustHorizontalPan(-1.0);
    }

    @Override
    public void panRight() {
        adjustHorizontalPan(1.0);
    }

    @Override
    public void panUp() {
        adjustVerticalPan(-1.0);
    }

    @Override
    public void panDown() {
        adjustVerticalPan(1.0);
    }

}
