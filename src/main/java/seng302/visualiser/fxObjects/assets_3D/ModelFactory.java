package seng302.visualiser.fxObjects.assets_3D;

import com.interactivemesh.jfx.importer.col.ColModelImporter;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

/**
 * Factory class for creating 3D models of boats.
 */
public class ModelFactory {

    public static BoatModel boatIconView(BoatMeshType boatType, Color primaryColour) {
        Group boatAssets = getUnmodifiedBoatModel(boatType, primaryColour);
        boatAssets.getTransforms().addAll(
            new Scale(20, 20, 20),
            new Rotate(90, new Point3D(0,0,1)),
            new Rotate(90, new Point3D(0, 1, 0))
        );
        boatAssets.getChildren().add(new AmbientLight(new Color(1, 1, 1, 0.01)));
        return new BoatModel(boatAssets, null, boatType);
    }

    public static BoatModel boatRotatingView(BoatMeshType boatType, Color primaryColour) {
        Group boatAssets = getUnmodifiedBoatModel(boatType, primaryColour);
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

    public static BoatModel boatGameView(BoatMeshType boatType, Color primaryColour) {
        Group boatAssets = getUnmodifiedBoatModel(boatType, primaryColour);
        boatAssets.getTransforms().setAll(
            new Rotate(-90, new Point3D(0,0,1)),
            new Scale(0.2, 0.2, 0.2)
        );
        return new BoatModel(boatAssets, null, boatType);
    }

    private static Group getUnmodifiedBoatModel(BoatMeshType boatType, Color primaryColour) {
        Group boatAssets = new Group();
        MeshView hull = importFile(boatType.hullFile);
        hull.setMaterial(new PhongMaterial(primaryColour));
        MeshView mast = importFile(boatType.mastFile);
        mast.setMaterial(new PhongMaterial(primaryColour));
        MeshView sail = importFile(boatType.sailFile);
        sail.setMaterial(new PhongMaterial(Color.WHITE));
        boatAssets.getChildren().addAll(hull, mast, sail);
        return boatAssets;
    }

    private static MeshView importFile(String fileName) {
        StlMeshImporter importer = new StlMeshImporter();
        importer.read(ModelFactory.class.getResource("/meshes/" + fileName));
        return new MeshView(importer.getImport());
    }

    public static Model importModel(ModelType tokenType) {
        Group assets;
        if (tokenType.filename == null) {
            assets = new Group();
        } else {
            ColModelImporter importer = new ColModelImporter();
            importer.read(ModelFactory.class.getResource("/meshes/" + tokenType.filename));
            assets = new Group(importer.getImport());
        }
        switch (tokenType) {
            case VELOCITY_COIN:
                return makeCoinPickup(assets);
            case FINISH_MARKER:
            case PLAIN_MARKER:
            case START_MARKER:
                return makeMarker(assets);
            case OCEAN:
                return makeOcean(assets);
            default:
                return new Model(assets, null);
        }
    }

    private static Model makeCoinPickup(Group assets){
        assets.setRotationAxis(new Point3D(1,0,0));
        assets.setRotate(90);
        assets.setTranslateX(0.2);
        assets.setTranslateY(1);
        assets.getTransforms().add(new Rotate(0 ,new Point3D(0,1,0)));
        return new Model(assets, new AnimationTimer() {

            private double rotation = 0;
            private Group group = assets;

            @Override
            public void handle(long now) {
                rotation += 0.5;
                ((Rotate) group.getTransforms().get(0)).setAngle(rotation);
            }
        });
    }

    private static Model makeMarker(Group marker) {
        ColModelImporter importer = new ColModelImporter();
        importer.read(ModelFactory.class.getResource("/meshes/" + ModelType.MARK_AREA.filename));
        Group area = new Group(importer.getImport());
        area.getChildren().add(marker);
        area.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        return new Model(area, null);
    }

    private static Model makeOcean(Group group) {
//        group.setScaleY(Double.MAX_VALUE);
//        group.setScaleX(Double.MAX_VALUE);
//        group.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        Circle ocean = new Circle(0,0,1000, Color.DEEPSKYBLUE);
        ocean.setStroke(Color.TRANSPARENT);
        group.getChildren().add(ocean);
        return new Model(group, null);
    }
}
