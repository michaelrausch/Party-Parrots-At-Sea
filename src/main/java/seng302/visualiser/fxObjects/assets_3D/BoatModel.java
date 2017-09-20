package seng302.visualiser.fxObjects.assets_3D;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

/**
 * Container class for a group of 3d objects representing a boat and it's animation.
 */
public class BoatModel extends Model {

    private static final int HULL_INDEX = 0;
    private static final int MAST_INDEX = 1;
    private static final int SAIL_INDEX = 2;

    private BoatMeshType meshType;

    /**
     * Stores a model and it's optional animation.
     * @param boatAssets The group with 3d assets for the boat.
     * @param animation Animation, can be null.
     */
    BoatModel(Group boatAssets, AnimationTimer animation, BoatMeshType meshType) {
        super(boatAssets, animation);
        this.meshType = meshType;
    }

    /**
     * Rotates the sail of this model by the given amount.
     * @param degrees The rotation of the sail in degrees
     */
    public void rotateSail(double degrees) {
        if (!meshType.fixedSail) {
            MeshView mast = getMeshViewChild(MAST_INDEX);
            MeshView sail = getMeshViewChild(SAIL_INDEX);
            mast.getTransforms().setAll(
                new Rotate(degrees, 0, -meshType.mastOffset, 0, new Point3D(0, 0, 1))
            );
            sail.getTransforms().setAll(
                new Rotate(degrees, 0, -meshType.sailOffset,0, new Point3D(0, 0, 1))
            );
        }
    }

    public void hideSail() {
        getMeshViewChild(SAIL_INDEX).setVisible(false);
    }

    public void showSail() {
        getMeshViewChild(SAIL_INDEX).setVisible(true);
    }

    /**
     * Changes the colour of the model in this class.
     * @param newColour the new colour for the boat.
     */
    public void changeColour(Color newColour) {
        changeColourChild(HULL_INDEX, newColour);
        changeColourChild(MAST_INDEX, newColour);
    }

    private void changeColourChild(int index, Color newColour) {
        MeshView meshView = getMeshViewChild(index);
        meshView.setMaterial(new PhongMaterial(newColour));
    }

    private MeshView getMeshViewChild(int index) {
        return (MeshView) assets.getChildren().get(index);
    }
}