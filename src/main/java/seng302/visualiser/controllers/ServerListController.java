package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.discoveryServer.DiscoveryServerClient;
import seng302.discoveryServer.util.ServerListing;
import seng302.gameServer.ServerDescription;
import seng302.gameServer.messages.ServerRegistrationMessage;
import seng302.utilities.Sounds;
import seng302.visualiser.ServerListener;
import seng302.visualiser.ServerListenerDelegate;
import seng302.visualiser.controllers.cells.ServerCell;
import seng302.visualiser.controllers.dialogs.DirectConnectController;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ServerListController implements Initializable, ServerListenerDelegate {

    //--------FXML BEGIN--------//
    // Layout Related
    @FXML
    private VBox serverListVBox;
    @FXML
    private ScrollPane serverListScrollPane;
    @FXML
    private StackPane serverListMainStackPane;
    // Host Button
    @FXML
    private JFXButton serverListHostButton;
    //Direct Connect
    @FXML
    private JFXButton directConnectButton;
    @FXML
    private JFXTextField serverPortNumber;
    @FXML
    private JFXButton roomConnectButton;
    @FXML
    private JFXTextField roomNumber;
    @FXML
    private JFXButton autoSelectGame;
    //---------FXML END---------//

    private Label noServersFound;
    private Logger logger = LoggerFactory.getLogger(ServerListController.class);
    private JFXDialog directConnectDialog;


    // TODO: 12/09/17 ajm412: break this method down, its way too long.
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serverListVBox.minWidthProperty().bind(serverListScrollPane.widthProperty());

        // Set Event Bindings
        directConnectButton.setOnMouseEntered(event -> Sounds.playHoverSound());
        serverListHostButton.setOnMouseEntered(event -> Sounds.playHoverSound());



        roomNumber.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                connectToRoomCode(roomNumber.getText());
            }
        });

        directConnectButton.setOnMouseReleased(event -> {
            directConnectDialog.show();
            Sounds.playButtonClick();
        });

        directConnectDialog = createDirectConnectDialog();

        for (JFXTextField textField : Arrays.asList(roomNumber)) {
            // Event for pressing enter to submit direct connection
            textField.setOnKeyPressed(event -> {
                if (event.getCode().equals(KeyCode.ENTER)) {
                    attemptToDirectConnect();
                }
            });

            // Validators as empty fields are invalid.
            RequiredFieldValidator validator = new RequiredFieldValidator();
            validator.setMessage("Field is Required");
            textField.getValidators().add(validator);
        }

        autoSelectGame.setOnMouseReleased(e -> {
            ServerListing listing;
            DiscoveryServerClient client = new DiscoveryServerClient();

            try {
                listing = client.getRandomServer();
            } catch (Exception e1) {
                ViewManager.getInstance().showErrorSnackBar("Unable to connect to matchmaking server. Are you connected to the internet?");
                return;
            }

            if (client.didFail()){
                return;
            }

            if (listing == null || listing.equals(ServerRegistrationMessage.getEmptyRegistration())) {
                ViewManager.getInstance().showErrorSnackBar("There are currently no servers available for you to connect to.");
                return;
            }

            ViewManager.getInstance().getGameClient().runAsClient(listing.getAddress(), listing.getPortNumber());
        });

        /*
        // Validating the hostname
        HostNameFieldValidator hostNameValidator = new HostNameFieldValidator();
        hostNameValidator.setMessage("Host name incorrect");
        roomCodeInput.getValidators().add(hostNameValidator);

        // Validating the port number
        NumberRangeValidator portNumberValidator = new NumberRangeValidator(1025, 65536);
        portNumberValidator.setMessage("Port number incorrect");
        serverPortNumber.getValidators().add(portNumberValidator);
        TODO later
        */

        // Start listening for servers on network
        try {
            ServerListener.getInstance().setDelegate(this);
        } catch (IOException e) {
            logger.warn("Could not start Server Listener Delegate");
        }

        // Create Label for no servers found.
        noServersFound = new Label();
        noServersFound.minWidthProperty().bind(serverListVBox.widthProperty());
        noServersFound.setAlignment(Pos.CENTER);
        noServersFound.setText("No Servers Found");
        noServersFound.setStyle(
            "-fx-font-size: 30px;"
                + "-fx-padding:50px;"
                + "-fx-text-fill: -fx-pp-dark-text-color;"
        );
        serverListVBox.getChildren().add(noServersFound);

        roomConnectButton.setOnMouseReleased(e -> {
            String roomCode = roomNumber.getText();
            connectToRoomCode(roomCode);
        });

        // Set up dialog for server creation
        serverListHostButton.setOnAction(action -> {
            showServerCreationDialog();
        });
    }

    /**
     * Shows Server Creation Dialog when "Host" button is clicked.
     */
    private void showServerCreationDialog() {
        Platform.runLater(() -> {
            FXMLLoader dialogContent = new FXMLLoader(getClass().getResource(
                "/views/dialogs/ServerCreationDialog.fxml"));
            try {
                JFXDialog dialog = new JFXDialog(serverListMainStackPane, dialogContent.load(),
                    DialogTransition.CENTER);
                dialog.show();
                Sounds.playButtonClick();
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Could not create Server Creation Dialog.");
            }
        });
    }

    private JFXDialog createDirectConnectDialog() {
        FXMLLoader dialog = new FXMLLoader(
                getClass().getResource("/views/dialogs/DirectConnect.fxml"));

        JFXDialog dcDialog = null;

        try {
            dcDialog = new JFXDialog(serverListMainStackPane, dialog.load(),
                    JFXDialog.DialogTransition.CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DirectConnectController controller = dialog.getController();

        return dcDialog;
    }

    /**
     * Validates the connection and attempts to connect to a given hostname and port number.
     */
    private void attemptToDirectConnect() {
        /*if (validateDirectConnection(serverHostName.getText(), serverPortNumber.getText())) {
            DirectConnect();
        }*/
    }

    /**
     * Checks if the hostName and portNumber are valid values to connect to.
     * @param hostName host name to check.
     * @param portNumber port number to check
     * @return boolean value if host and port number are valid values
     */
    private Boolean validateDirectConnection(String hostName, String portNumber) {
        /*Boolean hostNameValid = ValidationTools.validateTextField(serverHostName);
        *
        Boolean portNumberValid = ValidationTools.validateTextField(serverPortNumber);

        return hostNameValid && portNumberValid;*/
        return true;
    }

    private void connectToRoomCode(String roomCode){
        DiscoveryServerClient client = new DiscoveryServerClient();
        ServerListing serverListing;

        if (client.didFail()){
            return;
        }

        try {
            serverListing = client.getServerForRoomCode(roomCode);
        } catch (Exception e) {
            ViewManager.getInstance().showErrorSnackBar("Error connecting to matchmaking server. Please try again later.");
            return;
        }

        if (serverListing == null || serverListing.equals(new ServerListing("","","", 0, 0))){
            ViewManager.getInstance().showErrorSnackBar("No servers could be found with that room code.");
            return;
        }

        try {
            ViewManager.getInstance().getGameClient().runAsClient(serverListing.getAddress(), serverListing.getPortNumber());
        }
        catch (Exception e) {
            ViewManager.getInstance().showErrorSnackBar("Error connecting to matchmaking service.");
        }
    }

    /**
     * Connects the user to a lobby via the Direct Connect form.
     */
    private void DirectConnect() {
        Sounds.playButtonClick();
       // ViewManager.getInstance().getGameClient().runAsClient(serverHostName.getText(), Integer.parseInt(serverPortNumber.getText()));
    }

    /**
     * Refreshes the list of available servers.
     * @param servers A list of ServerDescription objects showing available servers.
     */
    private void refreshServers(List<ServerDescription> servers) {
        serverListVBox.getChildren().clear();

        if (servers.size() == 0) { // "No Servers Found"
            serverListVBox.getChildren().add(noServersFound);
        } else { // Populate the server list with a series of server cell objects.
            for (ServerDescription server : servers) {
                VBox pane = null;

                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cells/ServerCell.fxml"));

                loader.setController(new ServerCell(server));

                try {
                    pane = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                serverListVBox.getChildren().add(pane);
            }
        }
    }

    @Override
    public void serverRemoved(List<ServerDescription> servers) {
        Platform.runLater(() -> refreshServers(servers));
    }

    @Override
    public void serverDetected(ServerDescription serverDescription, List<ServerDescription> servers) {
        Platform.runLater(() -> refreshServers(servers));
    }
}
