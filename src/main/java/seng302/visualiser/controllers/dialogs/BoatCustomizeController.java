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
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import seng302.gameServer.messages.CustomizeRequestType;
import seng302.utilities.Sounds;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.controllers.LobbyController;
import seng302.visualiser.controllers.ViewManager;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;
import seng302.visualiser.fxObjects.assets_3D.BoatModel;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
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
    private Pane boatPane;
    @FXML
    void colorChanged(ActionEvent event) {
        Color color = colorPicker.getValue();
        RefreshBoat();
    }
    //---------FXML END---------//

    private ClientToServerThread socketThread;
    private LobbyController lobbyController;
    private BoatMeshType currentBoat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        socketThread = ViewManager.getInstance().getGameClient().getServerThread();

        RequiredFieldValidator playerNameReqValidator = new RequiredFieldValidator();
        playerNameReqValidator.setMessage("Player name required.");

        FieldLengthValidator playerNameLengthValidator = new FieldLengthValidator(20);
        playerNameLengthValidator.setMessage("Player name too long.");

        boatName.setValidators(playerNameLengthValidator, playerNameReqValidator);

        submitBtn.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            submitCustomization();
        });

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
            socketThread.sendCustomizationRequest(CustomizeRequestType.SHAPE, currentBoat.toString().getBytes());
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

    public void setCurrentBoat(String boatType) {
        Group group = new Group();
        this.currentBoat = BoatMeshType.getBoatMeshType(boatType);
        System.out.println(boatType.toString());
        boatPane.setBackground(new Background(new BackgroundFill(Color.SKYBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        boatPane.getChildren().add(group);
        BoatModel bo = ModelFactory.boatCustomiseView(currentBoat, colorPicker.getValue());
        group.getChildren().add(bo.getAssets());
        group.getChildren().add(new PointLight());
    }

    public void nextBoat(ActionEvent actionEvent) {
        boatPane.getChildren().clear();
        Group group = new Group();
        boatPane.getChildren().add(group);
        currentBoat = BoatMeshType.getNextBoatType(currentBoat);
        BoatModel bo = ModelFactory.boatCustomiseView(currentBoat, colorPicker.getValue());
        group.getChildren().add(bo.getAssets());
        group.getChildren().add(new PointLight());
    }

    public void prevBoat(ActionEvent actionEvent) {
        boatPane.getChildren().clear();
        Group group = new Group();
        boatPane.getChildren().add(group);
        currentBoat = BoatMeshType.getPrevBoatType(currentBoat);
        BoatModel bo = ModelFactory.boatCustomiseView(currentBoat, colorPicker.getValue());
        group.getChildren().add(bo.getAssets());
        group.getChildren().add(new PointLight());
    }

    private void RefreshBoat() {
        boatPane.getChildren().clear();
        Group group = new Group();
        boatPane.getChildren().add(group);
        BoatModel bo = ModelFactory.boatCustomiseView(currentBoat, colorPicker.getValue());
        group.getChildren().add(bo.getAssets());
    }
}
