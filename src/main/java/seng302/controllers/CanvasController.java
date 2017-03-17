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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Created by ptg19 on 15/03/17.
 */
public class CanvasController {
    @FXML private Canvas canvas;


    public void initialize() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.scale(5,5);
        RaceController raceController = new RaceController();
        raceController.initializeRace();
        Race race = raceController.getRace();
        HashMap<Boat, TimelineInfo> timelineInfos = new HashMap<>();

        HashMap<Boat, List> boat_events = race.getEvents();
//        System.out.println(boat_events);
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
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0,0,760,360);
                gc.setFill(Color.FORESTGREEN);
                for (Boat boat: timelineInfos.keySet()){
                    TimelineInfo timelineInfo = timelineInfos.get(boat);
//                    System.out.println(timelineInfo.getX().doubleValue());
//                    System.out.println(timelineInfo.getY().doubleValue());
                    drawBoat(gc, timelineInfo.getX().doubleValue(), timelineInfo.getY().doubleValue(), boat.getColor());
                }
            }
        };
        timer.start();
        for (TimelineInfo timelineInfo: timelineInfos.values()){
            Timeline timeline = timelineInfo.getTimeline();
            timeline.play();
        }

//        drawBoat(gc, 0, 0, Color.GREEN);
//        drawBoat(gc, 100, 100, Color.BLUE);
//        drawBoat(gc, 32.296577, -64.854304, Color.RED);
//        drawBoat(gc, 32.293771, -64.855242, Color.RED);
//        drawBoat(gc, 32.317379, -64.839291, Color.GREEN);
//        drawBoat(gc, 32.317257, -64.836260, Color.GREEN);
//        drawBoat(gc, 32.313291, -64.887057, Color.YELLOW);


    }

    private void drawBoat(GraphicsContext gc, double x, double y, Color color) {
        x = abs(x - 32.313291) * 1000;  // to prevent negative longtitude
        y = abs(y + 64.887057) * 1000;  // to prevent negative latitude

//        y = abs(y);
        int diameter = 2;
        gc.setFill(color);
        gc.fillOval(x, y, diameter, diameter);
    }
}
