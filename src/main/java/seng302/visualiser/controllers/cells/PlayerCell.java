package seng302.visualiser.controllers.cells;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import seng302.utilities.Sounds;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;
import seng302.visualiser.fxObjects.assets_3D.BoatModel;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;

public class PlayerCell {

    //--------FXML BEGIN--------//
    @FXML
    private Label playerName;
    @FXML
    private GridPane playerListCell;
    @FXML
    private Pane boatPane;
    //---------FXML END---------//

    private String name;
    private Integer boatAngle;
    private Color boatColor;
    private Integer playerId;

    public PlayerCell(Integer playerId, String playerName, Color color) {
        this.playerId = playerId;
        this.name = playerName;
        this.boatColor = color;
        this.boatAngle = -45;
    }

    public void initialize() {
        playerName.setText(name);

        Group group = new Group();
        boatPane.getChildren().add(group);

        BoatModel bo = ModelFactory.boatIconView(BoatMeshType.DINGHY, this.boatColor);
        bo.showSail();
        bo.rotateSail(45);
        bo.getAssets().getTransforms().setAll(
            new Scale(4, 4, 4),
            new Translate(12, 14, 0),
            new Rotate(270, new Point3D(1, 0, 0)),
            new Rotate(-45, new Point3D(0, 0, 1))
        );

        bo.setAnimation(new AnimationTimer() {
            Group group = bo.getAssets();

            @Override
            public void handle(long now) {
                ((Rotate) group.getTransforms().get(3)).setAngle(boatAngle++);
            }
        });

        group.getChildren().add(bo.getAssets());

    }

    public Integer getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return name;
    }

    public Color getBoatColor() {
        return boatColor;
    }

    public void playButtonHoverSound(MouseEvent mouseEvent) {
        Sounds.playHoverSound();
    }
}