package seng302.visualiser;

import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import seng302.model.ClientYacht;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.utilities.Sounds;

/**
 * Created by cir27 on 28/09/17.
 */
public class MiniMap extends MapPreview {

    private HashMap<ClientYacht, Circle> boatIcons = new HashMap<>();
    private Polygon playerBoat;
    private double playerRotation;
    private List<ClientYacht> boats;
    private ClientYacht player;

    public MiniMap (List<CompoundMark> marks, List<Corner> course, List<Limit> border, List<ClientYacht> boats, ClientYacht player) {
        super(marks, course, border);
        this.boats = boats;
        this.player = player;
        setBoats(boats);
//        player.addMarkRoundingListener(this::updateMarkArrows);
    }

    public void setBoats(List<ClientYacht> yachts) {
        for (ClientYacht yacht : yachts) {
            Circle boatIcon = new Circle(0, 0, 4);
            boatIcon.setStroke(Color.BLACK);
            boatIcon.setFill(Color.GRAY);
            boatIcon.setFill(yacht.getColour());
            boatIcon.setFill(yacht.getColour());
            boatIcons.put(yacht, boatIcon);
            yacht.addLocationListener((boat, lat, lon, heading, sailIn, velocity) -> {
                Circle bi = boatIcons.get(boat);
                Point2D p2d = scaledPoint.findScaledXY(lat, lon);
                bi.setCenterX(p2d.getX());
                bi.setCenterY(p2d.getY());
            });
        }
        Platform.runLater(() -> {
            gameObjects.getChildren().addAll(boatIcons.values());
        });
    }

    private void updateMarkArrows (ClientYacht yacht, int legNumber) {
        CompoundMark compoundMark;
        if (legNumber - 1 >= 0) {
            Sounds.playMarkRoundingSound();
            compoundMark = course.get(legNumber-1);
            for (Mark mark : compoundMark.getMarks()) {
                markerObjects.get(mark).showNextExitArrow();
            }
        }
        CompoundMark nextMark = null;
        if (legNumber < course.size() - 1) {
            Sounds.playMarkRoundingSound();
            nextMark = course.get(legNumber);
            for (Mark mark : nextMark.getMarks()) {
                markerObjects.get(mark).showNextEnterArrow();
            }
        }
        if (legNumber - 2 >= 0) {
            CompoundMark lastMark = course.get(Math.max(0, legNumber - 2));
            if (lastMark != nextMark) {
                for (Mark mark : lastMark.getMarks()) {
                    markerObjects.get(mark).hideAllArrows();
                }
            }
        }
    }
}
