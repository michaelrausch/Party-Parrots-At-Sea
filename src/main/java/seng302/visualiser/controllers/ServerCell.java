package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

public class ServerCell implements Initializable {

    @FXML
    private Label serverName;

    @FXML
    private GridPane serverListCell;

    @FXML
    private JFXButton serverConnButton;

    private String name;


    public ServerCell(String name) {
        this.name = name;
    }


    public void initialize(URL location, ResourceBundle resources) {
        serverName.setText(name);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(4.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.1));
        serverListCell.setEffect(dropShadow);

        DropShadow dropShadow2 = new DropShadow();
        dropShadow2.setRadius(10.0);
        dropShadow2.setOffsetX(5.0);
        dropShadow2.setOffsetY(6.0);
        dropShadow2.setColor(Color.color(0, 0, 0, 0.3));

        serverListCell.setOnMouseEntered(event -> {
            serverListCell.setEffect(dropShadow2);
        });

        serverListCell.setOnMouseExited(event -> {
            serverListCell.setEffect(dropShadow);
        });
    }

    public void createServer() {
        JFXDecorator decorator = (JFXDecorator) serverConnButton.getScene().getRoot();
        System.out.println("Connecting to " + serverName.getText());
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            Parent root = FXMLLoader.load(StartScreenController.class.getResource("/views/LobbyView.fxml"));
            decorator.setContent(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
