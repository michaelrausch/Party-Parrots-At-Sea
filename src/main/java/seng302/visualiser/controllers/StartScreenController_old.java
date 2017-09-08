package seng302.visualiser.controllers;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import seng302.gameServer.GameState;
import seng302.visualiser.GameClient;

/**
 * A Class describing the actions of the start screen controller
 * Created by wmu16 on 10/07/17.
 */
public class StartScreenController_old implements Initializable {

    @FXML
    private TextField ipTextField;

    @FXML
    private AnchorPane holder;

    GameClient gameClient;

    public void initialize(URL url,  ResourceBundle resourceBundle) {
    }

    /**
     * ATTEMPTS TO:
     * Sets up a new game state with your IP address as designated as the host.
     * Starts a thread to listen for incoming connections.
     * Starts a client to server thread and connects to own ip.
     * Switches to the lobby screen
     */
    @FXML
    public void hostButtonPressed() {
        gameClient = new GameClient(holder);
        //gameClient.runAsHost(getLocalHostIp(), 4942);
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
//        ClientState.setHostIp(ipAddress);
        return ipAddress;
    }
}
