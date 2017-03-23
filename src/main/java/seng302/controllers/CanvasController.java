package seng302.controllers;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.util.Duration;
import seng302.models.Boat;
import seng302.models.Event;
import seng302.models.Race;
import seng302.models.TimelineInfo;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.MarkType;
import seng302.models.mark.SingleMark;
import seng302.models.parsers.ConfigParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ptg19 on 15/03/17.
 * Modified by Haoming Yin (hyi25) on 20/3/2017.
 */
public class CanvasController {
    @FXML
    private Canvas canvas;
    @FXML
    private AnchorPane contentAnchorPane;
    @FXML
    private Text windArrowText, windDirectionText;
    @FXML
    private Pane raceTimer;
    @FXML
    private BoatPositionController teamPositionsController;
    @FXML
    private CheckBox toggleAnnotation;

    private Race race;
    private GraphicsContext gc;
    private HashMap<Boat, TimelineInfo> timelineInfos;

    private AnchorPane raceResults;

    private final double ORIGIN_LAT = 32.320504;
    private final double ORIGIN_LON = -64.857063;

    private Animation.Status raceStatus = Animation.Status.PAUSED;

    private final int SCALE = 16000;

    private boolean annotationCheck = true;

    /**
     * Initialize the controller
     */
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        //gc.scale(2, 2);
        RaceController raceController = new RaceController();
        raceController.initializeRace();
        race = raceController.getRace();
        timelineInfos = new HashMap<>();

