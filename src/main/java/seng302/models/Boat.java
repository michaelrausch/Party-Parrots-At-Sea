package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
* Represents a boat in the race.
*/
public class Boat {

    private static final double TEAMNAME_X_OFFSET = 15d;
    private static final double TEAMNAME_Y_OFFSET = -20d;
    private static final double VELOCITY_X_OFFSET = 15d;
    private static final double VELOCITY_Y_OFFSET = -10d;
    private static final double VELOCITY_WAKE_RATIO = 2d;             //Ratio for deciding how long the wake will be wrt velocity
    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;

    private String teamName; // The name of the team, this is also the name of the boat
    private double velocity; // In meters/second
    private double lat; // Boats position
    private double lon; // -
    private double distanceToNextMark;
    private Color color;
    private int markLastPast;
    private double heading;
    private String shortName;

    //Graphical
    private Polygon boatObject;
    private Polygon wake;
    private Text teamNameObject;
    private Text velocityObject;

    public Boat(String teamName) {
        this.teamName = teamName;
        this.velocity = 10; // Default velocity
        this.lat = 0.0;
        this.lon = 0.0;
        this.distanceToNextMark = 0.0;
        this.shortName = "";
    }

    /**
     * Represents a boat in the race.
     *
     * @param teamName     The name of the team sailing the boat
     * @param boatVelocity The speed of the boat in meters/second
     * @param shortName    A shorter version of the teams name
     */
    public Boat(String teamName, double boatVelocity, String shortName) {
        this.teamName = teamName;
        this.velocity = boatVelocity;
        this.distanceToNextMark = 0.0;
        this.color = Colors.getColor();
        this.shortName = shortName;
        this.boatObject =  new Polygon();
        this.boatObject.getPoints().addAll(BOAT_WIDTH /2,0.0,
                BOAT_WIDTH, BOAT_HEIGHT,
                0.0, BOAT_HEIGHT);
        createWake();
        this.teamNameObject = new Text(shortName);
        this.velocityObject = new Text(Double.toString(boatVelocity) + "ms");
    }

    /**
     * Returns the name of the team sailing the boat
     *
     * @return The name of the team
     */
    public String getTeamName() {
        return this.teamName;
    }

    /**
     * Sets the name of the team sailing the boat
     *
     * @param teamName The name of the team
     */
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    /**
     * Gets velocity of the boat
     *
     * @return a float number of the boat velocity
     */
    public double getVelocity() {
        return this.velocity;
    }

    /**
     * Sets velocity of the boat
     *
     * @param velocity The velocity of boat
     */
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    /**
     * Sets the boats location
     *
     * @param lat, the boats latitude
     * @param lon, the boats longitude
     */
    public void setLocation(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public void setDistanceToNextMark(double distance){
        this.distanceToNextMark = distance;
    }

    public double getLatitude(){
        return this.lat;
    }

    public double getLongitude(){
        return this.lon;
    }

    public Color getColor() {
        return color;
    }

    public double getSpeedInKnots(){
        return Math.round((this.velocity * 1.94384) * 100d) / 100d;
    }

    public void setMarkLastPast(int markLastPast) {
        this.markLastPast = markLastPast;
    }

    public int getMarkLastPast() {
        return markLastPast;
    }

    public void setHeading(double heading){
        boatObject.getTransforms().clear();
        wake.getTransforms().clear();
        wake.getTransforms().add(new Translate(0, BOAT_HEIGHT));
        wake.getTransforms().add(new Rotate(heading, BOAT_WIDTH/2, -BOAT_HEIGHT));
        boatObject.getTransforms().add(new Rotate(heading, BOAT_WIDTH/2, 0));
        this.heading = heading;
    }

    public double getHeading(){
        return this.heading;
    }

    public String getShortName(){
        return this.shortName;
    }


    /**
     * Moves the boat and its children annotations from its current coordinates by specified amounts.
     * @param x The amount to move the X coordinate by
     * @param y The amount to move the Y coordinate by
     */
    void moveBoatBy(Double x, Double y) {
        boatObject.setLayoutX(boatObject.getLayoutX() + x);
        boatObject.setLayoutY(boatObject.getLayoutY() + y);
        boatObject.relocate(boatObject.getLayoutX(), boatObject.getLayoutY());

        teamNameObject.setX(teamNameObject.getX() + x);
        teamNameObject.setY(teamNameObject.getY() + y);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(velocityObject.getX() + x);
        velocityObject.setY(velocityObject.getY() + y);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());


        wake.setLayoutX(wake.getLayoutX() + x);
        wake.setLayoutY(wake.getLayoutY() + y);
        wake.relocate(wake.getLayoutX(), wake.getLayoutY());
    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveBoatTo(Double x, Double y) {
        boatObject.setLayoutX(x);
        boatObject.setLayoutY(y);
        boatObject.relocate(boatObject.getLayoutX(), boatObject.getLayoutY());

        teamNameObject.setX(x + TEAMNAME_X_OFFSET);
        teamNameObject.setY(y + TEAMNAME_Y_OFFSET);
        teamNameObject.relocate(teamNameObject.getX(), teamNameObject.getY());

        velocityObject.setX(x + VELOCITY_X_OFFSET);
        velocityObject.setY(y + VELOCITY_Y_OFFSET);
        velocityObject.relocate(velocityObject.getX(), velocityObject.getY());

        wake.setLayoutX(x);
        wake.setLayoutY(y);
        wake.relocate(wake.getLayoutX(), wake.getLayoutY());
    }

    private void createWake(){
        wake = new Polygon();
        wake.setFill(Color.LIGHTSKYBLUE);
        wake.getPoints().addAll(5.0,0.0,
                10.0, velocity * VELOCITY_WAKE_RATIO,
                0.0, velocity * VELOCITY_WAKE_RATIO);
    }

    public Polygon getWake() {
        return wake;
    }

    public Polygon getBoatObject() {
        return boatObject;
    }

    public Text getTeamNameObject() {
        return teamNameObject;
    }

    public Text getVelocityObject() {
        return velocityObject;
    }
}