package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import seng302.gameServer.messages.CustomizeRequestType;
import seng302.utilities.Sounds;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.controllers.LobbyController;
import seng302.visualiser.controllers.ViewManager;
import seng302.visualiser.validators.FieldLengthValidator;
import seng302.visualiser.validators.ValidationTools;

public class BoatCustomizeController implements Initializable{

    //--------FXML BEGIN--------//
    @FXML
    private JFXColorPicker colorPicker;
    @FXML
    private JFXButton submitBtn;
    @FXML
    private JFXTextField boatName;
    @FXML
    void colorChanged(ActionEvent event) {
        Color color = colorPicker.getValue();
    }
    //---------FXML END---------//

    private ClientToServerThread socketThread;
    private LobbyController lobbyController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        submitBtn.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            submitCustomization();
        });

        socketThread = ViewManager.getInstance().getGameClient().getServerThread();

        RequiredFieldValidator playerNameReqValidator = new RequiredFieldValidator();
        playerNameReqValidator.setMessage("Player name required.");

        FieldLengthValidator playerNameLengthValidator = new FieldLengthValidator(20);
        playerNameLengthValidator.setMessage("Player name too long.");

        boatName.setValidators(playerNameLengthValidator, playerNameReqValidator);

        submitBtn.setOnMouseEntered(e -> Sounds.playHoverSound());
    }

    /**
     * Attempts to submit a valid customization packet for boat name and boat color.
     */
    private void submitCustomization() {

        if (ValidationTools.validateTextField(boatName)) {
            socketThread
                .sendCustomizationRequest(CustomizeRequestType.NAME, boatName.getText().getBytes());

            Color color = colorPicker.getValue();
            short red = (short) (color.getRed() * 255);
            short green = (short) (color.getGreen() * 255);
            short blue = (short) (color.getBlue() * 255);

            byte[] colorArray = new byte[3];

            colorArray[0] = (byte) red;
            colorArray[1] = (byte) green;
            colorArray[2] = (byte) blue;

            socketThread.sendCustomizationRequest(CustomizeRequestType.COLOR, colorArray);
            lobbyController.closeCustomizationDialog();
        }
    }

    public void setPlayerName(String name) {
        this.boatName.setText(name);
    }

    public void setPlayerColor(Color playerColor) {
        this.colorPicker.setValue(playerColor);
    }

    public void setParentController(LobbyController lobbyController){
        this.lobbyController = lobbyController;
    }
}
