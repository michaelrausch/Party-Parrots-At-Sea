package seng302.models;


import com.sun.xml.internal.bind.v2.TODO;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Created by cir27 on 24/04/17.
 */
public class BoatPolygon extends Polygon {

    private static final double TEAMNAME_X_OFFSET = 15d;
    private static final double TEAMNAME_Y_OFFSET = -20d;
    private static final double VELOCITY_X_OFFSET = 15d;
    private static final double VELOCITY_Y_OFFSET = -10d;
    private static final double VELOCITY_WAKE_RATIO = 2d;
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    //Time between sections of race - Should be changed to 200 for actual program.
    private static double expectedUpdateInterval = 5000;

    private Boat boat;
    private Polygon wake;
    private Text teamNameObject;
    private Text velocityObject;

    private double rotationalGoal;
    private double currentRotation;
    private double rotationalVelocity;
    private double pixelVelocityX;
    private double pixelVelocityY;
    //private double destinationX;
    //private double destinationY;

    public BoatPolygon (Boat boat, Color color){
        super();
        super.setFill(color);
        super.getPoints().addAll(
                BOAT_WIDTH / 2, 0.0,
                BOAT_WIDTH    , BOAT_HEIGHT,
                0.0           , BOAT_HEIGHT
        );
        this.boat = boat;
        initAnnotations();
    }

    public BoatPolygon (Boat boat, Color color, double... points)
    {
        super(points);
        super.setFill(color);
        this.boat = boat;
        initAnnotations();
    }

    private void initAnnotations ()
    {
        wake = new Polygon();
        wake.setFill(Color.DARKBLUE);
        wake.getPoints().addAll(
                5.0,0.0,
                10.0, boat.getVelocity() * VELOCITY_WAKE_RATIO,
                0.0, boat.getVelocity() * VELOCITY_WAKE_RATIO
        );
        teamNameObject = new Text(boat.getShortName());
        velocityObject = new Text(String.valueOf(boat.getVelocity()));
    }
    /**
     * Moves the boat and its children annotations from its current coordinates by specified amounts.
     * @param dx The amount to move the X coordinate by
     * @param dy The amount to move the Y coordinate by
     */
    void moveBy(Double dx, Double dy, Double rotation) {
        super.setLayoutX(super.getLayoutX() + dx);
        super.setLayoutY(super.getLayoutY() + dy);
        super.relocate(super.getLayoutX(), super.getLayoutY());

        teamNameObject.setX(teamNameObject.getX() + dx);
        teamNameObject.setY(teamNameObject.getY() + dy);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(velocityObject.getX() + dx);
        velocityObject.setY(velocityObject.getY() + dy);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());

        wake.setLayoutX(wake.getLayoutX() + dx);
        wake.setLayoutY(wake.getLayoutY() + dy);
        wake.relocate(wake.getLayoutX(), wake.getLayoutY());
        rotateBoat(rotation);
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveBoatTo(Double x, Double y, Double rotation) {
        super.setLayoutX(x);
        super.setLayoutY(y);
        super.relocate(super.getLayoutX(), super.getLayoutY());

        teamNameObject.setX(x + TEAMNAME_X_OFFSET);
        teamNameObject.setY(y + TEAMNAME_Y_OFFSET);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(x + VELOCITY_X_OFFSET);
        velocityObject.setY(y + VELOCITY_Y_OFFSET);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());

        wake.setLayoutX(x);
        wake.setLayoutY(y);
        wake.relocate(wake.getLayoutX(), wake.getLayoutY());
        currentRotation = 0;
        rotateBoat(rotation);
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
        moveBy(dx, dy, rotation);
    }

    public void setDestination (double newXValue, double newYValue) {
        this.pixelVelocityX = (newXValue - super.getLayoutX()) / expectedUpdateInterval;
        this.pixelVelocityY = (newYValue - super.getLayoutY()) / expectedUpdateInterval;
        //this.destinationX = newXValue;
        //this.destinationY = newYValue;
        this.rotationalGoal = Math.abs(
                Math.toDegrees(
                        Math.atan(
                                (newYValue - super.getLayoutY()) / (newXValue - super.getLayoutX())
                        )
                )
        );
        if (super.getLayoutY() >= newYValue && super.getLayoutX() <= newXValue)
            rotationalGoal = 90 - rotationalGoal;
        else if (super.getLayoutY() < newYValue && super.getLayoutX() <= newXValue)
            rotationalGoal = 90 + rotationalGoal;
        else if (super.getLayoutY() >= newYValue && super.getLayoutX() > newXValue)
            rotationalGoal = 270 + rotationalGoal;
        else
            rotationalGoal = 270 - rotationalGoal;
        // TODO: 25/04/2017 cir27 - Verify this logic is correct. Want to produce the shortest path.
        if (Math.abs(360 - rotationalGoal + currentRotation) < Math.abs(rotationalGoal - currentRotation)) {
            System.out.println("ROTATE");
            this.rotationalVelocity = (360 - rotationalGoal + currentRotation) / expectedUpdateInterval;
        } else {
            this.rotationalVelocity = (rotationalGoal - currentRotation) / expectedUpdateInterval;
        }
    }

    public void rotateBoat (double rotationDeg) {
        currentRotation += rotationDeg;
        super.getTransforms().clear();
        super.getTransforms().add(new Rotate(currentRotation, BOAT_WIDTH/2, 0));
        wake.getTransforms().clear();
        wake.getTransforms().add(new Translate(0, BOAT_HEIGHT));
        wake.getTransforms().add(new Rotate(currentRotation, BOAT_WIDTH/2, -BOAT_HEIGHT));
    }

    public static double getExpectedUpdateInterval() {
        return expectedUpdateInterval;
    }

    public static void setExpectedUpdateInterval(double expectedUpdateInterval) {
        BoatPolygon.expectedUpdateInterval = expectedUpdateInterval;
    }

    public Polygon getWake() {
        return wake;
    }

    public Text getTeamNameObject() {
        return teamNameObject;
    }

    public Text getVelocityObject() {
        return velocityObject;
    }

    public void forceRotation () {
        rotateBoat (rotationalGoal - currentRotation);
    }
}
