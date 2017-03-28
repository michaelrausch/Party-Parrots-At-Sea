package seng302.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import seng302.models.Boat;
import seng302.models.Event;
import seng302.models.Race;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

/**
 * Created by ptg19 on 29/03/17.
 */
public class RaceViewController {
    @FXML
    private VBox positionVbox;
    @FXML
    private CheckBox toggleAnnotation, toggleFps;
    @FXML
    private Text timerLabel;
    @FXML
    private AnchorPane contentAnchorPane;
    @FXML
    private Text windArrowText, windDirectionText;

    private boolean displayAnnotations;
    private boolean displayFps;
    private Timeline timeline;
    private Race race;
    private ArrayList<Boat> boatOrder = new ArrayList<>();

    private final double ORIGIN_LAT = 32.321504;
    private final double ORIGIN_LON = -64.857063;
    private final int SCALE = 16000;

//    /**
//     * Controller to control the race timer
//     * @param race the race the timer is timing
//     */
//    public RaceTimerController(Race race){
//        this.race = race;
//    }

    public void initialize() {
        RaceController raceController = new RaceController();
        raceController.initializeRace();
        race = raceController.getRace();

        initializeTimer();
        initializeSettings();



    }

    private void initializeSettings(){
        displayAnnotations = true;
        displayFps = true;

        toggleAnnotation.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                displayAnnotations = !displayAnnotations;
            }
        });
        toggleFps.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                displayFps = !displayFps;
            }
        });
    }

    private void initializeTimer(){
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        // Run timer update every second
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        event -> {
                            // Stop timer if race is finished
                            if (this.race.isRaceFinished()) {
                                this.timeline.stop();
                            } else {
                                timerLabel.setText(convertTimeToMinutesSeconds(race.getRaceTime()));
                                this.race.incrementRaceTime();
                            }
                        })
        );

        // Start the timer
        timeline.playFromStart();
    }

    public void handleEvent(Event event) {
        Boat boat = event.getBoat();
        boatOrder.remove(boat);
        boat.setMarkLastPast(event.getMarkPosInRace());
        boatOrder.add(boat);
        boatOrder.sort(new Comparator<Boat>() {
            @Override
            public int compare(Boat b1, Boat b2) {
                return b2.getMarkLastPast() - b1.getMarkLastPast();
            }
        });
        showOrder();
    }

    private void showOrder() {
        positionVbox.getChildren().clear();
        positionVbox.getChildren().removeAll();

        for (Boat boat : boatOrder) {
            positionVbox.getChildren().add(new Text(boat.getShortName() + " " + boat.getSpeedInKnots() + " Knots"));
        }
    }

    /**
     * Convert seconds to a string of the format mm:ss
     *
     * @param time the time in seconds
     * @return a formatted string
     */
    public String convertTimeToMinutesSeconds(int time) {
        if (time < 0) {
            return String.format("-%02d:%02d", (time * -1) / 60, (time * -1) % 60);
        }
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    /**
     * Stop the race timer
     */
    public void stop() {
        timeline.stop();
    }

    /**
     * Start the race timer
     */
    public void start() {
        timeline.play();
    }
}