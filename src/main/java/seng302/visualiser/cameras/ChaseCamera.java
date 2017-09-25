package seng302.visualiser.cameras;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import seng302.model.ClientYacht;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;


public class ChaseCamera extends PerspectiveCamera implements RaceCamera {

    private ObservableList<Transform> transforms;
    private BoatObject playerBoat;
    private ClientYacht playerYacht;
    private Double zoomFactor;


    public ChaseCamera() {
        super(true);
        transforms = this.getTransforms();
        this.zoomFactor = -75.0;
    }

    public void setPlayerBoat(BoatObject playerBoat, ClientYacht playerYacht) {
        this.playerBoat = playerBoat;
        this.playerYacht = playerYacht;

        this.playerYacht.getHeadingProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                Number newValue) {
                repositionCamera();
            }
        });

        this.playerBoat.layoutXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                Number newValue) {
                repositionCamera();
            }
        });
        this.playerBoat.layoutYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                Number newValue) {
                repositionCamera();
            }
        });
    }

    private void repositionCamera() {
        transforms.clear();
        transforms.addAll(
            new Translate(playerBoat.getLayoutX(), playerBoat.getLayoutY(), 0),
            new Rotate(playerYacht.getHeading(), new Point3D(0, 0, 1)),
            new Rotate(60, new Point3D(1, 0, 0)),
            new Translate(0, 0, zoomFactor)
        );
    }

    private void repositionCamera(Double newHeading) {
        transforms.clear();
        transforms.addAll(
            new Translate(playerBoat.getLayoutX(), playerBoat.getLayoutY(), 0),
            new Rotate(newHeading, new Point3D(0, 0, 1)),
            new Rotate(60, new Point3D(1, 0, 0)),
            new Translate(0, 0, zoomFactor)
        );
    }

    @Override
    public void zoomIn() {
        //transforms.addAll(new Translate(0, 0, 1.5));
        this.zoomFactor += 5;
        repositionCamera();
    }

    @Override
    public void zoomOut() {
        //transforms.addAll(new Translate(0, 0, -1.5));
        this.zoomFactor -= 5;
        repositionCamera();
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
