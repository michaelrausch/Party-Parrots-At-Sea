package seng302.visualiser.controllers.cells;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;


public class PlayerCell {
    @FXML
    private Label playerName;

    @FXML
    private GridPane playerListCell;

    private String name;

    public PlayerCell(String playerName) {
        this.name = playerName;
    }

    public void initialize() {
        playerName.setText(name);
    }
}
