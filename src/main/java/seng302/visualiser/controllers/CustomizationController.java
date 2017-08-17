package seng302.visualiser.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seng302.gameServer.messages.CustomizeRequestType;
import seng302.visualiser.ClientToServerThread;

public class CustomizationController {

    @FXML
    private TextField nameField;

    @FXML
    private ColorPicker boatColorPicker;

    @FXML
    private Button customizeSubmit;

    private LobbyController lc;
    private ClientToServerThread socketThread;
    private Stage windowStage;

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
        Color color = boatColorPicker.getValue();

        short red = (short) (color.getRed() * 255);
        short green = (short) (color.getGreen() * 255);
        short blue = (short) (color.getBlue() * 255);

        byte[] colorArray = new byte[3];

        colorArray[0] = (byte) red;
        colorArray[1] = (byte) green;
        colorArray[2] = (byte) blue;

        socketThread.sendCustomizationRequest(CustomizeRequestType.COLOR, colorArray);
        lc.setPlayersColor(color);
        windowStage.close();
    }

    public void setLobbyController(LobbyController lc) {
        this.lc = lc;
    }

    public void setStage(Stage stage) {
        this.windowStage = stage;
    }

    public void setPlayerName(String name) {
        this.nameField.setText(name);
    }

    public void setPlayerColor(Color playerColor) {
        this.boatColorPicker.setValue(playerColor);
    }


}
