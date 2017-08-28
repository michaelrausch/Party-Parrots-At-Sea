package seng302.visualiser.controllers;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import seng302.gameServer.GameState;
import seng302.utilities.Sounds;
import seng302.visualiser.GameClient;

/**
 * A Class describing the actions of the start screen controller
 * Created by wmu16 on 10/07/17.
 */
public class StartScreenController implements Initializable {

    @FXML
    private ToggleButton muteMusicButton;
    @FXML
    private ToggleButton muteSoundsButton;
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private GridPane startScreen2;
    @FXML
    private AnchorPane holder;

    GameClient gameClient;

    public void initialize(URL url,  ResourceBundle resourceBundle) {
        if (Sounds.isMusicMuted()) {
            muteMusicButton.setText("UnMute Music");
        } else {
            muteMusicButton.setText("Mute Music");
        }
        if (Sounds.isSoundEffectsMuted()) {
            muteSoundsButton.setText("UnMute Sounds");
        } else {
            muteSoundsButton.setText("Mute Sounds");
        }

//        gameClient = new GameClient(holder);
    }
//
//    /**
//     * Loads the fxml content into the parent pane
//     * @param jfxUrl
//     * @return the controller of the fxml
//     */
//    private Object setContentPane(String jfxUrl) {
//        try {
//            AnchorPane contentPane = (AnchorPane) startScreen2.getParent();
//            contentPane.getChildren().removeAll();
//            contentPane.getChildren().clear();
//            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
//            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(jfxUrl));
//            contentPane.getChildren().addAll((Pane) fxmlLoader.load());
//
//            return fxmlLoader.getController();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    /**
     * ATTEMPTS TO:
     * Sets up a new game state with your IP address as designated as the host.
     * Starts a thread to listen for incoming connections.
     * Starts a client to server thread and connects to own ip.
     * Switches to the lobby screen
     */
    @FXML
    public void hostButtonPressed() {
        Sounds.playButtonClick();
//        new GameState(getLocalHostIp());
        gameClient = new GameClient(holder);
        gameClient.runAsHost(getLocalHostIp(), 4942);
//        try {
////            String ipAddress = InetAddress.getLocalHost().getHostAddress();
////            new GameState(ipAddress);
////            new MainServerThread();
////            ClientToServerThread clientToServerThread = new ClientToServerThread("localhost", 4950);
////            controller.setClientToServerThread(clientToServerThread);
//            // get the lobby controller so that we can pass the game server thread to it
//            new GameState(getLocalHostIp());
//            MainServerThread mainServerThread = new MainServerThread();
////            ClientState.setHost(true);
//            // host will connect and handshake to itself after setting up the server
//            // TODO: 24/07/17 wmu16 - Make port number some static global type constant?
////            ClientToServerThread clientToServerThread = new ClientToServerThread(ClientState.getHostIp(), 4942);
////            ClientState.setConnectedToHost(true);
////            controller.setClientToServerThread(clientToServerThread);
//            LobbyController lobbyController = (LobbyController) setContentPane("/views/LobbyView.fxml");
//            lobbyController.setMainServerThread(mainServerThread);
//        } catch (Exception e) {
//            Alert alert = new Alert(AlertType.ERROR);
//            alert.setHeaderText("Cannot host");
//            alert.setContentText("Oops, failed to host, try to restart.");
//            alert.showAndWait();
//            e.printStackTrace();
//        }
    }

    /**
     * ATTEMPTS TO:
     * Connect to an ip address and port using the ip and port specified on start screen.
     * Starts a Client To Server Thread to maintain connection to host.
     * Switch view to lobby view.
     */
    @FXML
    public void connectButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function
        Sounds.playButtonClick();
        gameClient = new GameClient(holder);
        gameClient.runAsClient(ipTextField.getText().trim().toLowerCase(), 4942);

//        try {
//            String ipAddress = ipTextField.getText().trim().toLowerCase();
//            Integer port = Integer.valueOf(portTextField.getText().trim());
//
////            ClientToServerThread clientToServerThread = new ClientToServerThread(ipAddress, port);
////            ClientState.setHost(false);
////            ClientState.setConnectedToHost(true);
//
////            controller.setClientToServerThread(clientToServerThread);
////            setContentPane("/views/LobbyView.fxml");
//        } catch (Exception e) {
//            Alert alert = new Alert(AlertType.ERROR);
//            alert.setHeaderText("Cannot reach the host");
//            alert.setContentText("Please check your host IP address.");
//            alert.showAndWait();
//        }
    }

//    public void setController(Controller controller) {
//        this.controller = controller;
//    }

    /**
     * Gets the local host ip address and sets this ip to ClientState.
     * Only runs by the host.
     *
     * @return the localhost ip address
     */
    private String getLocalHostIp() {
        String ipAddress = null;
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                if (ni.isLoopback())
                    continue;
                if(ni.isPointToPoint())
                    continue;
                if(ni.isVirtual())
                    continue;

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if(address instanceof Inet4Address) {    // skip all ipv6
                        ipAddress = address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ipAddress == null) {
            System.out.println("[HOST] Cannot obtain local host ip address.");
        }
//        ClientState.setHostIp(ipAddress);
        return ipAddress;
    }

    public void toggleMusic(ActionEvent actionEvent) {
        Sounds.toggleMuteMusic();
        Sounds.playButtonClick();
        if (Sounds.isMusicMuted()) {
            muteMusicButton.setText("UnMute Music");
        } else {
            muteMusicButton.setText("Mute Music");
        }
    }

    public void toggleSounds(ActionEvent actionEvent) {
        Sounds.toggleMuteEffects();
        Sounds.playButtonClick();
        if (Sounds.isSoundEffectsMuted()) {
            muteSoundsButton.setText("UnMute Sounds");
        } else {
            muteSoundsButton.setText("Mute Sounds");
        }
    }
}
