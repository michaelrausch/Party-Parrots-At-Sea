package seng302.visualiser.controllers.cells;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import seng302.model.ClientYacht;
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
    private Color boatColor;
    private Integer playerId;
    private BoatMeshType boatType;

    public PlayerCell(Integer playerId, ClientYacht yacht) {
        this.playerId = playerId;
        this.name = yacht.getBoatName();
        this.boatColor = yacht.getColour();
        this.boatType = yacht.getBoatType();
    }

    public void initialize() {
        // Set Player Name
        playerName.setText(name);
        // Add Rotating Boat to Player Cell with players color on it.
        Group group = new Group();
        boatPane.getChildren().add(group);
        BoatModel bo = ModelFactory.boatIconView(boatType, boatColor);
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
}
