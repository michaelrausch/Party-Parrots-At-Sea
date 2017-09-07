package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXSlider;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ServerCreationController implements Initializable {

    @FXML
    private JFXButton submitBtn;

    @FXML
    private JFXSlider maxPlayers;

    @FXML
    private Label maxPlayersLabel;

    public ServerCreationController() {

    }


    public void initialize(URL location, ResourceBundle resources) {
        updateMaxPlayerLabel();
        maxPlayers.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateMaxPlayerLabel();
        });
    }

    public void createServer() {
        System.out.println(submitBtn.getScene().getRoot());
        JFXDecorator decorator = (JFXDecorator) submitBtn.getScene().getRoot();
        System.out.println(decorator.getChildren());
        StackPane stackPane = (StackPane) decorator.getChildren().get(1);

        FXMLLoader fxmlLoader = new FXMLLoader();

        try {
            Parent root = FXMLLoader.load(StartScreenController.class.getResource("/views/LobbyView.fxml"));

            ViewManager.getInstance().getGameClient().runAsHost("localhost", 4941);

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
