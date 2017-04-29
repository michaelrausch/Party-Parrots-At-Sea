package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by michaelrausch on 21/03/17.
 */
public class Controller implements Initializable {
    @FXML
    private AnchorPane contentPane;

    private void setContentPane(String jfxUrl){
        try{
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getChildren().addAll((Pane) FXMLLoader.load(getClass().getResource(jfxUrl)));
        }
        catch(javafx.fxml.LoadException e){
            System.err.println(e.getCause());
        }
        catch(IOException e){
            System.err.println(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
