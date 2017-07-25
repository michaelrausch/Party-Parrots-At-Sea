package seng302.models;

import static seng302.utilities.GeoUtility.getGeoCoordinate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import javafx.scene.paint.Color;
import seng302.controllers.RaceViewController;
import seng302.gameServer.GameState;
import seng302.models.mark.Mark;
import seng302.utilities.GeoPoint;

/**
 * Yacht class for the racing boat.
 *
 * Class created to store more variables (eg. boat statuses) compared to the XMLParser boat class,
 * also done outside Boat class because some old variables are not used anymore.
 */
public class Yacht {

    private final Double TURN_STEP = 2.0;

    private Double lastHeading;
    private Boolean sailIn;


    // Used in boat group
    private Color colour;

    private String boatType;
    private Integer sourceId;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;

    // Situational data


    // Boat status
    private Integer boatStatus;
    private Integer legNumber;
    private Integer penaltiesAwarded;
    private Integer penaltiesServed;
    private Long estimateTimeAtFinish;
    private String position;
    private GeoPoint location;
    private Double heading;
    private Double velocity;
    private Long timeTillNext;
    private Long markRoundTime;

    // Mark rounding
    private Mark lastMarkRounded;
    private Mark nextMark;


    /**
     * @param location latlon location of the boat stored in a geopoint
     * @param heading heading of the boat in degrees from 0 to 365 with 0 being north
     */
    public Yacht(GeoPoint location, Double heading) {
        this.location = location;
        this.heading = heading;
        this.velocity = 0.0;
        this.sailIn = false;
    }


    /**
     * Used in EventTest and RaceTest.
     *
     * @param boatName Create a yacht object with name.
     */
    public Yacht(String boatName, String shortName, GeoPoint location, Double heading) {
        this.boatName = boatName;
        this.shortName = shortName;
        this.location = location;
        this.heading = heading;
        this.velocity = 0.0;
        this.sailIn = false;
    }

    /**
     * Used in BoatGroupTest.
     *
     * @param boatName The name of the team sailing the boat
     * @param boatVelocity The speed of the boat in meters/second
     * @param shortName A shorter version of the teams name
     */
    public Yacht(String boatName, double boatVelocity, String shortName, int id) {
        this.boatName = boatName;
        this.velocity = boatVelocity;
        this.shortName = shortName;
        this.sourceId = id;
        this.sailIn = false;
    }


    public Yacht(String boatType, Integer sourceId, String hullID, String shortName,
            String boatName, String country) {
        this.boatType = boatType;
        this.sourceId = sourceId;
        this.hullID = hullID;
        this.shortName = shortName;
        this.boatName = boatName;
        this.country = country;
        this.position = "-";
        this.sailIn = false;
        this.location = new GeoPoint(57.670341, 11.826856);
        this.heading = 120.0;
        this.velocity = 50000.0;
    }

    /**
     * @param timeInterval since last update in milliseconds
     */
    public void update(Long timeInterval) {
        if (sailIn) {
            Double secondsElapsed = timeInterval / 1000000.0;
            Double metersCovered = velocity * secondsElapsed;
            location = getGeoCoordinate(location, heading, metersCovered);
        }
    }

    /**
     * Adjusts the yachts velocity based on the wind direction and speed from the polar table.
     *
     * @param windDir current wind Direction TODO: 20/07/17 ajm412: (TWA or AWA, not 100% sure?)
     * @param windSpd current wind Speed
     */
    public void updateYachtVelocity(Double windDir, Double windSpd) {
        Double closestSpd = PolarTable.getClosestMatch(windSpd);
        Map<Double, Double> polarsFromClosestSpd = PolarTable.getPolarTable().get(closestSpd);

        Double closest = 0d;
        Double closest_key = 0d;

        for (Double key : polarsFromClosestSpd.keySet()) {
            Double difference = Math.abs(key - windDir);
            if (difference <= closest) {
                closest = difference;
                closest_key = key;
            }
        }
//        System.out.println("Closest angle " + closest_key);
//        System.out.println("WindDir " + windDir);
        velocity = polarsFromClosestSpd.get(closest_key);
    }

    public Double getHeading() {
        return heading;
    }

    public void adjustHeading(Double amount) {
        lastHeading = heading;
        // TODO: 24/07/17 wmu16 - '%' in java does remainder, we need modulo. All this must be changed here, this is why we have neg values!
        heading = (heading + amount) % 360.0;
    }

