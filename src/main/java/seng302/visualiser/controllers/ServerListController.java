package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
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
import seng302.gameServer.ServerDescription;
import seng302.visualiser.ServerListener;
import seng302.visualiser.ServerListenerDelegate;
import seng302.visualiser.controllers.cells.ServerCell;
import seng302.visualiser.validators.HostNameFieldValidator;
import seng302.visualiser.validators.NumberRangeValidator;

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
    private JFXButton connectButton;
    @FXML
    private JFXTextField serverHostName;
    @FXML
    private JFXTextField serverPortNumber;
    //---------FXML END---------//

    //Validators
    private HostNameFieldValidator hostNameValidator;
    private NumberRangeValidator portNumberValidator;


    private Label noServersFound;
    private Logger logger = LoggerFactory.getLogger(ServerListController.class);

    public void initialize(URL location, ResourceBundle resources) {

        serverListVBox.minWidthProperty().bind(serverListScrollPane.widthProperty());

        // Set Event Bindings
        connectButton.setOnMouseReleased(event -> attemptToDirectConnect());
        for (JFXTextField textField : Arrays.asList(serverHostName, serverPortNumber)) {
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

        // Validating the hostname
        hostNameValidator = new HostNameFieldValidator();
        hostNameValidator.setMessage("Host Name is Incorrect");
        serverHostName.getValidators().add(hostNameValidator);

        portNumberValidator = new NumberRangeValidator(1025, 65536);
        portNumberValidator.setMessage("Port Number is Incorrect");
        serverPortNumber.getValidators().add(portNumberValidator);

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

        // Set up dialog for server creation
        Platform.runLater(() -> {
            FXMLLoader dialogContent = new FXMLLoader(getClass().getResource(
                "/views/dialogs/ServerCreationDialog.fxml"));
            try {
                JFXDialog dialog = new JFXDialog(serverListMainStackPane, dialogContent.load(),
                    DialogTransition.CENTER);
                serverListHostButton.setOnAction(action -> dialog.show());
            } catch (IOException e) {
                logger.warn("Could not create Server Creation Dialog.");
            }
        });
    }

    /**
     *
     */
    private void attemptToDirectConnect() {
        if (validateDirectConnection(serverHostName.getText(), serverPortNumber.getText())) {
            DirectConnect();
        }
    }


    private Boolean validateDirectConnection(String hostName, String portNumber) {
        Boolean hostNameValid = validateTextField(serverHostName);
        Boolean portNumberValid = validateTextField(serverPortNumber);

        return hostNameValid && portNumberValid;
    }


    /**
     *
     * @return
     */
    private Boolean validateTextField(JFXTextField textField) {
        textField.validate();
        for (ValidatorBase validator : textField.getValidators()) {
            if (validator.getHasErrors()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    private void DirectConnect() {
        ViewManager.getInstance().getGameClient().runAsClient(serverHostName.getText(), Integer.parseInt(serverPortNumber.getText()));
    }

    @Override
    public void serverRemoved(List<ServerDescription> servers) {
        Platform.runLater(() -> refreshServers(servers));
    }

    @Override
    public void serverDetected(ServerDescription serverDescription, List<ServerDescription> servers) {
        Platform.runLater(() -> refreshServers(servers));
    }

    /**
     *
     * @param servers
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
}
