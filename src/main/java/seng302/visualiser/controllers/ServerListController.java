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

public class ServerListController implements Initializable {

    @FXML
    private VBox serverListVBox;
    @FXML
    private ScrollPane serverListScrollPane;
    @FXML
    private StackPane serverListMainStackPane;
    @FXML
    private JFXButton serverListHostButton;

    private void refreshServerList(){

    }

    public void initialize(URL location, ResourceBundle resources) {

        serverListVBox.minWidthProperty().bind(serverListScrollPane.widthProperty());

//        for (int i = 0; i < 20; i++) {
//            VBox pane = null;
//
//            FXMLLoader loader = new FXMLLoader(
//                getClass().getResource("/views/cells/ServerCell.fxml"));
//
//            loader.setController(new ServerCell("Server " + i));
//
//            try {
//                pane = loader.load();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            serverListVBox.getChildren().add(pane);
//        }

        Platform.runLater(() -> {
            FXMLLoader dialogContent = new FXMLLoader(getClass().getResource(
                "/views/dialogs/ServerCreationDialog.fxml"));

            try {
                JFXDialog dialog = new JFXDialog(serverListMainStackPane, dialogContent.load(),
                    DialogTransition.CENTER);
                serverListHostButton.setOnAction(action -> dialog.show());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
