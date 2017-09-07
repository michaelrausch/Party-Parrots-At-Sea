package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXDecorator;
import javafx.scene.Node;
import seng302.visualiser.GameClient;

public class ViewManager {

    private static ViewManager instance;
    private GameClient gameClient;
    private JFXDecorator decorator;

    private ViewManager(){
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

    public GameClient getGameClient(){
        return gameClient;
    }

}
