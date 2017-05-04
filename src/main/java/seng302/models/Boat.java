package seng302.models;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Pair;

/**
* Represents a boat in the race.
*/
public class Boat {

    private String teamName;
    private double velocity;
    private double lat;
    private double lon;
    private double heading;
    private int markLastPast;
    private String shortName;
    private int id;

    /**
     * For testing only.
     * @param teamName Boat team name.
     */
    public Boat(String teamName) {
        this.teamName = teamName;
        this.velocity = 10; // Default velocity
        this.lat = 0.0;
        this.lon = 0.0;
        this.shortName = "";
    }


    /**
     * Represents a boat in the race.
     *
     * @param teamName     The name of the team sailing the boat
     * @param boatVelocity The speed of the boat in meters/second
     * @param shortName    A shorter version of the teams name
     */
    public Boat(String teamName, double boatVelocity, String shortName, int id) {
        this.teamName = teamName;
        this.velocity = boatVelocity;
        this.shortName = shortName;
        this.id = id;
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

    public Pair<Double, Double> getLocation ()
    {
        return new Pair<>(this.lat, this.lon);
    }

    public double getLatitude(){
        return this.lat;
    }

    public double getLongitude(){
        return this.lon;
    }

    public void setLatitude (double latitude) {
        this.lat = latitude;
    }

    public void setlongitude (double longitude) {
        this.lon =longitude;
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

    public double getHeading(){
        return this.heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public String getShortName(){
        return this.shortName;
    }

    public int getId() {
        return id;
    }
}