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
import seng302.models.*;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.MarkType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by ptg19 on 15/03/17.
 */
public class CanvasController {
    @FXML private Canvas canvas;
    Race race;
    GraphicsContext gc;
    HashMap<Boat, TimelineInfo> timelineInfos;


    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.scale(5,5);
        RaceController raceController = new RaceController();
        raceController.initializeRace();
        race = raceController.getRace();
        timelineInfos = new HashMap<>();

        HashMap<Boat, List> boat_events = race.getEvents();
//        System.out.println(boat_events);

        // generating timelines
        for (Boat boat : boat_events.keySet()) {
            DoubleProperty x  = new SimpleDoubleProperty();
            DoubleProperty y  = new SimpleDoubleProperty();
            List<KeyFrame> keyFrames = new ArrayList<>();
            List<Event> events = boat_events.get(boat);
            for (Event event: events){
                keyFrames.add(
//                        new KeyFrame(Duration.seconds(event.getDistanceBetweenMarks()/event.getBoat().getVelocity()),
                        new KeyFrame(Duration.seconds(event.getTime()/60/60/5),
                                new KeyValue(x, event.getMark().getLatitude()),
                                new KeyValue(y, event.getMark().getLongitude())
                        )
                );
//                drawBoat(gc, event.getMark().getLatitude(), event.getMark().getLongitude(), Colors.getColor());
                System.out.println(event.getMark().getName());
            }
            timelineInfos.put(boat, new TimelineInfo(new Timeline(keyFrames.toArray(new KeyFrame[keyFrames.size()])), x, y));
        }

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0,0,760,360);
                drawCourse();
                drawBoats();
            }
        };
        timer.start();
        for (TimelineInfo timelineInfo: timelineInfos.values()){
            Timeline timeline = timelineInfo.getTimeline();
            timeline.play();
        }
    }

    private void drawBoats(){
        for (Boat boat: timelineInfos.keySet()){
            TimelineInfo timelineInfo = timelineInfos.get(boat);
            drawBoat(timelineInfo.getX().doubleValue(), timelineInfo.getY().doubleValue(), boat.getColor());
        }
    }

    private void drawBoat(double x, double y, Color color) {
        x = abs(x - 32.313291) * 1000;  // to prevent negative longtitude
        y = abs(y + 64.887057) * 1000;  // to prevent negative latitude

//        y = abs(y);
        int diameter = 2;
        gc.setFill(color);
        gc.fillOval(x, y, diameter, diameter);
    }

    private void drawCourse(){
        for (Mark mark: race.getCourse()){
            gc.setFill(Color.BLACK);

            if (mark.getMarkType() == MarkType.SINGLE_MARK){
                double x = abs(mark.getLatitude() - 32.313291) * 1000;  // to prevent negative longtitude
                double y = abs(mark.getLongitude() + 64.887057) * 1000;  // to prevent negative latitude

                gc.fillOval(x, y, 2, 2);
            }

            else if (mark.getMarkType() == MarkType.GATE_MARK){
                double x;
                double y;
                GateMark gateMark = (GateMark) mark;
                Mark mark1 = gateMark.getSingleMark1();
                Mark mark2 = gateMark.getSingleMark1();

                x = abs(mark1.getLatitude() - 32.313291) * 1000;  // to prevent negative longtitude
                y = abs(mark1.getLongitude() + 64.887057) * 1000;  // to prevent negative latitude
                gc.fillOval(x, y, 2, 2);

                x = abs(mark2.getLatitude() - 32.313291) * 1000;  // to prevent negative longtitude
                y = abs(mark2.getLongitude() + 64.887057) * 1000;  // to prevent negative latitude
                gc.fillOval(x, y, 2, 2);
            }
        }
    }
}
