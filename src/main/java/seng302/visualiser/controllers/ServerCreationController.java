package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXSlider;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import seng302.gameServer.ServerDescription;

public class ServerCreationController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private JFXTextField serverName;

    @FXML
    private JFXSlider maxPlayers;
    @FXML
    private Label maxPlayersLabel;

    @FXML
    private JFXButton submitBtn;
    //---------FXML END---------//

    public void initialize(URL location, ResourceBundle resources) {
        updateMaxPlayerLabel();
        maxPlayers.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateMaxPlayerLabel();
        });

        submitBtn.setOnMouseReleased(event -> createServer());
    }

    public void createServer() {
        try {
            ServerDescription serverDescription = ViewManager.getInstance().getGameClient().runAsHost("localhost", 4941);

            ViewManager.getInstance().setProperty("serverName", serverDescription.getName());
            ViewManager.getInstance().setProperty("mapName", serverDescription.getMapName());

            Parent root = FXMLLoader.load(StartScreenController.class.getResource("/views/LobbyView.fxml"));

            ViewManager.getInstance().setScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMaxPlayerLabel() {
        maxPlayers.setValue(Math.floor(maxPlayers.getValue()));
        maxPlayersLabel.setText(Double.toString(maxPlayers.getValue()));
    }
}
