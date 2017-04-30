package seng302.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.StringConverter;
import seng302.models.*;
import seng302.models.parsers.ConfigParser;

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
    private CanvasController includedCanvasController;

    private ArrayList<Boat> startingBoats = new ArrayList<>();
    private boolean displayFps;
    private Timeline timerTimeline;
    private Map<Boat, TimelineInfo> timelineInfos = new HashMap<>();
    private ArrayList<Boat> boatOrder = new ArrayList<>();
    private Race race;

    public void initialize() {

        RaceController raceController = new RaceController();
        raceController.initializeRace();
        race = raceController.getRace();
        for (Boat boat : race.getBoats()) {
            startingBoats.add(boat);
        }
//        try{
//            initializeTimelines();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

        includedCanvasController.setup(this);
        includedCanvasController.initializeCanvas();
        //initializeTimer();
        initializeSettings();

        //set wind direction!!!!!!! can't find another place to put my code --haoming
        double windDirection = new ConfigParser("/config/config.xml").getWindDirection();
        windDirectionText.setText(String.format("%.1f°", windDirection));
        windArrowText.setRotate(windDirection);
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
                            // Stop timer if race is finished
                            if (this.race.isRaceFinished()) {
                                this.timerTimeline.stop();
                            } else {
                                timerLabel.setText(convertTimeToMinutesSeconds(race.getRaceTime()));
                                this.race.incrementRaceTime();
                            }
                        })
        );

        // Start the timer
        timerTimeline.playFromStart();
    }

    /**
     * Generates time line for each boat, and stores time time into timelineInfos hash map
     */
    private void initializeTimelines() {
        HashMap<Boat, List> boat_events = race.getEvents();
        for (Boat boat : boat_events.keySet()) {
            startingBoats.add(boat);
//            // x, y are the real time coordinates
//            DoubleProperty x = new SimpleDoubleProperty();
//            DoubleProperty y = new SimpleDoubleProperty();
//
//            List<KeyFrame> keyFrames = new ArrayList<>();
//            List<Event> events = boat_events.get(boat);
//
//            // iterates all events and convert each event to keyFrame, then add them into a list
//            for (Event event : events) {
//                if (event.getIsFinishingEvent()) {
//                    keyFrames.add(
//                            new KeyFrame(Duration.seconds(event.getTime()),
//                                    onFinished -> {race.setBoatFinished(boat); handleEvent(event);},
//                                    new KeyValue(x, event.getThisMark().getLatitude()),
//                                    new KeyValue(y, event.getThisMark().getLongitude())
//                            )
//                    );
//                } else {
//                    keyFrames.add(
//                            new KeyFrame(Duration.seconds(event.getTime()),
//                                    onFinished ->{
//                                        handleEvent(event);
//                                        boat.setHeading(event.getBoatHeading());
//                                    },
//                                    new KeyValue(x, event.getThisMark().getLatitude()),
//                                    new KeyValue(y, event.getThisMark().getLongitude())
//                            )
//                    );
//                }
//            }
//            timelineInfos.put(boat, new TimelineInfo(new Timeline(keyFrames.toArray(new KeyFrame[keyFrames.size()])), x, y));
        }
        setRaceDuration();
    }

    private void setRaceDuration(){
        Double maxDuration = 0.0;
        Timeline maxTimeline = null;

        for (TimelineInfo timelineInfo : timelineInfos.values()) {

            Timeline timeline = timelineInfo.getTimeline();
            if (timeline.getTotalDuration().toMillis() >= maxDuration) {
                maxDuration = timeline.getTotalDuration().toMillis();
                maxTimeline = timeline;
            }

            // Timelines are paused by default
            timeline.play();
            timeline.pause();
        }

        maxTimeline.setOnFinished(event -> {
            race.setRaceFinished();
            loadRaceResultView();
        });
    }

    /**
     * Play each boats timerTimeline
     */
    public void playTimelines(){
        for (TimelineInfo timelineInfo : timelineInfos.values()){
            Timeline timeline = timelineInfo.getTimeline();

            if (timeline.getStatus() == Animation.Status.PAUSED){
                timeline.play();
            }
        }
    }

    /**
     * Pause each boats timerTimeline
     */
    public void pauseTimelines(){
        for (TimelineInfo timelineInfo : timelineInfos.values()){
            Timeline timeline = timelineInfo.getTimeline();

            if (timeline.getStatus() == Animation.Status.RUNNING){
                timeline.pause();
            }
        }
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

    public Map<Boat, TimelineInfo> getTimelineInfos() {
        return timelineInfos;
    }

    public ArrayList<Boat> getStartingBoats(){
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
            case 2:
                for (RaceObject ro : includedCanvasController.getRaceObjects()) {
                    if(ro instanceof BoatGroup) {
                        BoatGroup bg = (BoatGroup) ro;
                        bg.setTeamNameObjectVisible(true);
                        bg.setVelocityObjectVisible(false);
                        bg.setLineGroupVisible(true);
                        bg.setWakeVisible(false);
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

}