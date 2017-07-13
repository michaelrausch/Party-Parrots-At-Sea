package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import seng302.gameServer.GameServerThread;
import seng302.gameServer.GameState;
import seng302.models.stream.StreamReceiver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A Class describing the actions of the start screen controller
 * Created by wmu16 on 10/07/17.
 */
public class StartScreenController {

    @FXML
    private TextField ipTextField;
    @FXML
    private GridPane startScreen2;


    private void setContentPane(String jfxUrl) {
        try {
            AnchorPane contentPane = (AnchorPane) startScreen2.getParent();
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            contentPane.getChildren()
                    .addAll((Pane) FXMLLoader.load(getClass().getResource(jfxUrl)));
        } catch (javafx.fxml.LoadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * ATTEMPTS TO:
     * Sets up a new game state with your IP address as designated as the host.
     * Starts a thread to listen for incoming connections
     * Switches to the lobby screen
     */
    @FXML
    public void hostButtonPressed() {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            new GameState(ipAddress);
            new GameServerThread("Game Server");
            System.out.println("Server thread started");
            setContentPane("/views/LobbyView.fxml");
        } catch (UnknownHostException e) {
            System.err.println("COULD NOT FIND YOUR IP ADDRESS!");
            e.printStackTrace();
        }

    }


    @FXML
    public void connectButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function
        String ipAddress = ipTextField.getText().trim();
        StreamReceiver sr = new StreamReceiver(ipAddress, GameServerThread.PORT_NUMBER, "HostStream");
        sr.start();
    }
}
