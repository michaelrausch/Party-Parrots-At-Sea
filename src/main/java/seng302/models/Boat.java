package seng302.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

/**
* Represents a boat in the race.
*/
public class Boat {

    private static final double BOAT_HEIGHT = 15d;
    private static final double BOAT_WIDTH = 10d;
    private static final double VELOCITY_WAKE_RATIO = 1/2d;             //Ratio for deciding how long the wake will be wrt velocity

    private String teamName; // The name of the team, this is also the name of the boat
    private Double velocity; // In meters/second
    private Double lat; // Boats position
    private Double lon; // -
    private Double legDistance;
    private Color color;
    private Leg currentLeg;
    private Double heading;
    private String shortName;

    private Polygon boatObject = new Polygon();

    public Boat(String teamName) {
        this.teamName = teamName;
        this.velocity = 10d; // Default velocity
        this.lat = 0.0;
        this.lon = 0.0;
        this.legDistance = 0.0;
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
        this.legDistance = 0.0;
        this.color = Colors.getColor();
        this.shortName = shortName;
        this.boatObject.getPoints().addAll(BOAT_WIDTH /2,0.0,
                BOAT_WIDTH, BOAT_HEIGHT,
                0.0, BOAT_HEIGHT);
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

    public Double getLegDistance() {
        return legDistance;
    }

    public void setLegDistance(double legDistance){
        this.legDistance = legDistance;
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

    public Leg getCurrentLeg() {
        return currentLeg;
    }

    public void setCurrentLeg(Leg currentLeg) {
        this.currentLeg = currentLeg;
    }

    public void setHeading(double heading){
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

    }

    /**
     * Moves the boat and its children annotations to coordinates specified
     * @param x The X coordinate to move the boat to
     * @param y The Y coordinate to move the boat to
     */
    public void moveBoatTo(int x, int y) {
        boatObject.setLayoutX(x);
        boatObject.setLayoutY(y);
        boatObject.relocate(boatObject.getLayoutX(), boatObject.getLayoutY());

    }

    public Polygon getBoatObject() {
        return boatObject;
    }
}