package seng302.visualiser.controllers.cells;

import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import seng302.gameServer.ServerDescription;
import seng302.utilities.Sounds;
import seng302.visualiser.controllers.ViewManager;

public class ServerCell implements Initializable {

    //--------FXML BEGIN--------//
    //Layout
    @FXML
    private VBox serverCellVBox;
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

        serverCellVBox.setOnMouseEntered(event -> Sounds.playHoverSound());

        serverConnButton.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            joinServer();
        });

    }

    /**
     * Attempts to connect to the chosen server using the button on the serverCell.
     */
    private void joinServer() {
        System.out.println("Connecting to " + serverName.getText());
        ViewManager.getInstance().getGameClient().runAsClient(hostName, portNumber);
    }

}