    public void tackGybe(Double windDirection) {
        adjustHeading(-2 * ((heading - windDirection) % 360));
    }

    public void toggleSailIn() {
        sailIn = !sailIn;
    }

    public void turnUpwind() {
        Double normalizedHeading = (heading - GameState.windDirection) % 360;
        if (normalizedHeading == 0) {
            if (lastHeading < 180) {
                adjustHeading(-TURN_STEP);
            } else {
                adjustHeading(TURN_STEP);
            }
        } else if (normalizedHeading == 180) {
            if (lastHeading < 180) {
                adjustHeading(TURN_STEP);
            } else {
                adjustHeading(-TURN_STEP);
            }
        } else if (normalizedHeading < 180) {
            adjustHeading(-TURN_STEP);
        } else {
            adjustHeading(TURN_STEP);
        }
    }

    public void turnDownwind() {
        Double normalizedHeading = (heading - GameState.windDirection) % 360;
        if (normalizedHeading == 0) {
            if (lastHeading < 180) {
                adjustHeading(TURN_STEP);
            } else {
                adjustHeading(-TURN_STEP);
            }
        } else if (normalizedHeading == 180) {
            if (lastHeading < 180) {
                adjustHeading(-TURN_STEP);
            } else {
                adjustHeading(TURN_STEP);
            }
        } else if (normalizedHeading < 180) {
            adjustHeading(TURN_STEP);
        } else {
            adjustHeading(-TURN_STEP);
        }
    }


    public String getBoatType() {
        return boatType;
    }

    public Integer getSourceId() {
        //@TODO Remove and merge with Creating Game Loop
        if (sourceId == null) return 0;
        return sourceId;
    }

    public String getHullID() {
        if (hullID == null) return "";
        return hullID;
    }

    public String getShortName() {
        return shortName;
    }

    public String getBoatName() {
        return boatName;
    }

    public String getCountry() {
        if (country == null) return "";
        return country;
    }

    public Integer getBoatStatus() {
        return boatStatus;
    }

    public void setBoatStatus(Integer boatStatus) {
        this.boatStatus = boatStatus;
    }

    public Integer getLegNumber() {
        return legNumber;
    }

    public void setLegNumber(Integer legNumber) {
        if (colour != null  && position != "-" && legNumber != this.legNumber&& RaceViewController.sparkLineStatus(
            sourceId)) {
            RaceViewController.updateYachtPositionSparkline(this, legNumber);
        }
        this.legNumber = legNumber;
    }

    public Integer getPenaltiesAwarded() {
        return penaltiesAwarded;
    }

    public void setPenaltiesAwarded(Integer penaltiesAwarded) {
        this.penaltiesAwarded = penaltiesAwarded;
    }

    public Integer getPenaltiesServed() {
        return penaltiesServed;
    }

    public void setPenaltiesServed(Integer penaltiesServed) {
        this.penaltiesServed = penaltiesServed;
    }

    public void setEstimateTimeAtNextMark(Long estimateTimeAtNextMark) {
        timeTillNext = estimateTimeAtNextMark;
    }

    public String getEstimateTimeAtFinish() {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(estimateTimeAtFinish);
    }

    public void setEstimateTimeAtFinish(Long estimateTimeAtFinish) {
        this.estimateTimeAtFinish = estimateTimeAtFinish;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }


    public void setMarkRoundingTime(Long markRoundingTime) {
        this.markRoundTime = markRoundingTime;
    }

    public double getVelocity() {
        return velocity;
    }

    public Long getTimeTillNext() {
        return timeTillNext;
    }

    public Long getMarkRoundTime() {
        return markRoundTime;
    }

    public Mark getLastMarkRounded() {
        return lastMarkRounded;
    }

    public void setLastMarkRounded(Mark lastMarkRounded) {
        this.lastMarkRounded = lastMarkRounded;
    }

    public void setNextMark(Mark nextMark) {
        this.nextMark = nextMark;
    }

    public Mark getNextMark(){
        return nextMark;
    }

    public Boolean getSailIn() {
        return sailIn;
    }

    @Override
    public String toString() {
        return boatName;
    }

    public GeoPoint getLocation() {
        return location;
    }

}
