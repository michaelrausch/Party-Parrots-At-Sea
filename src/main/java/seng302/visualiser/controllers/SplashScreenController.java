package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXSnackbar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seng302.gameServer.ServerAdvertiser;
import seng302.utilities.Sounds;
import seng302.visualiser.GameClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Kusal on 26-Sep-17.
 */
public class SplashScreenController implements Initializable{

    @FXML
    private StackPane rootPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new SplashScreen().start();
    }


    class SplashScreen extends Thread {
        public void run(){
            try {
                Thread.sleep(2000);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Stage stage = new Stage();
                            ViewManager.getInstance().initialStartView(stage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        rootPane.getScene().getWindow().hide();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
