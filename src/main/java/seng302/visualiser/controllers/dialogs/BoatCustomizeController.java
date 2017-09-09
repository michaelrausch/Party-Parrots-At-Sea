package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seng302.gameServer.messages.CustomizeRequestType;
import seng302.visualiser.ClientToServerThread;

import java.net.URL;
import java.util.ResourceBundle;
import seng302.visualiser.controllers.LobbyController;

public class BoatCustomizeController implements Initializable{

    @FXML
    private JFXColorPicker colorPicker;

    @FXML
    private JFXButton submitBtn;

    @FXML
    private JFXTextField boatName;
    private ClientToServerThread socketThread;
    private LobbyController lobbyController;


    public BoatCustomizeController(){

    }

    @FXML
    void colorChanged(ActionEvent event) {
        Color color = colorPicker.getValue();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorPicker.setValue(Color.BISQUE);
        submitBtn.setOnMouseReleased(event -> {
            submitCustomization();
            lobbyController.closeCustomizationDialog();
        });
    }

    @FXML
    public void submitCustomization() {
        socketThread.sendCustomizationRequest(CustomizeRequestType.NAME, boatName.getText().getBytes());
        // TODO: 16/08/17 ajm412: Turn colors into byte array.
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

    public void setPlayerName(String name) {
        this.boatName.setText(name);
    }

    public void setPlayerColor(Color playerColor) {
        this.colorPicker.setValue(playerColor);
    }

    public void setServerThread(ClientToServerThread ctsThread) {
        this.socketThread = ctsThread;
    }

    public void setParentController(LobbyController lobbyController){
        this.lobbyController = lobbyController;
    }
}