        // overriding the handle so that it can clean canvas and redraw boats and course marks
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 33000000){
                    gc.clearRect(0, 0, 19200, 10800);
                    drawCourse();
                    drawBoats();
                    gc.clearRect(0, 0, canvas.getWidth(),canvas.getHeight());
                    gc.setFill(Color.SKYBLUE);
                    gc.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
                    drawCourse();
                    drawBoats();

                    // If race has started, draw the boats and play the timeline
                    if (race.getRaceTime() > 1){
                        playTimelines();

                    }
                    // Race has not started, pause the timelines
                    else if (race.getRaceTime() < 1 || raceStatus == Animation.Status.RUNNING){
                        pauseTimelines();
                    }

                    lastUpdate = now;
                }

            }
        };

        generateTimelines();
        timer.start();
        loadTimerView();

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
            raceStatus = Animation.Status.RUNNING;
        }

        maxTimeline.setOnFinished(event -> {
            race.setRaceFinished();
            loadRaceResultView();
        });

        toggleAnnotation.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                annotationCheck = !annotationCheck;
            }
        });

        //set wind direction!!!!!!! can't find another place to put my code --haoming
        double windDirection = new ConfigParser("/config.xml").getWindDirection();
        windDirectionText.setText(String.format("%.1f°", windDirection));
        windArrowText.setRotate(windDirection);
    }

    /**
     * Display the list of boats in the order they finished the race
     */
    private void loadRaceResultView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FinishView.fxml"));
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

    /**
     * Load the race timer
     */
    private void loadTimerView(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/raceTimer.fxml"));
        loader.setController(new RaceTimerController(race));

        try{
            raceTimer.getChildren().clear();
            raceTimer.getChildren().removeAll();
            raceTimer.getChildren().addAll((Pane) loader.load());
        }
        catch(javafx.fxml.LoadException e){
            System.out.println(e);
        }
        catch(IOException e){
            System.out.println(e);
        }
    }

    /**
     * Play each boats timeline
     */
    private void playTimelines(){
        for (TimelineInfo timelineInfo : timelineInfos.values()){
            Timeline timeline = timelineInfo.getTimeline();

            if (timeline.getStatus() == Animation.Status.PAUSED){
                timeline.play();
            }
        }
        raceStatus = Animation.Status.RUNNING;
    }

    /**
     * Pause each boats timeline
     */
    private void pauseTimelines(){
        for (TimelineInfo timelineInfo : timelineInfos.values()){
            Timeline timeline = timelineInfo.getTimeline();

            if (timeline.getStatus() == Animation.Status.RUNNING){
                timeline.pause();
            }
        }
        raceStatus = Animation.Status.PAUSED;
    }

    /**
     * Generates time line for each boat, and stores time time into timelineInfos hash map
     */
    private void generateTimelines() {
        HashMap<Boat, List> boat_events = race.getEvents();

        for (Boat boat : boat_events.keySet()) {
            // x, y are the real time coordinates
            DoubleProperty x = new SimpleDoubleProperty();
            DoubleProperty y = new SimpleDoubleProperty();

            List<KeyFrame> keyFrames = new ArrayList<>();
            List<Event> events = boat_events.get(boat);

            // iterates all events and convert each event to keyFrame, then add them into a list
            for (Event event : events) {
                if (event.getIsFinishingEvent()) {
                    keyFrames.add(
                            new KeyFrame(Duration.seconds(event.getTime() / 60 / 60 / 5),
                                    onFinished -> {race.setBoatFinished(boat); teamPositionsController.handleEvent(event);},
                                    new KeyValue(x, event.getThisMark().getLatitude()),
                                    new KeyValue(y, event.getThisMark().getLongitude())
                            )
                    );
                } else {
                    keyFrames.add(
                            new KeyFrame(Duration.seconds(event.getTime() / 60 / 60 / 5),
                                    onFinished -> teamPositionsController.handleEvent(event),
                                    new KeyValue(x, event.getThisMark().getLatitude()),
                                    new KeyValue(y, event.getThisMark().getLongitude())
                            )
                    );
                }
            }

            // uses the lists generated above to create a Timeline for the boat.
            timelineInfos.put(boat, new TimelineInfo(new Timeline(keyFrames.toArray(new KeyFrame[keyFrames.size()])), x, y));
        }
    }

    /**
     * Draws all the boats.
     */
    private void drawBoats() {
        for (Boat boat : timelineInfos.keySet()) {
            TimelineInfo timelineInfo = timelineInfos.get(boat);
            drawBoat(timelineInfo.getX().doubleValue(), timelineInfo.getY().doubleValue(), boat.getColor(), boat.getTeamName(), boat.getSpeedInKnots());
        }
    }

    /**
     * Draws a boat with given (x, y) position in the given color
     *
     * @param lat
     * @param lon
     * @param color
     * @param name
     * @param speed
     */
    private void drawBoat(double lat, double lon, Color color, String name, double speed) {
        // Latitude
        double x = (lon - ORIGIN_LON) * SCALE;
        double y = (ORIGIN_LAT - lat) * SCALE;

        double diameter = 9;

        gc.setFill(color);

        if (annotationCheck) {
            // Set boat text
            gc.setFont(new Font(14));
            gc.setLineWidth(3);
            gc.setFontSmoothingType(FontSmoothingType.GRAY);
            gc.fillText(name + ", " + speed + " knots", x + 15, y + 15);
        }

        gc.fillOval(x, y, diameter, diameter);
    }

    /**
     * Draws the course.
     */
    private void drawCourse() {
        for (Mark mark : race.getCourse()) {
            if (mark.getMarkType() == MarkType.SINGLE_MARK) {
                drawSingleMark((SingleMark) mark, Color.BLACK);
            } else if (mark.getMarkType() == MarkType.GATE_MARK) {
                drawGateMark((GateMark) mark);
            }
        }
    }

    /**
     * Draw a given mark on canvas
     *
     * @param singleMark
     */
    private void drawSingleMark(SingleMark singleMark, Color color) {
        double x = (singleMark.getLongitude() - ORIGIN_LON) * SCALE;
        double y = (ORIGIN_LAT - singleMark.getLatitude()) * SCALE;

        gc.setFill(color);
        gc.fillRect(x,y,5.5,5.5);
    }

    /**
     * Draw a gate mark which contains two single marks
     *
     * @param gateMark
     */
    private void drawGateMark(GateMark gateMark) {
        Color color = Color.BLUE;

        if (gateMark.getName().equals("Start")){
            color = Color.RED;
        }

        if (gateMark.getName().equals("Finish")){
            color = Color.GREEN;
        }

        drawSingleMark(gateMark.getSingleMark1(), color);
        drawSingleMark(gateMark.getSingleMark2(), color);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(color);

        // Convert lat/lon to x,y
        double x1 = (gateMark.getSingleMark1().getLongitude()- ORIGIN_LON) * SCALE;
        double y1 = (ORIGIN_LAT - gateMark.getSingleMark1().getLatitude()) * SCALE;

        double x2 = (gateMark.getSingleMark2().getLongitude() - ORIGIN_LON) * SCALE;
        double y2 = (ORIGIN_LAT - gateMark.getSingleMark2().getLatitude()) * SCALE;

        gc.setLineWidth(1);
        gc.strokeLine(x1, y1, x2, y2);
    }

    public Race getRace(){
        return this.race;
    }
}