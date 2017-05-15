package seng302.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.StringConverter;
import seng302.models.*;
import seng302.models.stream.StreamParser;

import java.util.*;

/**
 * Created by ptg19 on 29/03/17.
 */
public class RaceViewController extends Thread{
    @FXML
    private VBox positionVbox;
    @FXML
    private CheckBox toggleFps;
    @FXML
    private Text timerLabel;
    @FXML
    private AnchorPane contentAnchorPane;
    @FXML
    private Text windArrowText, windDirectionText;
    @FXML
    private Slider annotationSlider;
    @FXML
    private CanvasController includedCanvasController;

    private ArrayList<Yacht> startingBoats = new ArrayList<>();
    private boolean displayFps;
    private Timeline timerTimeline;
    private ArrayList<Yacht> boatOrder = new ArrayList<>();

    public void initialize() {

        includedCanvasController.setup(this);
        includedCanvasController.initializeCanvas();
        initializeTimer();
        initializeSettings();
        initialiseWindDirection();
        initialisePositionVBox();
        includedCanvasController.timer.start();
    }



    private void initializeSettings() {
        displayFps = true;

        toggleFps.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                displayFps = !displayFps;
            }
        });

        //SLIFER STUFF BELOW
        annotationSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 0) return "None";
                if (n == 1) return "Low";
                if (n == 2) return "Medium";
                if (n == 3) return "All";

                return "All";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "None":
                        return 0d;
                    case "Low":
                        return 1d;
                    case "Medium":
                        return 2d;
                    case "All":
                        return 3d;

                    default:
                        return 3d;
                }
            }
        });

        annotationSlider.valueProperty().addListener((obs, oldval, newVal) ->
                    setAnnotations((int)annotationSlider.getValue()));

        annotationSlider.setValue(3);
    }

    private void initializeTimer(){
        timerTimeline = new Timeline();
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        // Run timer update every second
        timerTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        event -> {
                            if (StreamParser.isRaceFinished()) {
                                timerLabel.setFill(Color.RED);
                                timerLabel.setText("Race Finished!");
                            } else {
                                timerLabel.setText(currentTimer());
                            }
                        })
        );

        // Start the timer
        timerTimeline.playFromStart();
    }

    private void initialiseWindDirection() {
        Timeline windDirTimeline = new Timeline();
        windDirTimeline.setCycleCount(Timeline.INDEFINITE);
        windDirTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        event -> {
                            windDirectionText.setText(String.format("%.1f°", StreamParser.getWindDirection()));
                            windArrowText.setRotate(StreamParser.getWindDirection());
                        })
        );
        windDirTimeline.playFromStart();
    }

    private void initialisePositionVBox() {
        Timeline posVBoxTimeline = new Timeline();
        posVBoxTimeline.setCycleCount(Timeline.INDEFINITE);
        posVBoxTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        event -> {
                            showOrder();
                        })
        );
        posVBoxTimeline.playFromStart();
    }

    private void showOrder() {
        positionVbox.getChildren().clear();
        positionVbox.getChildren().removeAll();
        for (Yacht boat : StreamParser.getBoatsPos().values()) {
            if (boat.getBoatStatus() == 3) {  // 3 is finish status
                positionVbox.getChildren().add(new Text(boat.getPosition() + ". " +
                        boat.getShortName() + " (Finished)"));
            } else {
                positionVbox.getChildren().add(new Text(boat.getPosition() + ". " +
                        boat.getShortName() + " "));
            }

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

    private String currentTimer() {
        String timerString = "0:00";
        if (StreamParser.getTimeSinceStart() > 0) {
            String timerMinute = Long.toString(StreamParser.getTimeSinceStart() / 60);
            String timerSecond = Long.toString(StreamParser.getTimeSinceStart() % 60);
            if (timerSecond.length() == 1) {
                timerSecond = "0" + timerSecond;
            }
            timerString = "-" + timerMinute + ":" + timerSecond;
        } else {
            String timerMinute = Long.toString(-1 * StreamParser.getTimeSinceStart() / 60);
            String timerSecond = Long.toString(-1 * StreamParser.getTimeSinceStart() % 60);
            if (timerSecond.length() == 1) {
                timerSecond = "0" + timerSecond;
            }
            timerString = timerMinute + ":" + timerSecond;
        }
        return timerString;
    }

    public boolean isDisplayFps() {
        return displayFps;
    }

    private void setAnnotations(Integer annotationLevel) {
        switch (annotationLevel) {
            case 0:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                        bg.setTeamNameObjectVisible(false);
                        bg.setVelocityObjectVisible(false);
                        bg.setLineGroupVisible(false);
                        bg.setWakeVisible(false);
                }
                break;
            case 1:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                        bg.setTeamNameObjectVisible(true);
                        bg.setVelocityObjectVisible(false);
                        bg.setLineGroupVisible(false);
                        bg.setWakeVisible(false);
                }
                break;
            case 2:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                        bg.setTeamNameObjectVisible(true);
                        bg.setVelocityObjectVisible(false);
                        bg.setLineGroupVisible(true);
                        bg.setWakeVisible(false);
                }
                break;
            case 3:
                for (BoatGroup bg : includedCanvasController.getBoatGroups()) {
                        bg.setTeamNameObjectVisible(true);
                        bg.setVelocityObjectVisible(true);
                        bg.setLineGroupVisible(true);
                        bg.setWakeVisible(true);
                }
                break;
        }
    }
}