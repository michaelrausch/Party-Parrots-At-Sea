package seng302.visualiser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.GeoPoint;
import seng302.model.Limit;
import seng302.model.ScaledPoint;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.utilities.GeoUtility;
import seng302.visualiser.fxObjects.MarkArrowFactory;
import seng302.visualiser.fxObjects.Marker;
import seng302.visualiser.fxObjects.assets_2D.CourseBoundary;
import seng302.visualiser.fxObjects.assets_2D.Gate;
import seng302.visualiser.fxObjects.assets_2D.Marker2D;

/**
 * Created by cir27 on 20/07/17.
 */
public class MapPreview extends GameView {

    private Polygon raceBorder = new CourseBoundary();

    public MapPreview(List<CompoundMark> marks, List<Corner> course, List<Limit> border) {
        this.compoundMarks = marks;
        this.courseOrder = course;
        this.borderPoints = border;
        gameObjects.getChildren().addAll(raceBorder, markers, tokens);
        gameObjects.parentProperty().addListener((obs, old, parent) -> {
            if (parent != null) {
                canvasWidth = parent.prefWidth(1);
                canvasHeight = parent.prefHeight(1);
                updateBorder(borderPoints);
                updateCourse(compoundMarks, courseOrder);
            }
        });
    }

    @Override
    public Node getAssets() {
        return gameObjects;
    }

    public void setSize(double width, double height) {
        canvasHeight = height;
        canvasWidth = width;
        updateBorder(borderPoints);
        updateCourse(compoundMarks, courseOrder);
    }

    /**
     * Adds a course to the GameView. The view is scaled accordingly unless a border is set in which
     * case the course is added relative ot the border.
     *
     * @param newCourse the mark objects that make up the course.
     * @param sequence The sequence the marks travel through
     */
    @Override
    public void updateCourse(List<CompoundMark> newCourse, List<Corner> sequence) {

        if (newCourse.size() == 0) {
            return;
        }
        compoundMarks = newCourse;
        markerObjects = new HashMap<>();
        courseOrder = sequence;

        for (Corner corner : courseOrder) { //Makes course out of all compound marks.
            for (CompoundMark compoundMark : newCourse) {
                if (corner.getCompoundMarkID() == compoundMark.getId()) {
                    course.add(compoundMark);
                }
            }
        }

        // TODO: 16/08/17 Updating mark roundings here. It should not happen here. Nor should it be done this way.
        for (Corner corner : sequence){
            CompoundMark compoundMark = course.get(corner.getSeqID() - 1);
            compoundMark.setRoundingSide(
                RoundingSide.getRoundingSide(corner.getRounding())
            );
        }

        final List<Gate> gates = new ArrayList<>();
        Paint colour = Color.BLACK;
        //Creates new markers
        for (CompoundMark cMark : newCourse) {
            //Set start and end colour
            if (cMark.getId() == sequence.get(0).getCompoundMarkID()) {
                colour = Color.GREEN;
            } else if (cMark.getId() == sequence.get(sequence.size() - 1).getCompoundMarkID()) {
                colour = Color.RED;
            }
            //Create mark dots
            for (Mark mark : cMark.getMarks()) {
                makeAndBindMarker(mark, colour);
            }
            //Create gate line
            if (cMark.isGate()) {
                for (int i = 1; i < cMark.getMarks().size(); i++) {
                    gates.add(
                        makeAndBindGate(
                            markerObjects.get(cMark.getSubMark(i)),
                            markerObjects.get(cMark.getSubMark(i + 1)),
                            colour
                        )
                    );
                }
            }
            colour = Color.BLACK;
        }

        createMarkArrows(sequence);

        //Scale race to markers if there is no border.
        if (borderPoints == null) {
            scaledPoint = ScaledPoint.makeScaledPoint(
                canvasWidth, canvasHeight, new ArrayList<>(markerObjects.keySet()), false
            );
        }
        //Move the Markers to initial position.
        markerObjects.forEach(((mark, marker2D) -> {
            Point2D p2d = scaledPoint.findScaledXY(mark.getLat(), mark.getLng());
            marker2D.setLayoutX(p2d.getX());
            marker2D.setLayoutY(p2d.getY());
        }));
        Platform.runLater(() -> {
            markers.getChildren().clear();
            markers.getChildren().addAll(gates);
            markers.getChildren().addAll(markerObjects.values());
        });
    }

