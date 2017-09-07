package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import seng302.gameServer.ServerAdvertiser;
import seng302.gameServer.ServerDescription;
import seng302.visualiser.GameClient;
import seng302.visualiser.ServerListener;
import seng302.visualiser.ServerListenerDelegate;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

public class StartScreenController implements Initializable{

    @FXML
    private Label headText;

    @FXML
    private JFXButton startBtn;

    private Node serverList;

    private Logger logger = LoggerFactory.getLogger(StartScreenController.class);

    private List<ServerDescription> servers;

    private void setInitialDropShadow(){
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(4.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.5));
        headText.setEffect(dropShadow);

    }

    private void preloadServerListView(){
        try {
            serverList = FXMLLoader
                    .load(StartScreenController.class.getResource("/views/ServerListView.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not preload server list view");
        }
    }

    private void goToServerBrowser() {
        try {
            ViewManager.getInstance().setScene(serverList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        startBtn.setOnMousePressed(event -> startBtn.setText("LOADING..."));
        startBtn.setOnMouseReleased(event -> goToServerBrowser());

        setInitialDropShadow();
        preloadServerListView();


    }

}
