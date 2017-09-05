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
import javafx.scene.input.MouseEvent;
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
    private AnchorPane holder;

    private GameClient gameClient;

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

    /**
     * Creates an instance of GameClient and runs it as a host.
     */
    @FXML
    public void hostButtonPressed() {
        Sounds.playButtonClick();
        gameClient = new GameClient(holder);
        gameClient.runAsHost(getLocalHostIp(), 4942);
    }

    /**
     * Creates an instance of GameClient and runs it has a client.
     */
    @FXML
    public void connectButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function
        Sounds.playButtonClick();
        gameClient = new GameClient(holder);
        gameClient.runAsClient(ipTextField.getText().trim().toLowerCase(), 4942);
    }


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

    public void playButtonHoverSound(MouseEvent mouseEvent) {
        Sounds.playHoverSound();
    }
}