    /**
     * Calculates all the data needed for to create mark arrows. Requires that a course has been
     * added to the gameview.
     * @param sequence The order in which marks are traversed.
     */
    private void createMarkArrows (List<Corner> sequence) {
        for (int i=1; i < sequence.size()-1; i++) { //General case.
            double averageLat = 0;
            double averageLng = 0;
            int numMarks = course.get(i-1).getMarks().size();
            for (Mark mark : course.get(i-1).getMarks()) {
                averageLat += mark.getLat();
                averageLng += mark.getLng();
            }
            GeoPoint lastMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
            numMarks = course.get(i+1).getMarks().size();
            averageLat = 0;
            averageLng = 0;
            for (Mark mark : course.get(i+1).getMarks()) {
                averageLat += mark.getLat();
                averageLng += mark.getLng();
            }
            GeoPoint nextMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
            // TODO: 16/08/17 This comparison doesn't need to exist but the alternative is to user server enum client side.
            for (Mark mark : course.get(i).getMarks()) {
                markerObjects.get(mark).addArrows(
                    mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                    GeoUtility.getBearing(lastMarkAv, mark),
                    GeoUtility.getBearing(mark, nextMarkAv)
                );
            }
        }
        createStartLineArrows();
        createFinishLineArrows();
    }

    private void createStartLineArrows () {
        double averageLat = 0;
        double averageLng = 0;
        int numMarks = 0;
        for (Mark mark : course.get(1).getMarks()) {
            numMarks += 1;
            averageLat += mark.getLat();
            averageLng += mark.getLng();
        }
        GeoPoint firstMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
        for (Mark mark : course.get(0).getMarks()) {
            markerObjects.get(mark).addArrows(
                mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                0d, //90
                GeoUtility.getBearing(mark, firstMarkAv)
            );
        }
    }

    private void createFinishLineArrows () {
        double numMarks = 0;
        double averageLat = 0;
        double averageLng = 0;
        for (Mark mark : course.get(course.size()-2).getMarks()) {
            numMarks += 1;
            averageLat += mark.getLat();
            averageLng += mark.getLng();
        }
        GeoPoint secondToLastMarkAv = new GeoPoint(averageLat / numMarks, averageLng / numMarks);
        for (Mark mark : course.get(course.size()-1).getMarks()) {
            markerObjects.get(mark).addArrows(
                mark.getRoundingSide() == RoundingSide.STARBOARD ? MarkArrowFactory.RoundingSide.STARBOARD : MarkArrowFactory.RoundingSide.PORT,
                GeoUtility.getBearing(secondToLastMarkAv, mark),
                GeoUtility.getBearing(mark, mark)
            );
        }
    }

    /**
     * Creates a new Marker and binds it's position to the given Mark.
     *
     * @param observableMark The mark to bind the marker to.
     * @param colour The desired colour of the mark
     */
    private void makeAndBindMarker(Mark observableMark, Paint colour) {
        Marker2D marker2D = new Marker2D(colour);
//        marker.addArrows(MarkArrowFactory.RoundingSide.PORT, ThreadLocalRandom.current().nextDouble(91, 180), ThreadLocalRandom.current().nextDouble(1, 90));
        markerObjects.put(observableMark, marker2D);
        observableMark.addPositionListener((mark, lat, lon) -> {
            Point2D p2d = scaledPoint.findScaledXY(lat, lon);
            markerObjects.get(mark).setLayoutX(p2d.getX());
            markerObjects.get(mark).setLayoutY(p2d.getY());
        });
    }

    /**
     * Creates a new gate connecting the given marks.
     *
     * @param m1 The first Mark of the gate.
     * @param m2 The second Mark of the gate.
     * @param colour The desired colour of the gate.
     * @return the new gate.
     */
    private Gate makeAndBindGate(Marker m1, Marker m2, Paint colour) {
        Gate gate = new Gate(colour);
        gate.startXProperty().bind(
            m1.layoutXProperty()
        );
        gate.startYProperty().bind(
            m1.layoutYProperty()
        );
        gate.endXProperty().bind(
            m2.layoutXProperty()
        );
        gate.endYProperty().bind(
            m2.layoutYProperty()
        );
        return gate;
    }

    /**
     * Adds a border to the GameView and rescales to the size of the border, does not rescale if a
     * border already exists. Assumes the border is larger than the course.
     *
     * @param border the race border to be drawn.
     */
    @Override
    public void updateBorder(List<Limit> border) {
        if (border.size() == 0) {
            return;
        }

        borderPoints = border;
        scaledPoint = ScaledPoint.makeScaledPoint(canvasWidth, canvasHeight, border, false);

        List<Double> boundaryPoints = new ArrayList<>();
        for (Limit limit : border) {
            Point2D location = scaledPoint.findScaledXY(limit.getLat(), limit.getLng());
            boundaryPoints.add(location.getX());
            boundaryPoints.add(location.getY());
        }
        raceBorder.getPoints().setAll(boundaryPoints);
    }
}
