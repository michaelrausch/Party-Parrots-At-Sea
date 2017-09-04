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
import seng302.visualiser.GameClient;

/**
 * A Class describing the actions of the start screen controller
 * Created by wmu16 on 10/07/17.
 */
public class StartScreenController implements Initializable {

    @FXML
    private TextField ipTextField;
    @FXML
    private AnchorPane holder;

    private GameClient gameClient;

    public void initialize(URL url,  ResourceBundle resourceBundle) {

    }

    /**
     * Creates an instance of GameClient and runs it as a host.
     */
    @FXML
    public void hostButtonPressed() {
        gameClient = new GameClient(holder);
        gameClient.runAsHost(getLocalHostIp(), 4942);
    }

    /**
     * Creates an instance of GameClient and runs it has a client.
     */
    @FXML
    public void connectButtonPressed() {
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
}
