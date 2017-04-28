package seng302.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import seng302.models.parsers.StreamPacket;
import seng302.models.parsers.StreamParser;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by michaelrausch on 21/03/17.
 */
public class Controller implements Initializable {
    @FXML
    private AnchorPane contentPane;
    @FXML
    private Label timeTillLive;
    @FXML
    private Button streamButton;
    @FXML
    private Button switchToRaceViewButton;

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

    public void startStream() {
        if (StreamParser.isStreamStatus()) {
            streamButton.setVisible(false);
            timeTillLive.setVisible(true);
            timeTillLive.setTextFill(Color.GREEN);
            timeTillLive.setText("Connecting...");
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (StreamParser.getTimeSinceStart() != 0) {
                            timeTillLive.setTextFill(Color.BLACK);
                            switchToRaceViewButton.setDisable(false);
                            Long timerMinute = -1 * StreamParser.getTimeSinceStart() / 60;
                            Long timerSecond = -1 * StreamParser.getTimeSinceStart() % 60;
                            String timerString = timerMinute + "." + timerSecond + " minutes";
                            timeTillLive.setText(timerString);
                        }
                    });
                }
            }, 0, 500);
        } else {
            timeTillLive.setText("Stream not available.");
            timeTillLive.setTextFill(Color.RED);
        }
    }

    public void switchToRaceView() {
        setContentPane("/views/RaceView.fxml");
    }
}
