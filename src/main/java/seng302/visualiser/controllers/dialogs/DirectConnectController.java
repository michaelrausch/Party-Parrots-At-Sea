package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import seng302.gameServer.ServerDescription;
import seng302.utilities.Sounds;
import seng302.visualiser.controllers.ViewManager;
import seng302.visualiser.validators.FieldLengthValidator;
import seng302.visualiser.validators.ValidationTools;

public class DirectConnectController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private JFXTextField serverAddress;
    @FXML
    private JFXTextField portNumber;
    @FXML
    private JFXButton submitBtn;
    //---------FXML END---------//

    public void initialize(URL location, ResourceBundle resources) {
        FieldLengthValidator fieldLengthValidator = new FieldLengthValidator(40);
        fieldLengthValidator.setMessage("Too long.");

        RequiredFieldValidator fieldRequiredValidator = new RequiredFieldValidator();
        fieldRequiredValidator.setMessage("Required.");

        serverAddress.setValidators(fieldLengthValidator, fieldRequiredValidator);
        portNumber.setValidators(fieldLengthValidator, fieldRequiredValidator);

        submitBtn.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            connectToServer();
        });

    }

    /**
     * connects to the server
     */
    private void connectToServer() {
        //TODO fix port number validation
        ViewManager.getInstance().getGameClient()
                .runAsClient(serverAddress.getText(), Integer.parseInt(portNumber.getText()));
    }

    public void playButtonHoverSound(MouseEvent mouseEvent) {
        Sounds.playHoverSound();
    }

}
