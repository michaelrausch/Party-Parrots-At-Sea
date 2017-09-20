package seng302.visualiser.fxObjects.assets_3D;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

/**
 * BoatGroup is a javafx group that by default contains a graphical objects for representing a 2
 * dimensional boat. It contains a single polygon for the boat, a group of lines to show it's path,
 * a wake object and two text labels to annotate the boat teams name and the boats velocity. The
 * boat will update it's position onscreen everytime UpdatePosition is called unless the window is
 * minimized in which case it attempts to store animations and apply them when the window is
 * maximised.
 */
public class BoatObject extends Group {

    @FunctionalInterface
    public interface SelectedBoatListener {

        void notifySelected(BoatObject boatObject, Boolean isSelected);
    }

    private BoatModel boatAssets;
    private Group wake;
    private Color colour = Color.BLACK;
    private Boolean isSelected = false;
    private Rotate rotation = new Rotate(0, new Point3D(0,0,1));

    private List<SelectedBoatListener> selectedBoatListenerListeners = new ArrayList<>();

    /**
     * Creates a BoatGroup with the default triangular boat polygon.
     */
    public BoatObject() {
        boatAssets = ModelFactory.boatGameView(BoatMeshType.CATAMARAN, colour);
        boatAssets.hideSail();
        boatAssets.getAssets().getTransforms().addAll(
            rotation
        );
        boatAssets.getAssets().setOnMouseClicked(event -> {
            setIsSelected(!isSelected);
            updateListeners();
        });
        boatAssets.getAssets().setCache(true);
        wake = ModelFactory.importModel(ModelType.WAKE).getAssets();
        super.getChildren().addAll(boatAssets.getAssets());
    }

    public void setFill (Color value) {
        this.colour = value;
        boatAssets.changeColour(colour);
    }


    /**
     * Moves the boat and its children annotations to coordinates specified
     *  @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     * @param rotation The rotation by which the boat moves
     * @param velocity The velocity the boat is moving
     * @param sailIn Boolean to toggle sail state.
     * @param windDir .
     */
    public void moveTo(double x, double y, double rotation, double velocity, Boolean sailIn, double windDir) {
        Platform.runLater(() -> {
            rotateTo(rotation, sailIn, windDir);
            this.layoutXProperty().setValue(x);
            this.layoutYProperty().setValue(y);
            wake.setLayoutX(x);
            wake.setLayoutY(y);
        });
    }

    private Double normalizeHeading(double heading, double windDirection) {
        Double normalizedHeading = heading - windDirection;
        normalizedHeading = (double) Math.floorMod(normalizedHeading.longValue(), 360L);
        return normalizedHeading;
    }


    private void rotateTo(double heading, boolean sailsIn, double windDir) {
        rotation.setAngle(heading);
        wake.getTransforms().setAll(new Rotate(heading, new Point3D(0,0,1)));
        if (sailsIn) {
            boatAssets.showSail();
            Double sailWindOffset = 30.0;
            Double upwindAngleLimit = 15.0;
            Double downwindAngleLimit = 10.0; //Upwind from normalised horizontal
            Double normalizedHeading = normalizeHeading(heading, windDir);
            if (normalizedHeading < 180) {
                if (normalizedHeading < sailWindOffset + upwindAngleLimit){
                    boatAssets.rotateSail(-upwindAngleLimit);
                } else if (normalizedHeading > 90 + sailWindOffset){
                    boatAssets.rotateSail(-90 + downwindAngleLimit);
                } else {
                    boatAssets.rotateSail(-heading + windDir + sailWindOffset);
                }
            } else {
                if (normalizedHeading > 360 - (sailWindOffset + upwindAngleLimit)) {
                    boatAssets.rotateSail(upwindAngleLimit);
                } else if (normalizedHeading < 270 - sailWindOffset) {
                    boatAssets.rotateSail(90 - downwindAngleLimit);
                } else {
                    boatAssets.rotateSail(-heading + windDir - sailWindOffset);
                }
            }
        } else {
            boatAssets.hideSail();
        }
    }

    public Group getWake () {
        return wake;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    private void updateListeners() {
        for (SelectedBoatListener sbl : selectedBoatListenerListeners) {
            sbl.notifySelected(this, this.isSelected);
        }
    }

    public void addSelectedBoatListener(SelectedBoatListener sbl) {
        selectedBoatListenerListeners.add(sbl);
    }
}