package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.model.ClientYacht;
import seng302.visualiser.GameClient;

import java.io.IOException;
import java.util.HashMap;

public class ViewManager {

    private static ViewManager instance;
    private GameClient gameClient;
    private JFXDecorator decorator;
    private HashMap<String, String> props; //TODO is this the best way to do this??
    private ObservableList<String> playerList;
    private Logger logger = LoggerFactory.getLogger(ViewManager.class);

    private ViewManager(){
        props = new HashMap<>();
        gameClient = new GameClient(decorator);
    }

    private FXMLLoader loadFxml(String fxmlLocation) {
        return new FXMLLoader(
                getClass().getResource(fxmlLocation)
        );
    }

    public static ViewManager getInstance(){
        if (instance == null){
            instance = new ViewManager();
        }

        return instance;
    }

    public void setDecorator(JFXDecorator decorator){
        this.decorator = decorator;
    }

    public JFXDecorator getDecorator(){
        return decorator;
    }

    public void setScene(Node scene){
        Platform.runLater(() -> decorator.setContent(scene));
    }

    public void goToStartView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/StartScreenView.fxml"));
            this.setScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameClient getGameClient(){
        return gameClient;
    }

    public String getProperty(String key){
        return props.get(key);
    }

    public void setProperty(String key, String val){
        props.put(key, val);
    }

    public void setPlayerList(ObservableList<String> playerList) {
        this.playerList = playerList;
    }

    public ObservableList<String> getPlayerList(){
        return playerList;
    }

    public LobbyController goToLobby(Boolean disableReadyButton){
        FXMLLoader loader = loadFxml("/views/LobbyView.fxml");

        try {
            setScene(loader.load());
        } catch (IOException e) {
            logger.error("Could not load lobby view");
        }

        if (disableReadyButton){
            LobbyController lobbyController = loader.getController();
            lobbyController.disableReadyButton();
        }

        return loader.getController();
    }

    public RaceViewController loadRaceView() {
        FXMLLoader loader = loadFxml("/views/RaceView.fxml");

        try {
            setScene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        decorator.getScene().setOnKeyPressed((ke) -> gameClient.keyPressed(ke));
        decorator.getScene().setOnKeyReleased((ke) -> gameClient.keyReleased(ke));

        return loader.getController();
    }

    public JFXDialog loadCustomizationDialog(StackPane parent, LobbyController lobbyController, Color playerColor, String name) {
        FXMLLoader dialog = loadFxml("/views/dialogs/BoatCustomizeDialog.fxml");

        JFXDialog customizationDialog = null;

        try {
            customizationDialog = new JFXDialog(parent, dialog.load(),
                    JFXDialog.DialogTransition.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }


        BoatCustomizeController controller = dialog.getController();

        controller.setParentController(lobbyController);
        controller.setPlayerColor(playerColor);
        controller.setPlayerName(name);
        controller.setServerThread(gameClient.getServerThread());


        return customizationDialog;
    }
}
