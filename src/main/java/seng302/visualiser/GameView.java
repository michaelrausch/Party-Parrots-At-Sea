package seng302.visualiser;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import seng302.gameServer.messages.RoundingSide;
import seng302.model.GeoPoint;
import seng302.model.Limit;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.mark.Mark;
import seng302.utilities.GeoUtility;
import seng302.visualiser.fxObjects.assets_2D.*;

import java.util.*;

/**
 * Created by cir27 on 20/07/17.
 */
public class GameView extends Pane {

    private double bufferSize = 50;
    private double horizontalBuffer = 0;

    private double canvasWidth = 1100;
    private double canvasHeight = 920;
    private boolean horizontalInversion = false;

    private double distanceScaleFactor;
    private ScaleDirection scaleDirection;
    private GeoPoint minLatPoint, minLonPoint, maxLatPoint, maxLonPoint;
    private double referencePointX, referencePointY;

    private Polygon raceBorder = new CourseBoundary();

    /* Note that if either of these is null then values for it have not been added and the other
       should be used as the limits of the map. */
    private List<Limit> borderPoints;
    private Map<Mark, Marker2D> markerObjects;

    private ObservableList<Node> gameObjects;
    private Group markers = new Group();
    private Group tokens = new Group();
    private List<CompoundMark> course = new ArrayList<>();

    private ImageView mapImage = new ImageView();

    private enum ScaleDirection {
        HORIZONTAL,
        VERTICAL
    }

    public GameView () {
        gameObjects = this.getChildren();
        gameObjects.addAll(mapImage, raceBorder, markers, tokens);
    }

