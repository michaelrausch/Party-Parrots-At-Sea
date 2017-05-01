package seng302.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import seng302.models.Boat;
import seng302.models.parsers.StreamParser;
import seng302.models.parsers.XMLParser;


import javax.xml.crypto.dsig.XMLObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
    @FXML
    private TableView teamList;
    @FXML
    private TableColumn boatNameCol;
    @FXML
    private TableColumn shortNameCol;
    @FXML
    private TableColumn countryCol;

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

    /**
     * Running a timer to update the livestream status on welcome screen. Update interval is 500 miliseconds.
     */
    public void startStream() {
        if (StreamParser.isStreamStatus()) {
            XMLParser xmlParser = StreamParser.getXmlObject();
            streamButton.setVisible(false);
            timeTillLive.setVisible(true);
            timeTillLive.setTextFill(Color.GREEN);
            timeTillLive.setText("Connecting...");
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (StreamParser.isRaceFinished()) {
                            timeTillLive.setTextFill(Color.RED);
                            timeTillLive.setText("Race finished! Waiting for new race...");
                            switchToRaceViewButton.setDisable(true);
                        } else if (StreamParser.getTimeSinceStart() > 0) {
                            updateTeamList();
                            timeTillLive.setTextFill(Color.RED);
                            switchToRaceViewButton.setDisable(false);
                            Long timerMinute = StreamParser.getTimeSinceStart() / 60;
                            Long timerSecond = StreamParser.getTimeSinceStart() % 60;
                            String timerString = "-" + timerMinute + ":" + timerSecond + " minutes";
                            timeTillLive.setText(timerString);
                        } else {
                            updateTeamList();
                            timeTillLive.setTextFill(Color.BLACK);
                            switchToRaceViewButton.setDisable(false);
                            Long timerMinute = -1 * StreamParser.getTimeSinceStart() / 60;
                            Long timerSecond = -1 * StreamParser.getTimeSinceStart() % 60;
                            String timerString = timerMinute + ":" + timerSecond + " minutes";
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

    private void updateTeamList() {
        ObservableList<Boat> data = FXCollections.observableArrayList();
        teamList.setItems(data);
        boatNameCol.setCellValueFactory(
                new PropertyValueFactory<Boat,String>("boatName")
        );
        shortNameCol.setCellValueFactory(
                new PropertyValueFactory<Boat,String>("shortName")
        );
        countryCol.setCellValueFactory(
                new PropertyValueFactory<Boat,String>("country")
        );
        for (Boat boat : StreamParser.getBoats()) {
            data.add(boat);
        }
    }
}
