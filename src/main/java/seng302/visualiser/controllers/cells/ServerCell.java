package seng302.visualiser.controllers.cells;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.sun.org.apache.bcel.internal.classfile.Unknown;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import seng302.gameServer.ServerDescription;
import seng302.utilities.Sounds;
import seng302.visualiser.GameClient;
import seng302.visualiser.controllers.ViewManager;

public class ServerCell implements Initializable {

    //--------FXML BEGIN--------//
    //Layout
    @FXML
    private GridPane serverListCell;
    //Server Information
    @FXML
    private Label serverName;
    @FXML
    private Label mapName;
    @FXML
    private Label serverPlayerCount;
    //Server Connection
    @FXML
    private JFXButton serverConnButton;
    //---------FXML END---------//

    private String name;
    private String mapNameString;

    private String currPlayerCount;

    private String hostName;
    private Integer portNumber;

    public ServerCell(ServerDescription server) {
        this.name = server.getName();

        this.currPlayerCount = server.getNumPlayers().toString() + "/" + server.getCapacity().toString();
        this.mapNameString = server.getMapName();

        // Can cause issues on windows PCs without the bonjour service installed.
        this.hostName = server.getAddress();
        this.portNumber = server.portNumber();
    }

    public void initialize(URL location, ResourceBundle resources) {
        serverName.setText(name);
        serverPlayerCount.setText(currPlayerCount);
        mapName.setText(mapNameString);

        serverConnButton.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            joinServer();
        });
    }

    /**
     *
     */
    private void joinServer() {
        System.out.println("Connecting to " + serverName.getText());
        ViewManager.getInstance().getGameClient().runAsClient(hostName, portNumber);
    }

    public void playButtonHoverSound(MouseEvent mouseEvent) {
        Sounds.playHoverSound();
    }
}
