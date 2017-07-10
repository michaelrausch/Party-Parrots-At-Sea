package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * A Class describing the actions of the start screen controller
 * Created by wmu16 on 10/07/17.
 */
public class StartScreen2Controller {



    @FXML
    private TextField ipTextField;
    @FXML
    private GridPane startScreen2;


    private void setContentPane(String jfxUrl) {
        try {
            AnchorPane contentPane = (AnchorPane) startScreen2.getParent();
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            contentPane.getChildren()
                    .addAll((Pane) FXMLLoader.load(getClass().getResource(jfxUrl)));
        } catch (javafx.fxml.LoadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void hostButtonPressed() {
        setContentPane("/views/LobbyView.fxml");
    }


    @FXML
    public void connectButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function

        System.out.println("connecting to: " + ipTextField.getText());
    }
}
