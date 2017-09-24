package seng302.visualiser.cameras;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;


public class ChaseCamera extends PerspectiveCamera implements RaceCamera {

    private ObservableList<Transform> transforms;
    private BoatObject playerBoat;

    public ChaseCamera() {
        super(true);
        transforms = this.getTransforms();
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
                new Translate(playerBoat.getLayoutX() - 30, playerBoat.getLayoutY() - 30, -125),
                new Rotate(80, new Point3D(0, 0, 1))
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


    /*
    These have been left intentionally empty for now. it would be cool to be able to pan around the boat and have the camera move around the boat though.
    */

    @Override
    public void panLeft() {
    }

    @Override
    public void panRight() {
    }

    @Override
    public void panUp() {
    }

    @Override
    public void panDown() {
    }
}
