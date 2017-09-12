package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import seng302.gameServer.ServerDescription;
import seng302.utilities.Sounds;
import seng302.visualiser.controllers.ViewManager;
import seng302.visualiser.validators.FieldLengthValidator;
import seng302.visualiser.validators.ValidationTools;

public class ServerCreationController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private JFXTextField serverName;
    @FXML
    private JFXSlider maxPlayersSlider;
    @FXML
    private Label maxPlayersLabel;
    @FXML
    private JFXButton submitBtn;
    //---------FXML END---------//

    public void initialize(URL location, ResourceBundle resources) {
        updateMaxPlayerLabel();
        maxPlayersSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateMaxPlayerLabel();
        });

        FieldLengthValidator fieldLengthValidator = new FieldLengthValidator(40);
        fieldLengthValidator.setMessage("Server name too long.");

        RequiredFieldValidator fieldRequiredValidator = new RequiredFieldValidator();
        fieldRequiredValidator.setMessage("Server name is required.");

        serverName.setValidators(fieldLengthValidator, fieldRequiredValidator);

        submitBtn.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            validateServerSettings();
        });
    }

    /**
     * Validates that a server has a valid name and creates the server.
     */
    private void validateServerSettings() {
        submitBtn.setText("CREATING...");
        if (ValidationTools.validateTextField(serverName)) {
            createServer();
        } else {
            submitBtn.setText("SUBMIT");
        }
    }

    /**
     * Creates a server with a given set of details.
     */
    private void createServer() {
        ServerDescription serverDescription = ViewManager.getInstance().getGameClient()
            .runAsHost("localhost", 4941, serverName.getText(), (int) maxPlayersSlider
                .getValue());

        ViewManager.getInstance().setProperty("serverName", serverDescription.getName());
        ViewManager.getInstance().setProperty("mapName", serverDescription.getMapName());
    }

    /**
     * Updates a label as the user slides along the max players slider.
     */
    private void updateMaxPlayerLabel() {
        maxPlayersSlider.setValue(Math.floor(maxPlayersSlider.getValue()));
        maxPlayersLabel.setText(String.format("YOU SELECTED: %.0f", maxPlayersSlider.getValue()));
    }

    public void playButtonHoverSound(MouseEvent mouseEvent) {
        Sounds.playHoverSound();
    }

}
