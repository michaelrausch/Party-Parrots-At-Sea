package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import seng302.gameServer.ServerDescription;
import seng302.visualiser.controllers.ViewManager;
import seng302.visualiser.validators.FieldLengthValidator;

import java.net.URL;
import java.util.ResourceBundle;

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

    FieldLengthValidator fieldLengthValidator;
    RequiredFieldValidator fieldRequiredValidator;

    public void initialize(URL location, ResourceBundle resources) {
        updateMaxPlayerLabel();
        maxPlayersSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateMaxPlayerLabel();
        });

        fieldLengthValidator = new FieldLengthValidator(40);
        fieldLengthValidator.setMessage("Server Name Too Long");
        fieldRequiredValidator = new RequiredFieldValidator();
        fieldRequiredValidator.setMessage("Server Name is Required.");
        serverName.setValidators(fieldLengthValidator, fieldRequiredValidator);

        submitBtn.setOnMouseClicked(event -> submitBtn.setText("CREATING..."));
        submitBtn.setOnMouseReleased(event -> validateServerSettings());
    }

    private void validateServerSettings() {
        serverName.validate();
        System.out.println(serverName.getActiveValidator());
        createServer();
    }


    public void createServer() {

        ServerDescription serverDescription = ViewManager.getInstance().getGameClient()
            .runAsHost("localhost", 4941, serverName.getText(), (int) maxPlayersSlider
                .getValue());

        ViewManager.getInstance().setProperty("serverName", serverDescription.getName());
        ViewManager.getInstance().setProperty("mapName", serverDescription.getMapName());
    }

    private void updateMaxPlayerLabel() {
        maxPlayersSlider.setValue(Math.floor(maxPlayersSlider.getValue()));
        maxPlayersLabel.setText(String.format("YOU SELECTED: %.0f", maxPlayersSlider.getValue()));
    }
}
