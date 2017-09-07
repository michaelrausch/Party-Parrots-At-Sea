package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;

public class LobbyController implements Initializable {

    @FXML
    private VBox playerListVBox;

    @FXML
    private ScrollPane playerListScrollpane;

    @FXML
    private JFXButton customizeButton, leaveLobbyButton;

    @FXML
    private StackPane serverListMainStackPane;

    @FXML
    private Label serverName;

    @FXML
    private Label mapName;

    private List<LobbyController_old.LobbyCloseListener> lobbyListeners = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        leaveLobbyButton.setOnMouseReleased(event -> leaveLobby());

        Platform.runLater(() -> {

            serverName.setText(ViewManager.getInstance().getProperty("serverName"));
            mapName.setText(ViewManager.getInstance().getProperty("mapName"));

            ViewManager.getInstance().getPlayerList().addListener((ListChangeListener<String>) c -> {
                Platform.runLater(this::refreshPlayerList);
            });

            ViewManager.getInstance().getPlayerList().setAll(ViewManager.getInstance().getPlayerList().sorted());
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

    private void refreshPlayerList() {
        playerListVBox.getChildren().clear();

        for (String player : ViewManager.getInstance().getPlayerList()) {
            VBox pane = null;

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cells/PlayerCell.fxml"));

            loader.setController(new PlayerCell(player));

            try {
                pane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            playerListVBox.getChildren().add(pane);
        }
    }

    public void leaveLobby() {
        // TODO: 10/07/17 wmu16 - Finish function!
        GameState.setCurrentStage(GameStages.CANCELLED);
//        for (LobbyController_old.LobbyCloseListener readyListener : lobbyListeners)
//            readyListener.notify(LobbyController_old.CloseStatus.LEAVE);

        //TODO close threads and figure out what the above lines do;
        ViewManager.getInstance().goToStartView();
    }
}
