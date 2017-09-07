package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import seng302.gameServer.ServerDescription;

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

    private Integer currPlayerCount;

    private String hostName;
    private Integer portNumber;


    public ServerCell(ServerDescription server) {
        this.name = server.getName();
        this.currPlayerCount = server.spacesLeft();
        this.mapNameString = server.getMapName();

        this.hostName = server.getAddress();
        this.portNumber = server.portNumber();
    }


    public void initialize(URL location, ResourceBundle resources) {
        serverName.setText(name);
        serverPlayerCount.setText(Integer.toString(currPlayerCount));

        serverConnButton.setOnMouseReleased(event -> joinServer());
    }

    public void joinServer() {
        // TODO: 7/09/17 ajm412: Connect to a server here with the values stored in the hostName/portNumber variables.
        System.out.println("Connecting to " + serverName.getText());
        try {
            Parent root = FXMLLoader.load(StartScreenController.class.getResource("/views/LobbyView.fxml"));
            ViewManager.getInstance().setScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
