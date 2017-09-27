package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.ServerDescription;
import seng302.utilities.Sounds;
import seng302.visualiser.GameClient;

public class StartScreenController implements Initializable{

    //--------FXML BEGIN--------//
    @FXML
    private Label headText;
    @FXML
    private JFXButton startBtn;
    //---------FXML END---------//

    private Node serverList;
    private Logger logger = LoggerFactory.getLogger(StartScreenController.class);
    private List<ServerDescription> servers;
    private GameClient gameClient;

    public void initialize(URL location, ResourceBundle resources) {
        startBtn.setOnMousePressed(event -> {
            startBtn.setText("LOADING...");
            Sounds.playButtonClick();
        });

        startBtn.setOnMouseReleased(event -> goToServerBrowser());
        startBtn.setOnMouseEntered(event -> Sounds.playHoverSound());

        preloadServerListView();

    }

    /**
     * Preloads the server list view to reduce load time between start screen and server list screen.
     */
    private void preloadServerListView(){
        try {
            serverList = FXMLLoader
                    .load(StartScreenController.class.getResource("/views/ServerListView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not preload server list view");
        }
    }

    /**
     * Changes the view to the Server Browser.
     */
    public void goToServerBrowser() {
        try {
            ViewManager.getInstance().setScene(serverList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
