package seng302.models;

import javafx.scene.paint.Color;

/**
* Represents a boat in the race.
*/
public class Boat {

    private String teamName; // The name of the team, this is also the name of the boat
    private double velocity; // In meters/second
    private double lat; // Boats position
    private double lon; // -
    private double distanceToNextMark;
    private Color color;

    public Boat(String teamName) {
        this.teamName = teamName;
        this.velocity = 10; // Default velocity
        this.lat = 0.0;
        this.lon = 0.0;
        this.distanceToNextMark = 0.0;
        this.color = Colors.getColor();
    }

    /**
     * Represents a boat in the race.
     *
     * @param teamName     The name of the team sailing the boat
     * @param boatVelocity The speed of the boat in meters/second
     */
    public Boat(String teamName, double boatVelocity) {
        this.teamName = teamName;
        this.velocity = boatVelocity;
        this.distanceToNextMark = 0.0;
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
}