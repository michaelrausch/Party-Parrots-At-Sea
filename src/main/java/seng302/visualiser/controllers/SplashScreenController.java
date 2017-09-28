package seng302.visualiser.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * The pre loading screen before launch the start view
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
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    try {
                        Stage stage = new Stage();
                        ViewManager.getInstance().initialStartView(stage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rootPane.getScene().getWindow().hide();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