    /**
     * Adds a course to the GameView. The view is scaled accordingly unless a border is set in which
     * case the course is added relative ot the border.
     *
     * @param newCourse the mark objects that make up the course.
     * @param sequence The sequence the marks travel through
     */
    public void updateCourse(List<CompoundMark> newCourse, List<Corner> sequence) {
        markerObjects = new HashMap<>();

        for (Corner corner : sequence) { //Makes course out of all compound marks.
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
            rescaleRace(new ArrayList<>(markerObjects.keySet()));
        }
        //Move the Markers to initial position.
        markerObjects.forEach(((mark, marker2D) -> {
            Point2D p2d = findScaledXY(mark.getLat(), mark.getLng());
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
            Point2D p2d = findScaledXY(lat, lon);
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
    private Gate makeAndBindGate(Marker2D m1, Marker2D m2, Paint colour) {
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
    public void updateBorder(List<Limit> border) {
        if (borderPoints == null) {
            borderPoints = border;
            rescaleRace(new ArrayList<>(borderPoints));
        }

        rescaleRace(new ArrayList<>(border));

        List<Double> boundaryPoints = new ArrayList<>();
        for (Limit limit : border) {
            Point2D location = findScaledXY(limit.getLat(), limit.getLng());
            boundaryPoints.add(location.getX());
            boundaryPoints.add(location.getY());
        }
        raceBorder.getPoints().setAll(boundaryPoints);
    }

    /**
     * Rescales the race to the size of the window.
     *
     * @param limitingCoordinates the set of geo points that contains the extremities of the race.
     */
    public void rescaleRace(List<GeoPoint> limitingCoordinates) {
        //Check is called once to avoid unnecessarily change the course limits once the race is running
        findMinMaxPoint(limitingCoordinates);
        double minLonToMaxLon = scaleRaceExtremities();
        calculateReferencePointLocation(minLonToMaxLon);
    }

    /**
     * Sets the class variables minLatPoint, maxLatPoint, minLonPoint, maxLonPoint to the point with
     * the leftmost point, rightmost point, southern most point and northern most point
     * respectively.
     */
    private void findMinMaxPoint(List<GeoPoint> points) {
        List<GeoPoint> sortedPoints = new ArrayList<>(points);
        sortedPoints.sort(Comparator.comparingDouble(GeoPoint::getLat));
        minLatPoint = new GeoPoint(sortedPoints.get(0).getLat(), sortedPoints.get(0).getLng());
        GeoPoint maxLat = sortedPoints.get(sortedPoints.size() - 1);
        maxLatPoint = new GeoPoint(maxLat.getLat(), maxLat.getLng());

        sortedPoints.sort(Comparator.comparingDouble(GeoPoint::getLng));
        minLonPoint = new GeoPoint(sortedPoints.get(0).getLat(), sortedPoints.get(0).getLng());
        GeoPoint maxLon = sortedPoints.get(sortedPoints.size() - 1);
        maxLonPoint = new GeoPoint(maxLon.getLat(), maxLon.getLng());
        if (maxLonPoint.getLng() - minLonPoint.getLng() > 180) {
            horizontalInversion = true;
        }
    }

    /**
     * Calculates the location of a reference point, this is always the point with minimum latitude,
     * in relation to the canvas.
     *
     * @param minLonToMaxLon The horizontal distance between the point of minimum longitude to
     * maximum longitude.
     */
    private void calculateReferencePointLocation(double minLonToMaxLon) {
        GeoPoint referencePoint = minLatPoint;
        double referenceAngle;

        if (scaleDirection == ScaleDirection.HORIZONTAL) {
            referenceAngle = Math.abs(
                GeoUtility.getBearingRad(referencePoint, minLonPoint)
            );
            referencePointX =
                bufferSize + distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility
                    .getDistance(referencePoint, minLonPoint);
            referenceAngle = Math.abs(GeoUtility.getDistance(referencePoint, maxLatPoint));
            referencePointY = canvasHeight - (bufferSize + bufferSize);
            referencePointY -= distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility
                .getDistance(referencePoint, maxLatPoint);
            referencePointY = referencePointY / 2;
            referencePointY += bufferSize;
            referencePointY += distanceScaleFactor * Math.cos(referenceAngle) * GeoUtility
                .getDistance(referencePoint, maxLatPoint);
        } else {
            referencePointY = canvasHeight - bufferSize;
            referenceAngle = Math.abs(
                Math.toRadians(
                    GeoUtility.getDistance(referencePoint, minLonPoint)
                )
            );
            referencePointX = bufferSize;
            referencePointX += distanceScaleFactor * Math.sin(referenceAngle) * GeoUtility
                .getDistance(referencePoint, minLonPoint);
            referencePointX +=
                ((canvasWidth - (bufferSize + bufferSize)) - (minLonToMaxLon * distanceScaleFactor))
                    / 2;
            referencePointX += horizontalBuffer;
        }
        if (horizontalInversion) {
            referencePointX = canvasWidth - bufferSize - (referencePointX - bufferSize);
        }
    }


    /**
     * Finds the scale factor necessary to fit all race markers within the onscreen map and assigns
     * it to distanceScaleFactor Returns the max horizontal distance of the map.
     */
    private double scaleRaceExtremities() {

        double vertAngle = Math.abs(
            GeoUtility.getBearingRad(minLatPoint, maxLatPoint)
        );
        double vertDistance =
            Math.cos(vertAngle) * GeoUtility.getDistance(minLatPoint, maxLatPoint);
        double horiAngle = Math.abs(
            GeoUtility.getBearingRad(minLonPoint, maxLonPoint)
        );
        if (horiAngle <= (Math.PI / 2)) {
            horiAngle = (Math.PI / 2) - horiAngle;
        } else {
            horiAngle = horiAngle - (Math.PI / 2);
        }
        double horiDistance =
            Math.cos(horiAngle) * GeoUtility.getDistance(minLonPoint, maxLonPoint);

        double vertScale = (canvasHeight - (bufferSize + bufferSize)) / vertDistance;

        if ((horiDistance * vertScale) > (canvasWidth - (bufferSize + bufferSize))) {
            distanceScaleFactor = (canvasWidth - (bufferSize + bufferSize)) / horiDistance;
            scaleDirection = ScaleDirection.HORIZONTAL;
        } else {
            distanceScaleFactor = vertScale;
            scaleDirection = ScaleDirection.VERTICAL;
        }
        return horiDistance;
    }

    private Point2D findScaledXY(double unscaledLat, double unscaledLon) {
        double distanceFromReference;
        double angleFromReference;
        double xAxisLocation = referencePointX;
        double yAxisLocation = referencePointY;

        angleFromReference = GeoUtility.getBearingRad(
            minLatPoint, new GeoPoint(unscaledLat, unscaledLon)
        );
        distanceFromReference = GeoUtility.getDistance(
            minLatPoint, new GeoPoint(unscaledLat, unscaledLon)
        );
        if (angleFromReference >= 0 && angleFromReference <= Math.PI / 2) {
            xAxisLocation += Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else if (angleFromReference >= 0) {
            angleFromReference = angleFromReference - Math.PI / 2;
            xAxisLocation += Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        } else if (angleFromReference < 0 && angleFromReference >= -Math.PI / 2) {
            angleFromReference = Math.abs(angleFromReference);
            xAxisLocation -= Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
            yAxisLocation -= Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
        } else {
            angleFromReference = Math.abs(angleFromReference) - Math.PI / 2;
            xAxisLocation -= Math
                .round(distanceScaleFactor * Math.cos(angleFromReference) * distanceFromReference);
            yAxisLocation += Math
                .round(distanceScaleFactor * Math.sin(angleFromReference) * distanceFromReference);
        }
        if (horizontalInversion) {
            xAxisLocation = canvasWidth - bufferSize - (xAxisLocation - bufferSize);
        }
        return new Point2D(xAxisLocation, yAxisLocation);
    }


    public void setSize(Double width, Double height){
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    public void setHorizontalBuffer(Double buff){
        this.horizontalBuffer = buff;
    }
}
