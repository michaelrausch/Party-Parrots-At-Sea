package seng302.controllers;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import seng302.models.Boat;
import seng302.models.Event;
import seng302.models.Race;
import seng302.models.TimelineInfo;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.MarkType;
import seng302.models.mark.SingleMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by ptg19 on 15/03/17.
 * Modified by Haoming Yin (hyi25) on 20/3/2017.
 */
public class CanvasController {

    private Race race;
    private GraphicsContext gc;
    private HashMap<Boat, TimelineInfo> timelineInfos;
    @FXML
    private Canvas canvas;

    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.scale(5, 5);
        RaceController raceController = new RaceController();
        raceController.initializeRace();
        race = raceController.getRace();
        timelineInfos = new HashMap<>();

        // overriding the handle so that it can clean canvas and redraw boats and course marks
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, 760, 360);
                drawCourse();
                drawBoats();
            }
        };

        generateTimeline();

        // starts the timer and reads events from each boat's time line
        timer.start();
        for (TimelineInfo timelineInfo : timelineInfos.values()) {
            Timeline timeline = timelineInfo.getTimeline();
            timeline.play();
        }
    }

    /**
     * Generates time line for each boat, and stores time time into timelineInfos hash map
     */
    private void generateTimeline() {
        HashMap<Boat, List> boat_events = race.getEvents();
        for (Boat boat : boat_events.keySet()) {
            // x, y are the real time coordinates
            DoubleProperty x = new SimpleDoubleProperty();
            DoubleProperty y = new SimpleDoubleProperty();

            List<KeyFrame> keyFrames = new ArrayList<>();
            List<Event> events = boat_events.get(boat);
            // iterates all events and convert each event to keyFrame, then add them into a list
            for (Event event : events) {
                keyFrames.add(
                        new KeyFrame(Duration.seconds(event.getTime() / 60 / 60 / 5),
                                new KeyValue(x, event.getMark().getLatitude()),
                                new KeyValue(y, event.getMark().getLongitude())
                        )
                );
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
            drawBoat(timelineInfo.getX().doubleValue(), timelineInfo.getY().doubleValue(), boat.getColor());
        }
    }

    /**
     * Draws a boat with given (x, y) position in the given color
     *
     * @param x
     * @param y
     * @param color
     */
    private void drawBoat(double x, double y, Color color) {
        x = abs(x - 32.313291) * 1000;  // to prevent negative longitude
        y = abs(y + 64.887057) * 1000;  // to prevent negative latitude

        int diameter = 2;
        gc.setFill(color);
        gc.fillOval(x, y, diameter, diameter);
    }

    /**
     * Draws the course.
     */
    private void drawCourse() {
        for (Mark mark : race.getCourse()) {
            if (mark.getMarkType() == MarkType.SINGLE_MARK) {
                drawSingleMark((SingleMark) mark);
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
    private void drawSingleMark(SingleMark singleMark) {
        double x = abs(singleMark.getLatitude() - 32.313291) * 1000;  // to prevent negative longitude
        double y = abs(singleMark.getLongitude() + 64.887057) * 1000;  // to prevent negative latitude
        gc.setFill(Color.BLACK);
        gc.fillOval(x, y, 2, 2);
    }

    /**
     * Draw a gate mark which contains two single marks
     *
     * @param gateMark
     */
    private void drawGateMark(GateMark gateMark) {
        drawSingleMark(gateMark.getSingleMark1());
        drawSingleMark(gateMark.getSingleMark2());
    }
}
