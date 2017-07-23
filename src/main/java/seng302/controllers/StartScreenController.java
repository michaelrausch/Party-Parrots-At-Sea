package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import seng302.client.ClientState;
import seng302.client.ClientToServerThread;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;

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
    private TextField portTextField;
    @FXML
    private GridPane startScreen2;

    private Controller controller;

    /**
     * Loads the fxml content into the parent pane
     * @param jfxUrl
     * @return the controller of the fxml
     */
    private Object setContentPane(String jfxUrl) {
        try {
            AnchorPane contentPane = (AnchorPane) startScreen2.getParent();
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(jfxUrl));
            contentPane.getChildren().addAll((Pane) fxmlLoader.load());

            return fxmlLoader.getController();
        } catch (javafx.fxml.LoadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
            new MainServerThread();
            ClientState.setHost(true);
            // get the lobby controller so that we can pass the game server thread to it
            setContentPane("/views/LobbyView.fxml");
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Cannot host");
            alert.setContentText("Oops, failed to host, try to restart.");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    public void connectButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function
        try {
            String ipAddress = ipTextField.getText().trim().toLowerCase();
            Integer port = Integer.valueOf(portTextField.getText().trim());

            ClientToServerThread clientToServerThread = new ClientToServerThread(ipAddress, port);
            controller.setClientToServerThread(clientToServerThread);
            ClientState.setHost(false);
            ClientState.setConnectedToHost(true);
            setContentPane("/views/LobbyView.fxml");
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Cannot reach the host");
            alert.setContentText("Please check your host IP address.");
            alert.showAndWait();
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
