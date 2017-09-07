package seng302.visualiser.controllers;

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

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(4.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.1));
        playerListCell.setEffect(dropShadow);

        DropShadow dropShadow2 = new DropShadow();
        dropShadow2.setRadius(10.0);
        dropShadow2.setOffsetX(5.0);
        dropShadow2.setOffsetY(6.0);
        dropShadow2.setColor(Color.color(0, 0, 0, 0.3));

        playerListCell.setOnMouseEntered(event -> {
            playerListCell.setEffect(dropShadow2);
        });

        playerListCell.setOnMouseExited(event -> {
            playerListCell.setEffect(dropShadow);
        });

    }
}
