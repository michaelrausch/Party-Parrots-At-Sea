package seng302.visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import seng302.gameServer.server.messages.CustomizeRequestType;
import seng302.visualiser.ClientToServerThread;

public class CustomizationController {

    @FXML
    private TextField nameField;

    @FXML
    private ColorPicker boatColorPicker;

    @FXML
    private Button customizeSubmit;

    private ClientToServerThread socketThread;

    public void initialize() {

    }

    public void setServerThread(ClientToServerThread ctsThread) {
        this.socketThread = ctsThread;
    }

    @FXML
    public void submitCustomization() {
        System.out.println("Attempting to send");
        socketThread.sendCustomizationRequest(CustomizeRequestType.NAME, nameField.getText().getBytes());
        // TODO: 16/08/17 ajm412: Turn colors into byte array.
        socketThread.sendCustomizationRequest(CustomizeRequestType.COLOR,
            boatColorPicker.getValue().toString().getBytes());
    }



}
