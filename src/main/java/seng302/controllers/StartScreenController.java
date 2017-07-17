package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import seng302.client.ClientTransmitterThread;
import seng302.gameServer.GameServerThread;
import seng302.gameServer.GameState;
import seng302.gameServerWithThreading.MainServerThread;
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
            new MainServerThread().start();
//            new GameServerThread("Fuck you");
            // get the lobby controller so that we can pass the game server thread to it
            setContentPane("/views/LobbyView.fxml");

        } catch (UnknownHostException e) {
            System.err.println("COULD NOT FIND YOUR IP ADDRESS!");
            e.printStackTrace();
        }

    }


    @FXML
    public void connectButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function
        String ipAddress = ipTextField.getText().trim().toLowerCase();
        //startClientTransmitterThread();
        StreamReceiver sr = new StreamReceiver(ipAddress, 4950, "HostStream");
        sr.start();

    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void startClientTransmitterThread() {
        this.controller.setClientTransmitterThread(new ClientTransmitterThread("RaceVision Test Client Transmitter"));
    }
}
