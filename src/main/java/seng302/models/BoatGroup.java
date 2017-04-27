package seng302.models;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJIRWIN on 25/04/2017.
 */
public class BoatGroup extends RaceObject{

    private static final double TEAMNAME_X_OFFSET = 15d;
    private static final double TEAMNAME_Y_OFFSET = -20d;
    private static final double VELOCITY_X_OFFSET = 15d;
    private static final double VELOCITY_Y_OFFSET = -10d;
    private static final double VELOCITY_WAKE_RATIO = 2d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Time between sections of race - Should be changed to 200 for actual program.
    private static double expectedUpdateInterval = 200;
    private static int WAKE_FRAME_INTERVAL = 30;
    private double framesForNewLine = 0;
    private Point2D lastPoint;

    private Boat boat;
    private int wakeCounter = WAKE_FRAME_INTERVAL;
    private List<Wake> wakes = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();
    private Polygon boatPoly;
//    private Polygon wakePoly;
    private Text teamNameObject;
    private Text velocityObject;

    public BoatGroup (Boat boat, Color color){
        this.boat = boat;
        initChildren(color);
    }

    public BoatGroup (Boat boat, Color color, double... points)
    {
        initChildren(color, points);
    }

    private void initChildren (Color color, double... points) {
        boatPoly = new Polygon(points);
        boatPoly.setFill(color);
//        boatPoly.setLayoutX(0);
//        boatPoly.setLayoutY(0);
//        boatPoly.relocate(boatPoly.getLayoutX(), boatPoly.getLayoutY());
//
//        wakePoly = new Polygon(
//                5.0,0.0,
//                10.0, boat.getVelocity() * VELOCITY_WAKE_RATIO,
//                0.0, boat.getVelocity() * VELOCITY_WAKE_RATIO
//        );
//        wakePoly.setFill(Color.DARKBLUE);

        teamNameObject = new Text(boat.getShortName());
        velocityObject = new Text(String.valueOf(boat.getVelocity()));

        teamNameObject.setX(TEAMNAME_X_OFFSET);
        teamNameObject.setY(TEAMNAME_Y_OFFSET);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(VELOCITY_X_OFFSET);
        velocityObject.setY(VELOCITY_Y_OFFSET);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());

//        super.getChildren().addAll(wakePoly, boatPoly, teamNameObject, velocityObject);
        super.getChildren().addAll(teamNameObject, velocityObject, boatPoly);
    }

    private void initChildren (Color color) {
       initChildren(color,
               BOAT_WIDTH / 2, 0.0,
               BOAT_WIDTH, BOAT_HEIGHT,
               0.0, BOAT_HEIGHT);
    }

    /**
     * Moves the boat and its children annotations from its current coordinates by specified amounts.
     * @param dx The amount to move the X coordinate by
     * @param dy The amount to move the Y coordinate by
     */
    public void moveGroupBy(double dx, double dy, double rotation) {
        boatPoly.setLayoutX(boatPoly.getLayoutX() + dx);
        boatPoly.setLayoutY(boatPoly.getLayoutY() + dy);
        teamNameObject.setLayoutX(teamNameObject.getLayoutX() + dx);
        teamNameObject.setLayoutY(teamNameObject.getLayoutY() + dy);
        velocityObject.setLayoutX(velocityObject.getLayoutX() + dx);
        velocityObject.setLayoutY(velocityObject.getLayoutY() + dy);
//        wakePoly.setLayoutX(wakePoly.getLayoutX() + dx);
//        wakePoly.setLayoutY(wakePoly.getLayoutY() + dy);
        rotateTo(currentRotation);
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveTo (double x, double y, double rotation) {
        rotateTo(rotation);
        moveTo(x, y);
    }

    public void moveTo (double x, double y) {
        boatPoly.setLayoutX(x);
        boatPoly.setLayoutY(y);
        teamNameObject.setLayoutX(x);
        teamNameObject.setLayoutY(y);
        velocityObject.setLayoutX(x);
        velocityObject.setLayoutY(y);
//        wakePoly.setLayoutX(x);
//        wakePoly.setLayoutY(y);
    }

    public void updatePosition (double timeInterval) {
        double dx = pixelVelocityX * timeInterval;
        double dy = pixelVelocityY * timeInterval;
        double rotation = 0d;
        if (rotationalGoal > currentRotation && rotationalVelocity > 0) {
            rotation = rotationalVelocity * timeInterval;
        } else if (rotationalGoal < currentRotation && rotationalVelocity < 0) {
            rotation = rotationalVelocity * timeInterval;
        }
        moveGroupBy(dx, dy, rotation);
//        if (super.getChildren().size() > 3) {
//            for (Node wake : super.getChildren().subList(4, super.getChildren().size())) {
//                if (!((Wake) wake).updatePosition(timeInterval))
//                    super.getChildren().remove(wake);
//            }
//        }
        for (Wake wake : wakes) {
            if (wake.updatePosition(timeInterval)) {
                super.getChildren().remove(wake);
            }
        }
        if (wakeCounter-- == 0) {
//            if (boat.getShortName().equals("BAR"))
//                System.out.println("thinking");
            wakeCounter = WAKE_FRAME_INTERVAL;
            if (pixelVelocityX > 0 && pixelVelocityY > 0) {
//                super.getChildren().add(
//                        new Wake(
//                                super.getLayoutX() + BOAT_HEIGHT, super.getLayoutY() + BOAT_HEIGHT, pixelVelocityX, pixelVelocityY
//                        )
//                );
                Wake wake = new Wake(
                        boatPoly.getLayoutX(),
                        boatPoly.getLayoutY(),
                        pixelVelocityX,
                        pixelVelocityY, rotation);
//                wake.getTransforms().clear();
//                wake.getTransforms().add(new Rotate(rotation, 0,  0));
                super.getChildren().add(wake);
                wakes.add(wake);
            }

        }
        if (framesForNewLine == 0) {
            framesForNewLine = 121;
            if (lastPoint != null) {
                Line l = new Line(lastPoint.getX(), lastPoint.getY(), boatPoly.getLayoutX(), boatPoly.getLayoutY());
                l.getStrokeDashArray().setAll(4d, 4d);
                lines.add(l);
                super.getChildren().add(l);
            }
            lastPoint = new Point2D(boatPoly.getLayoutX(), boatPoly.getLayoutY());
        }
        framesForNewLine -= 1;
    }

    public void setDestination (double newXValue, double newYValue, double rotation, int... raceIds) {
        //System.out.println("MADE IT");
        if (hasRaceId(raceIds)) {
            this.pixelVelocityX = (newXValue - boatPoly.getLayoutX()) / expectedUpdateInterval;
            this.pixelVelocityY = (newYValue - boatPoly.getLayoutY()) / expectedUpdateInterval;
            this.rotationalGoal = rotation;
            calculateRotationalVelocity();
            rotateTo(rotation);
        }
    }

    public void setDestination (double newXValue, double newYValue, int... raceIDs) {
        if (hasRaceId(raceIDs)) {
            double rotation = Math.abs(
                    Math.toDegrees(
                            Math.atan(
                                    (newYValue - boatPoly.getLayoutY()) / (newXValue - boatPoly.getLayoutX())
                            )
                    )
            );

//            if (boatPoly.getLayoutY() >= newYValue && boatPoly.getLayoutX() <= newXValue)
//                rotation = 90 - rotation;
//            else if (boatPoly.getLayoutY() < newYValue && boatPoly.getLayoutX() <= newXValue)
//                rotation = 90 + rotation;
//            else if (boatPoly.getLayoutY() >= newYValue && boatPoly.getLayoutX() > newXValue)
//                rotation = 270 + rotation;
//            else
//                rotation = 270 - rotation;
            setDestination(newXValue, newYValue, rotation, raceIDs);
        }
    }

    public void rotateTo (double rotation) {
        if(rotation != 0) {
            rotationalGoal = rotation;
            boatPoly.getTransforms().clear();
            boatPoly.getTransforms().add(new Rotate(rotation, BOAT_WIDTH / 2, 0));
        }
//        wakePoly.getTransforms().clear();
//        wakePoly.getTransforms().add(new Rotate(rotation, 0, 0));
    }

    public void forceRotation () {
        rotateTo (rotationalGoal);
    }

    public void toggleAnnotations () {
        teamNameObject.setVisible(!teamNameObject.isVisible());
        velocityObject.setVisible(!velocityObject.isVisible());
        for (Wake wake : wakes) {
            wake.setVisible(!wake.isVisible());
        }
        for (Line line : lines) {
            line.setVisible(!line.isVisible());
        }
    }

    public Boat getBoat() {
        return boat;
    }

    public boolean hasRaceId (int... raceIds) {
        for (int id : raceIds) {
            if (id == boat.getId())
                return true;
        }
        return false;
    }

    public int[] getRaceIds () {
        return new int[] {boat.getId()};
    }
}
