package seng302.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import seng302.models.Boat;
import seng302.models.Course;
import seng302.models.Race;
import seng302.models.mark.Mark;
import seng302.models.mark.MarkType;
import seng302.models.parsers.ConfigParser;
import seng302.models.parsers.CourseParser;
import seng302.models.parsers.TeamsParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller for main window for RaceVision
 * Created by Kusal on 3/22/2017.
 */
public class Controller implements Initializable {

    private static final int MARKER_WIDTH = 10;
    private static final int MARKER_HEIGHT = 10;
    private static final double ORIGINAL_LAT = 32.321504;
    private static final double ORIGINAL_LON = -64.857063;


    @FXML
    private Canvas courseCanvas;

    @FXML
    private Group boatGroup;

    @FXML
    private Button playPauseButton;


    private Course thisCourse;
    private ArrayList<Boat> startingBoats;
    private int raceDuration;
    private Race race;
    private boolean raceRunning = false;

    private CourseParser cp;
    private TeamsParser tp;
    private ConfigParser cop;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cp = new CourseParser("/config/course.xml");
        tp = new TeamsParser("/config/teams.xml");
        cop = new ConfigParser("/config/config.xml");

        thisCourse = new Course(cp.getCourse());
        startingBoats = tp.getBoats();
        race = new Race(thisCourse.getMarks(), startingBoats, cop.getTimeScale(), this);
        init();
    }

    /**
     * Initialises a race on the screen after it has been loaded
     */
    private void init() {
        initMap();
        initBoats();
        playPauseButton.setDisable(false);
    }

    /**
     * Initialise the map by drawing it onto courseCanvas.
     */
    private void initMap() {
        //Create the boundary of the course displayed on the map
        drawCourse();
    }


    /**
     * Draw the markers and gates onto the courseCanvas.
     */
    private void drawCourse() {
        GraphicsContext gc = courseCanvas.getGraphicsContext2D();
        gc.save();

        for(Mark rp : thisCourse.getMarks()) {
            if (rp.getMarkType().equals(MarkType.SINGLE_MARK)) {
                gc.setFill(Color.GRAY);
                gc.fillOval(convertLongToX(rp.getLongitude()), convertLatToY(rp.getLatitude()), MARKER_WIDTH, MARKER_HEIGHT);
            } else if (rp.getMarkType().equals(MarkType.GATE_MARK)) {
                gc.setFill(Color.GRAY);
                gc.fillOval(convertLongToX(rp.getLongitude()), convertLatToY(rp.getLatitude()), MARKER_WIDTH, MARKER_HEIGHT);
//                gc.fillOval(((OpenGate) rp).getDrawX2(), ((OpenGate) rp).getDrawY2(), MARKER_WIDTH, MARKER_HEIGHT);

                gc.setLineWidth(2);
                gc.setFill(Color.GREEN);
                gc.setStroke(Color.GREEN);
            }
                gc.fillOval(convertLongToX(rp.getLongitude()), convertLatToY(rp.getLatitude()), MARKER_WIDTH, MARKER_HEIGHT);
//                gc.fillOval(((ClosedGate) rp).getDrawX2(), ((ClosedGate) rp).getDrawY2(), MARKER_WIDTH, MARKER_HEIGHT);
//                gc.strokeLine(convertLongToX(rp.getLongitude()) + 5, convertLatToY(rp.getLatitude()) + 5, ((ClosedGate) rp).getDrawX2() + 5, ((ClosedGate) rp).getDrawY2() + 5);
            }
            gc.restore();
    }

    /**
     * Places boats at starting line
     */
    private void initBoats() {

//        int startingX = (convertLongToX(thisCourse.getMarks().get(0).getLongitude())).intValue();
//        int startingY = (convertLatToY(thisCourse.getMarks().get(0).getLongitude())).intValue();

        int startingX = 50;
        int startingY = 100;

        for(Boat boat : startingBoats) {
            boat.moveBoatTo(startingX, startingY);
            boatGroup.getChildren().add(boat.getBoatObject());
            boat.setCurrentLeg(race.getRaceLegs().get(0));
            boat.setHeading(race.getRaceLegs().get(0).getHeading());
            boat.setLegDistance(0d);
        }
    }


    /**
     * Starts and stops the race depending on whether or not it is already running
     */
    public void playPause() {
        if (!raceRunning) {
            play();
        } else {
            pause();
        }
    }

    /**
     * Plays the race and updates the play / pause button
     */
    private void play() {
        race.run();
        raceRunning = true;
        playPauseButton.setText("Pause");

    }

    /**
     * Pauses the race and updates the play / pause button
     */
    private void pause() {
        race.pause();
        raceRunning = false;
        playPauseButton.setText("Play");
    }



    private Double convertLongToX(Double lon) {
        return (lon - ORIGINAL_LON) * thisCourse.getDistanceScaleFactor();
    }

    private Double convertLatToY(Double lat) {
        return (ORIGINAL_LAT - lat) * thisCourse.getDistanceScaleFactor();
    }

    public Button getPlayPauseButton() {
        return playPauseButton;
    }
}
