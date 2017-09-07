package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LobbyController implements Initializable {

    @FXML
    private VBox playerListVBox;

    @FXML
    private ScrollPane playerListScrollpane;

    @FXML
    private JFXButton customizeButton;

    @FXML
    private StackPane serverListMainStackPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            Integer max = 6;
            for (int i = 0; i < max; i++) {
                VBox pane = null;

                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cells/PlayerCell.fxml"));

                loader.setController(new PlayerCell("Player " + i));

                try {
                    pane = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                playerListVBox.getChildren().add(pane);
            }
        });


        Platform.runLater(() -> {
            FXMLLoader dialogContent = new FXMLLoader(getClass().getResource(
                "/views/dialogs/BoatCustomizeDialog.fxml"));

            try {
                JFXDialog dialog = new JFXDialog(serverListMainStackPane, dialogContent.load(),
                    DialogTransition.CENTER);
                customizeButton.setOnAction(action -> dialog.show());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }
}
