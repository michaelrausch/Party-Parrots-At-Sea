package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXDecorator;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import seng302.visualiser.GameClient;

import java.io.IOException;
import java.util.HashMap;

public class ViewManager {

    private static ViewManager instance;
    private GameClient gameClient;
    private JFXDecorator decorator;
    private HashMap<String, String> props; //TODO is this the best way to do this??
    private ObservableList<String> playerList;

    private ViewManager(){
        props = new HashMap<>();
        gameClient = new GameClient(decorator);
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
        decorator.setContent(scene);
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
}
