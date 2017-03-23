package seng302.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.util.Duration;
import seng302.models.Race;

import java.net.URL;
import java.util.ResourceBundle;

public class RaceTimerController implements Initializable{
    private Timeline timeline;
    private Race race;

    @FXML
    private Text timerLabel;

    /**
     * Convert seconds to a string of the format mm:ss
     * @param time the time in seconds
     * @return a formatted string
     */
    public String convertTimeToMinutesSeconds(int time){
        if (time < 0){
            return String.format("-%02d:%02d", (time * -1) / 60, (time * -1)% 60);
        }
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    /**
     * Controller to control the race timer
     * @param race the race the timer is timing
     */
    public RaceTimerController(Race race){
        this.race = race;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);

        // Run timer update every second
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        event -> {
                            // Stop timer if race is finished
                            if (this.race.isRaceFinished()){
                                this.timeline.stop();
                            }
                            else{
                                timerLabel.setText(convertTimeToMinutesSeconds(race.getRaceTime()));
                                this.race.incrementRaceTime();
                            }
                        })
        );

        // Start the timer
        timeline.playFromStart();
    }

    /**
     * Stop the race timer
     */
    public void stop(){
        timeline.stop();
    }

    /**
     * Start the race timer
     */
    public void start(){
        timeline.play();
    }
}
