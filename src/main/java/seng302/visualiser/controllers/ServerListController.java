package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.ServerDescription;
import seng302.visualiser.ServerListener;
import seng302.visualiser.ServerListenerDelegate;
import seng302.visualiser.controllers.cells.ServerCell;

public class ServerListController implements Initializable, ServerListenerDelegate {

    //--------FXML BEGIN--------//
    // Layout Related
    @FXML
    private VBox serverListVBox;
    @FXML
    private ScrollPane serverListScrollPane;
    @FXML
    private StackPane serverListMainStackPane;

    // Host Button
    @FXML
    private JFXButton serverListHostButton;

    //Direct Connect
    @FXML
    private JFXButton connectButton;
    @FXML
    private JFXTextField serverHostName;
    @FXML
    private JFXTextField serverPortNumber;
    //---------FXML END---------//

    private Logger logger = LoggerFactory.getLogger(ServerListController.class);

    public void initialize(URL location, ResourceBundle resources) {

        serverListVBox.minWidthProperty().bind(serverListScrollPane.widthProperty());

        connectButton.setOnMouseReleased(event -> goToDirectConnectLobby());

        try {
            ServerListener.getInstance().setDelegate(this);
        } catch (IOException e) {
            logger.warn("Could not start Server Listener Delegate");
        }

        Platform.runLater(() -> {
            FXMLLoader dialogContent = new FXMLLoader(getClass().getResource(
                "/views/dialogs/ServerCreationDialog.fxml"));

            try {
                JFXDialog dialog = new JFXDialog(serverListMainStackPane, dialogContent.load(),
                    DialogTransition.CENTER);
                serverListHostButton.setOnAction(action -> dialog.show());
            } catch (IOException e) {
                logger.warn("Could not create Server Creation Dialog.");
            }
        });
    }

    private void goToDirectConnectLobby() {
        // TODO: 7/09/17 Error handling
        ViewManager.getInstance().getGameClient().runAsClient(serverHostName.getText(), Integer.parseInt(serverPortNumber.getText()));
    }

    @Override
    public void serverRemoved(List<ServerDescription> servers) {
        Platform.runLater(() -> refreshServers(servers));
    }

    @Override
    public void serverDetected(ServerDescription serverDescription, List<ServerDescription> servers) {
        Platform.runLater(() -> refreshServers(servers));
    }

    private void refreshServers(List<ServerDescription> servers) {
        // TODO: 7/09/17 ajm412: Add some way to force a refresh.
        // TODO: 7/09/17 ajm412: Add something for No Servers Found.
        serverListVBox.getChildren().clear();

        // Populate the server list with a series of server cell objects.
        for (ServerDescription server : servers){
            VBox pane = null;

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cells/ServerCell.fxml"));

            loader.setController(new ServerCell(server));

            try {
                pane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            serverListVBox.getChildren().add(pane);
        }

    }
}
