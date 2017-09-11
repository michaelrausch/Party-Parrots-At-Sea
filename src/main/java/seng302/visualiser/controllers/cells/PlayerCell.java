package seng302.visualiser.controllers.cells;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import seng302.utilities.Sounds;

public class PlayerCell {

    //--------FXML BEGIN--------//
    @FXML
    private Label playerName;
    @FXML
    private GridPane playerListCell;
    //---------FXML END---------//

    private String name;

    public PlayerCell(String playerName) {
        this.name = playerName;
    }

    public void initialize() {
        playerName.setText(name);
    }

    public void playButtonHoverSound(MouseEvent mouseEvent) {
        Sounds.playHoverSound();
    }
}
