package seng302.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import seng302.models.*;
import seng302.models.parsers.StreamParser;

import java.io.IOException;
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
    private Button selectAnnotationBtn;
    @FXML
    private CanvasController includedCanvasController;

    private ArrayList<Yacht> startingBoats = new ArrayList<>();
    private boolean displayFps;
    private Timeline timerTimeline;
    private Race race;
    private Stage stage;
    private Integer annotationLevel;
    private Map<String, Boolean> importantAnnotations = new HashMap<>();

    public void initialize() {

        RaceController raceController = new RaceController();
        raceController.initializeRace();
        race = raceController.getRace();
        for (Yacht boat : race.getBoats()) {
            startingBoats.add(boat);
        }

        includedCanvasController.setup(this);
        includedCanvasController.initializeCanvas();
        initializeTimer();
        initializeSettings();
        initialiseWindDirection();
        initialisePositionVBox();
        includedCanvasController.timer.start();

        selectAnnotationBtn.setOnAction(event -> {
            loadSelectAnnotationView();
        });
    }

    /**
     * Important annotations have been changed, update this view
     * @param newImportantAnnotations HashMap containing whether or not annotations
     *                                are important
     */
    void importantAnnotationsChanged(Map<String, Boolean> newImportantAnnotations){
        this.importantAnnotations = newImportantAnnotations;
        setAnnotations((int)annotationSlider.getValue());
    }

    /**
     * Loads the "select annotations" view in a new window
     */
    private void loadSelectAnnotationView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = new Stage();

            // Set controller
            ImportantAnnotationController controller = new ImportantAnnotationController(this, stage);
            fxmlLoader.setController(controller);

            // Load FXML and set CSS
            fxmlLoader.setLocation(getClass().getResource("/views/importantAnnotationSelectView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 469, 248);
            scene.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            stage.initStyle(StageStyle.UNDECORATED);

            stage.setScene(scene);
            stage.show();

            controller.loadState(importantAnnotations);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Map<String, Boolean> getImportantAnnotations(){
        return importantAnnotations;
    }

    private void initializeSettings() {
        displayFps = true;

        toggleFps.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                displayFps = !displayFps;
            }
        });

        //SLIDER STUFF BELOW
        annotationSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double n) {
                if (n == 0) return "None";
                if (n == 1) return "Low";
                if (n == 2) return "Important";
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
                    case "Important":
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

    /**
     * Display the list of boats in the order they finished the race
     */
    private void loadRaceResultView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FinishView.fxml"));
        loader.setController(new RaceResultController(race));

        try {
            contentAnchorPane.getChildren().removeAll();
            contentAnchorPane.getChildren().clear();
            contentAnchorPane.getChildren().addAll((Pane) loader.load());

        } catch (javafx.fxml.LoadException e) {
            System.err.println(e.getCause());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void showOrder() {
        positionVbox.getChildren().clear();
        positionVbox.getChildren().removeAll();
        positionVbox.getStylesheets().add(getClass().getResource("/css/master.css").toString());

        for (Yacht boat : StreamParser.getBoatsPos().values()) {
            if (boat.getBoatStatus() == 3) {  // 3 is finish status
                Text textToAdd = new Text(boat.getPosition() + ". " +
                        boat.getShortName() + " (Finished)");
                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
                positionVbox.getChildren().add(textToAdd);

            } else {
                Text textToAdd = new Text(boat.getPosition() + ". " +
                        boat.getShortName() + " ");
                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
                textToAdd.setStyle("");
                positionVbox.getChildren().add(textToAdd);
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

    public void stopTimer() {
        timerTimeline.stop();
    }
    public void startTimer() {
        timerTimeline.play();
    }

    public boolean isDisplayFps() {
        return displayFps;
    }

    public Race getRace() {
        return race;
    }

    public ArrayList<Yacht> getStartingBoats(){
        return startingBoats;
    }

    private void setAnnotations(Integer annotationLevel) {
        switch (annotationLevel) {
            case 0:
                for (RaceObject ro : includedCanvasController.getRaceObjects()) {
                    if(ro instanceof BoatGroup) {
                        BoatGroup bg = (BoatGroup) ro;
                        bg.setTeamNameObjectVisible(false);
                        bg.setVelocityObjectVisible(false);
                        bg.setLineGroupVisible(false);
                        bg.setWakeVisible(false);
                    }
                }
                break;
            case 1:
                for (RaceObject ro : includedCanvasController.getRaceObjects()) {
                    if(ro instanceof BoatGroup) {
                        BoatGroup bg = (BoatGroup) ro;
                        bg.setTeamNameObjectVisible(true);
                        bg.setVelocityObjectVisible(false);
                        bg.setLineGroupVisible(false);
                        bg.setWakeVisible(false);
                    }
                }
                break;
            // Important Annotations
            case 2:
                for (RaceObject ro : includedCanvasController.getRaceObjects()) {
                    if(ro instanceof BoatGroup) {
                        BoatGroup bg = (BoatGroup) ro;

                        if (importantAnnotations.containsKey("BoatName") && importantAnnotations.get("BoatName")){
                            bg.setTeamNameObjectVisible(true);
                        }
                        else{
                            bg.setTeamNameObjectVisible(false);
                        }

                        if (importantAnnotations.containsKey("BoatSpeed") && importantAnnotations.get("BoatSpeed")){
                            bg.setVelocityObjectVisible(true);
                        }
                        else{
                            bg.setTeamNameObjectVisible(false);
                        }

                        if (importantAnnotations.containsKey("BoatTrack") && importantAnnotations.get("BoatTrack")){
                            bg.setLineGroupVisible(true);
                        }
                        else{
                            bg.setLineGroupVisible(false);
                        }

                        if (importantAnnotations.containsKey("BoatWake") && importantAnnotations.get("BoatWake")){
                            bg.setWakeVisible(true);
                        }
                        else{
                            bg.setWakeVisible(false);
                        }
                    }
                }
                break;
            case 3:
                for (RaceObject ro : includedCanvasController.getRaceObjects()) {
                    if(ro instanceof BoatGroup) {
                        BoatGroup bg = (BoatGroup) ro;
                        bg.setTeamNameObjectVisible(true);
                        bg.setVelocityObjectVisible(true);
                        bg.setLineGroupVisible(true);
                        bg.setWakeVisible(true);
                    }
                }
                break;
        }
    }

    void setStage (Stage stage) {
        this.stage = stage;
    }

    Stage getStage () {
        return stage;
    }
}