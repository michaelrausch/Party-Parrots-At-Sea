package seng302.visualiser.cameras;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;

public class TopDownCamera extends PerspectiveCamera implements RaceCamera {

    private ObservableList<Transform> transforms;
    private BoatObject playerBoat;

    public TopDownCamera() {
        super(true);
        transforms = this.getTransforms();
        transforms.add(new Translate(0, 0, -125));
    }

    public void setPlayerBoat(BoatObject playerBoat) {
        this.playerBoat = playerBoat;
        this.playerBoat.layoutXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                Number newValue) {
                updateCameraX((Double) oldValue, (Double) newValue);
            }
        });
        this.playerBoat.layoutYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                Number newValue) {
                updateCameraY((Double) oldValue, (Double) newValue);
            }
        });
    }


    private void updateCameraX(Double oldXValue, Double newXValue) {
        if (transforms.size() == 0) { // boat is placed and then moved at start,
            transforms.addAll(
                new Translate(playerBoat.getLayoutX(), playerBoat.getLayoutY(), -125)
            );
        } else {
            transforms.addAll(new Translate(newXValue - oldXValue, 0, 0));
        }
    }

    private void updateCameraY(Double oldYValue, Double newYValue) {
        transforms.addAll(new Translate(0, (newYValue - oldYValue), 0));
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
