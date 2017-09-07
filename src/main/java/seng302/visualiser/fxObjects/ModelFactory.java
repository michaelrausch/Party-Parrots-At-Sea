package seng302.visualiser.fxObjects;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import seng302.visualiser.test3d;

/**
 * Factory class for creating 3D models of boats.
 */
public class ModelFactory {

    /**
     * Enum for boat meshes. Enum values should be of the form :
     * ENUM_VALUE (hull file, mast file, y offset of mast CoR from origin, sail file, y offset of sail CoR from origin)
     */
    public enum BoatMesh {

        DINGHY ("dinghy_hull.stl", "dinghy_mast.stl", 0, "dinghy_sail.stl", -1.36653);

        private final String hullFile, mastFile, sailFile;
        private final double mastOffset, sailOffset;

        BoatMesh (String hullFile, String mastFile, double mastOffset, String sailFile, double sailOffset) {
            this.hullFile = hullFile;
            this.mastFile = mastFile;
            this.mastOffset = mastOffset;
            this.sailFile = sailFile;
            this.sailOffset = sailOffset;
        }
    }

    /**
     * Container class for a group of 3d objects representing a boat and it's animation.
     */
    public class BoatModel {

        private AnimationTimer animationTimer;
        private Group assets;
        private BoatMesh meshType;

        /**
         * Stores a model and it's optional animation.
         * @param boatAssets The group with 3d assets for the boat.
         * @param animation Animation, can be null.
         */
        private BoatModel(Group boatAssets, AnimationTimer animation, BoatMesh meshType) {
            this.assets = boatAssets;
            this.animationTimer = animation;
            this.meshType = meshType;
            if (animation != null) {
                animation.start();
            }
        }

        /**
         * Rotates the sail of this model by the given amount.
         * @param degrees The rotation of the sail in degrees
         */
        public void RotateSail(double degrees) {
            MeshView mast = (MeshView) assets.getChildren().get(1);
            MeshView sail = (MeshView) assets.getChildren().get(2);
            mast.getTransforms().setAll(
                new Rotate(degrees, 0, meshType.mastOffset, 0, new Point3D(0, 0, 1))
            );
            sail.getTransforms().setAll(
                new Rotate(degrees, 0, meshType.sailOffset, 0, new Point3D(0, 0, 1))
            );
        }

        public Group getAssets() {
            return this.assets;
        }

        /**
         * Changes the colour of the model in this class.
         * @param newColour the new colour for the boat.
         */
        public void changeColour(Color newColour) {
            changeColourChild(0, newColour);
            changeColourChild(1, newColour);
        }

        private void changeColourChild(int index, Color newColour) {
            MeshView meshView = getMeshViewChild(index);
            meshView.setMaterial(new PhongMaterial(newColour));
        }

        private MeshView getMeshViewChild(int index) {
            return (MeshView) assets.getChildren().get(index);
        }

        private void setAnimation(AnimationTimer animation) {
            animationTimer = animation;
            if (animation != null) {
                animation.start();
            }
        }

        /**
         * Stops the animation of this model.
         */
        public void stopAnimation() {
            if (animationTimer != null) {
                animationTimer.stop();
                animationTimer = null;
            }
        }
    }

    public BoatModel getIconView(BoatMesh boatType, Color primaryColour) {
        Group boatAssets = new Group();
        MeshView hull = importFile(boatType.hullFile);
        hull.setMaterial(new PhongMaterial(primaryColour));
        MeshView mast = importFile(boatType.mastFile);
        mast.setMaterial(new PhongMaterial(primaryColour));
        MeshView sail = importFile(boatType.hullFile);
        sail.setMaterial(new PhongMaterial(Color.WHITE));
        boatAssets.getChildren().addAll(hull, mast, sail);
        boatAssets.getTransforms().addAll(
            new Scale(20, 20, 20),
            new Rotate(90, new Point3D(0,0,1)),
            new Rotate(90, new Point3D(0, 1, 0))
        );
        return new BoatModel(boatAssets, null, boatType);
    }

    public BoatModel getRotatingView(BoatMesh boatType, Color primaryColour) {
        Group boatAssets = new Group();
        MeshView hull = importFile(boatType.hullFile);
        hull.setMaterial(new PhongMaterial(primaryColour));
        MeshView mast = importFile(boatType.mastFile);
        mast.setMaterial(new PhongMaterial(primaryColour));
        MeshView sail = importFile(boatType.hullFile);
        sail.setMaterial(new PhongMaterial(Color.WHITE));
        boatAssets.getChildren().addAll(hull, mast, sail);
        boatAssets.getTransforms().addAll(
            new Scale(40, 40, 40),
            new Rotate(90, new Point3D(0,0,1)),
            new Rotate(90, new Point3D(0, 1, 0)),
            new Rotate(0, new Point3D(1,1,1))
        );
        // TODO: 7/09/17 This seems like it will never be garbage claimed. Might have to call BoatModel.stopAnimation();
        return new BoatModel(boatAssets, new AnimationTimer() {

            private double rotation = 0;
            private final Group group = boatAssets;

            @Override
            public void handle(long now) {
                rotation += 0.5;
                ((Rotate) group.getTransforms().get(3)).setAngle(rotation);
            }
        }, boatType);
    }

    public BoatModel getGameView(BoatMesh boatType, Color primaryColour) {
        BoatModel model = getIconView(boatType, primaryColour);
        model.getAssets().getTransforms().setAll(
            new Scale(20, 20, 20),
            new Rotate(90, new Point3D(0,0,1))
        );
        return model;
    }
    
    private MeshView importFile(String fileName) {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(test3d.class.getResource("/meshes/" + fileName).toString());
        return new MeshView(importer.getImport());
    }
}
