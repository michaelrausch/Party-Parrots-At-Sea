package seng302.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import seng302.models.parsers.StreamParser;
import seng302.models.parsers.XMLParser;


import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private TableView<XMLParser.BoatXMLObject.Boat> teamList;
    @FXML
    private TableColumn<XMLParser.BoatXMLObject.Boat, String> boatNameCol;
    @FXML
    private TableColumn<XMLParser.BoatXMLObject.Boat, String> shortNameCol;
    @FXML
    private TableColumn<XMLParser.BoatXMLObject.Boat, String> countryCol;
    @FXML
    private Label realTime;

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
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        realTime.setText(format.format(System.currentTimeMillis()));
    }

    /**
     * Running a timer to update the livestream status on welcome screen. Update interval is 1 second.
     */
    public void startStream() {
        if (StreamParser.isStreamStatus()) {
            XMLParser xmlParser = StreamParser.getXmlObject();
            streamButton.setVisible(false);
            realTime.setVisible(true);
            timeTillLive.setVisible(true);
            timeTillLive.setTextFill(Color.GREEN);
            timeTillLive.setText("Connecting...");
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (StreamParser.isRaceFinished()) {
                            realTime.setText(StreamParser.getCurrentTimeString());
                            timeTillLive.setTextFill(Color.RED);
                            timeTillLive.setText("Race finished! Waiting for new race...");
                            switchToRaceViewButton.setDisable(true);
                        } else if (StreamParser.getTimeSinceStart() > 0) {
                            realTime.setText(StreamParser.getCurrentTimeString());
                            updateTeamList();
                            timeTillLive.setTextFill(Color.RED);
                            switchToRaceViewButton.setDisable(false);
                            String timerMinute = Long.toString(StreamParser.getTimeSinceStart() / 60);
                            String timerSecond = Long.toString(StreamParser.getTimeSinceStart() % 60);
                            if (timerSecond.length() == 1) {
                                timerSecond = "0" + timerSecond;
                            }
                            String timerString = "-" + timerMinute + ":" + timerSecond;
                            timeTillLive.setText(timerString);
                        } else {
                            realTime.setText(StreamParser.getCurrentTimeString());
                            updateTeamList();
                            timeTillLive.setTextFill(Color.BLACK);
                            switchToRaceViewButton.setDisable(false);
                            String timerMinute = Long.toString(-1 * StreamParser.getTimeSinceStart() / 60);
                            String timerSecond = Long.toString(-1 * StreamParser.getTimeSinceStart() % 60);
                            if (timerSecond.length() == 1) {
                                timerSecond = "0" + timerSecond;
                            }
                            String timerString = timerMinute + ":" + timerSecond;
                            timeTillLive.setText(timerString);
                        }
                    });
                }
            }, 0, 1000);
        } else {
            timeTillLive.setText("Stream not available.");
            timeTillLive.setTextFill(Color.RED);
        }
    }

    public void switchToRaceView() {
        setContentPane("/views/RaceView.fxml");
    }

    private void updateTeamList() {
        ObservableList<XMLParser.BoatXMLObject.Boat> data = FXCollections.observableArrayList();
        teamList.setItems(data);
        boatNameCol.setCellValueFactory(
                new PropertyValueFactory<>("BoatName")
        );
        shortNameCol.setCellValueFactory(
                new PropertyValueFactory<>("ShortName")
        );
        countryCol.setCellValueFactory(
                new PropertyValueFactory<>("Country")
        );
        for (XMLParser.BoatXMLObject.Boat boat : StreamParser.getBoats()) {
            data.add(boat);
        }
    }
}
