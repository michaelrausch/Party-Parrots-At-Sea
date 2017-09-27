package seng302.visualiser;

import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import seng302.model.ClientYacht;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.utilities.Sounds;

/**
 * Class converts a map preview to a minimap by adding boats.
 */
public class MiniMap extends MapPreview {

    private HashMap<ClientYacht, Polygon> boatIcons = new HashMap<>();
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
            Polygon boatIcon = new Polygon(0, -3.5, 3.5, 3.5, -3.5, 3.5);
            boatIcon.setStroke(Color.BLACK);
            boatIcon.setFill(Color.GRAY);
            boatIcon.setFill(yacht.getColour());
            boatIcon.setFill(yacht.getColour());
            boatIcons.put(yacht, boatIcon);
            boatIcon.getTransforms().add(new Rotate(0));
            yacht.addLocationListener((boat, lat, lon, heading, sailIn, velocity) -> {
                Polygon bi = boatIcons.get(boat);
                Point2D p2d = scaledPoint.findScaledXY(lat, lon);
                bi.setLayoutX(p2d.getX());
                bi.setLayoutY(p2d.getY());
                ((Rotate) bi.getTransforms().get(0)).setAngle(heading);
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
