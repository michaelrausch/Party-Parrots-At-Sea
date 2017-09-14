package seng302.visualiser.fxObjects.assets_3D;

import com.interactivemesh.jfx.importer.col.ColModelImporter;
import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;



/**
 * Factory class for creating 3D models of boats.
 */
public class ModelFactory {

    public static BoatModel boatIconView(BoatMeshType boatType, Color primaryColour) {
        Group boatAssets = getUnmodifiedBoatModel(boatType, primaryColour);
        final Rotate animationRotate = new Rotate(0, new Point3D(0,0,1));
        boatAssets.getTransforms().addAll(
            new Scale(3.3, 3.3, 3.3),
            new Rotate(-70, new Point3D(1,0,0)),
            new Translate(13,50, 0),
            animationRotate
        );

        boatAssets.getTransforms().add(animationRotate);
        BoatModel bo = new BoatModel(boatAssets, null, boatType);
        bo.rotateSail(45);

        bo.setAnimation(new AnimationTimer() {
            double boatAngle = 0;
            Rotate rotate = animationRotate;
            @Override
            public void handle(long now) {
                boatAngle += 0.5;
                rotate.setAngle(boatAngle);
            }
        });
        boatAssets.getChildren().addAll(
            new AmbientLight()
        );
        return bo;
    }

    public static BoatModel boatRotatingView(BoatMeshType boatType, Color primaryColour) {
        Group boatAssets = getUnmodifiedBoatModel(boatType, primaryColour);
        boatAssets.getTransforms().addAll(
            new Scale(40, 40, 40),
            new Rotate(90, new Point3D(0,0,1)),
            new Rotate(90, new Point3D(0, 1, 0))
        );

        final Rotate animationRotate = new Rotate(0, new Point3D(1,1,1));
        boatAssets.getTransforms().add(animationRotate);

        return new BoatModel(boatAssets, new AnimationTimer() {

            private double rotation = 0;
            private Rotate rotate = animationRotate;

            @Override
            public void handle(long now) {
                rotation += 0.5;
                rotate.setAngle(rotation);
            }
        }, boatType);
    }

    public static BoatModel boatGameView(BoatMeshType boatType, Color primaryColour) {
        Group boatAssets = getUnmodifiedBoatModel(boatType, primaryColour);
        boatAssets.getTransforms().setAll(
            new Rotate(-90, new Point3D(0,0,1)),
            new Scale(0.3, 0.3, 0.3)
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
        MeshView importedFile = new MeshView(importer.getImport());
        importedFile.setCache(true);
        importedFile.setCacheHint(CacheHint.SCALE_AND_ROTATE);
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
            assets.setCache(true);
            assets.setCacheHint(CacheHint.SCALE_AND_ROTATE);
        }
        switch (tokenType) {
            case VELOCITY_PICKUP:
                return makeCoinPickup(assets);
            case FINISH_MARKER:
            case PLAIN_MARKER:
            case START_MARKER:
                return makeMarker(assets);
            case OCEAN:
                return makeOcean(assets);
            case BORDER_PYLON:
            case BORDER_BARRIER:
                return makeBarrier(assets);
            case FINISH_LINE:
            case START_LINE:
            case GATE_LINE:
                return makeGate(assets);
            case WAKE:
                return makeWake(assets);
            case TRAIL_SEGMENT:
                return makeTrail(assets);
            case PLAYER_IDENTIFIER:
                return makeIdentifierIcon(assets);
            case START_ARROW:
            case FINISH_ARROW:
            case PLAIN_ARROW:
                makeArrow(assets);
            default:
                return new Model(new Group(assets), null);
        }
    }

    private static Model makeCoinPickup(Group assets){
        assets.setRotationAxis(new Point3D(1,0,0));
        assets.setRotate(90);
        assets.setTranslateX(0.2);
        assets.setTranslateY(1);
        assets.getTransforms().addAll(
            new Translate(0,-1,0),
            new Rotate(0 ,new Point3D(1,1,1))
        );
        return new Model(new Group(assets), new AnimationTimer() {

            private double rotation = 0;
            private Group group = assets;

            @Override
            public void handle(long now) {
                rotation += 1;
                ((Rotate) group.getTransforms().get(1)).setAngle(rotation);
            }
        });
    }

    private static Model makeMarker(Group marker) {
        ColModelImporter importer = new ColModelImporter();
        importer.read(ModelFactory.class.getResource("/meshes/" + ModelType.MARK_AREA.filename));
        Group area = new Group(importer.getImport());
        area.getChildren().add(marker);
        area.getTransforms().add(new Rotate(90, new Point3D(1, 0, 0)));
        return new Model(new Group(area), null);
    }

    private static Model makeOcean(Group group) {
        Circle ocean = new Circle(
            0,0,250, Color.SKYBLUE
        );
        ocean.setStroke(Color.TRANSPARENT);
        group.getChildren().add(ocean);
        return new Model(new Group(group), null);
    }

    private static Model makeBarrier(Group assets) {
        assets.getTransforms().addAll(
            new Rotate(90, new Point3D(1,0,0))
        );
        return new Model(new Group(assets), null);
    }

    private static Model makeGate(Group assets) {
        assets.getTransforms().addAll(
            new Rotate(90, new Point3D(1,0,0))
        );
        return new Model(new Group(assets), null);
    }

    private static Model makeWake(Group assets) {
        assets.getTransforms().setAll(
            new Rotate(-90, new Point3D(0,0,1)),
            new Rotate(90, new Point3D(1,0,0)),
            new Scale(0.5, 0.5, 0.5)
        );
        return new Model(new Group(assets), null);
    }

    private static Model makeTrail(Group trailPiece) {
        trailPiece.getTransforms().addAll(
            new Rotate(-90, new Point3D(0,0,1)),
            new Rotate(90, new Point3D(1,0,0))
        );
        return new Model(new Group(trailPiece), null);
    }

    private static Model makeIdentifierIcon(Group assets) {
        assets.getTransforms().addAll(
            new Rotate(90, new Point3D(1,0,0)),
            new Scale(0.5, 0.5, 0.5)
        );
        return new Model(assets, null);
    }

    private static Model makeArrow(Group assets) {
        assets.getTransforms().addAll(
            new Rotate(90, new Point3D(1,0,0))
        );
        return new Model(new Group(assets), null);
    }
}
