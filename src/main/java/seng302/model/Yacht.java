package seng302.model;

import static seng302.utilities.GeoUtility.getGeoCoordinate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.scene.paint.Color;
import seng302.gameServer.GameState;
import seng302.model.mark.Mark;

/**
 * Yacht class for the racing boat.
 *
 * Class created to store more variables (eg. boat statuses) compared to the XMLParser boat class,
 * also done outside Boat class because some old variables are not used anymore.
 */
public class Yacht {

    @FunctionalInterface
    public interface YachtLocationListener {
        void notifyLocation(Yacht yacht, double lat, double lon, double heading, double velocity);
    }

    @FunctionalInterface
    public interface YachtPositionListener {
        void notifyPosition(int position);
    }

    //BOTH AFAIK
    private String boatType;
    private Integer sourceId;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;

    private Long estimateTimeAtFinish;
    private Long timeTillNext;
    private Long markRoundTime;
    private Double heading;
    private Double lat;
    private Double lon;
    private Integer legNumber = 0;

    //SERVER SIDE
    private final Double TURN_STEP = 5.0;
    private Double lastHeading;
    private Boolean sailIn;
    private String position;
    private GeoPoint location;
    private Integer boatStatus;
    private Double velocity;

    //CLIENT SIDE
    private List<YachtLocationListener> locationListeners = new ArrayList<>();
    private ReadOnlyDoubleWrapper velocityProperty = new ReadOnlyDoubleWrapper();
    private ReadOnlyLongWrapper timeTillNextProperty = new ReadOnlyLongWrapper();
    private ReadOnlyLongWrapper timeSinceLastMarkProperty = new ReadOnlyLongWrapper();
//    private ReadOnlyDoubleWrapper headingProperty = new ReadOnlyDoubleWrapper();
//    private ReadOnlyDoubleWrapper latitudeProperty = new ReadOnlyDoubleWrapper();
//    private ReadOnlyDoubleWrapper longitudeProperty = new ReadOnlyDoubleWrapper();
    private Mark lastMarkRounded;
    private Mark nextMark;
    private Integer positionInt = 0;
    private Color colour;


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
        this.heading = 120.0;   //In degrees
        this.velocity = 0d;     //in mms-1
    }

    /**
     * @param timeInterval since last update in milliseconds
     */
    public void update(Long timeInterval) {

        Double secondsElapsed = timeInterval / 1000000.0;
        Double windSpeedKnots = GameState.getWindSpeedKnots();
        Double trueWindAngle = Math.abs(GameState.getWindDirection() - heading);
        Double boatSpeedInKnots = PolarTable.getBoatSpeed(windSpeedKnots, trueWindAngle);
        Double maxBoatSpeed = boatSpeedInKnots / 1.943844492 * 1000;
        if (sailIn && velocity <= maxBoatSpeed && maxBoatSpeed != 0d) {

            if (velocity < maxBoatSpeed) {
                velocity += maxBoatSpeed / 15;  // Acceleration
            }
            if (velocity > maxBoatSpeed) {
                velocity = maxBoatSpeed;        // Prevent the boats from exceeding top speed
            }

        } else { // Deceleration

            if (velocity > 0d) {
                if (maxBoatSpeed != 0d) {
                    velocity -= maxBoatSpeed / 600;
                } else {
                    velocity -= velocity / 100;
                }
                if (velocity < 0) {
                    velocity = 0d;
                }
            }
        }
//        if (sailIn) {
//            Double secondsElapsed = timeInterval / 1000000.0;
//            Double windSpeedKnots = GameState.getWindSpeedKnots();
//            Double trueWindAngle = Math.abs(GameState.getWindDirection() - heading);
//            Double boatSpeedInKnots = PolarTable.getBoatSpeed(windSpeedKnots, trueWindAngle);
//            velocity = boatSpeedInKnots / 1.943844492 * 1000; // TODO: 26/07/17 cir27 - Remove magic number
//            Double metersCovered = velocity * secondsElapsed;
//            location = getGeoCoordinate(location, heading, metersCovered);
//        } else {
//            velocity = 0d;
//        }
        Double metersCovered = velocity * secondsElapsed;
        location = getGeoCoordinate(location, heading, metersCovered);
    }

    public void adjustHeading(Double amount) {
        Double newVal = heading + amount;
        lastHeading = heading;
        // TODO: 24/07/17 wmu16 - '%' in java does remainder, we need modulo. All this must be changed here, this is why we have neg values!
        heading = (double) Math.floorMod(newVal.longValue(), 360L);
    }

    public void tackGybe(Double windDirection) {
        Double normalizedHeading = normalizeHeading();
        adjustHeading(-2 * normalizedHeading);
    }

    public void toggleSailIn() {
        sailIn = !sailIn;
    }

    public void turnUpwind() {
        Double normalizedHeading = normalizeHeading();
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
        Double normalizedHeading = normalizeHeading();
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

    public void turnToVMG() {
        Double normalizedHeading = normalizeHeading();
        Double optimalHeading;
        HashMap<Double, Double> optimalPolarMap;

        if (normalizedHeading >= 90 && normalizedHeading <= 270) { // Downwind
            optimalPolarMap = PolarTable.getOptimalDownwindVMG(GameState.getWindSpeedKnots());
            optimalHeading = optimalPolarMap.keySet().iterator().next();
        } else {
            optimalPolarMap = PolarTable.getOptimalUpwindVMG(GameState.getWindSpeedKnots());
            optimalHeading = optimalPolarMap.keySet().iterator().next();
        }
        // Take optimal heading and turn into correct
        optimalHeading =
            optimalHeading + (double) Math.floorMod(GameState.getWindDirection().longValue(), 360L);

        turnTowardsHeading(optimalHeading);

    }

    private void turnTowardsHeading(Double newHeading) {
        System.out.println(newHeading);
        if (heading < 90 && newHeading > 270) {
            adjustHeading(-TURN_STEP);
        } else {
            if (heading < newHeading) {
                adjustHeading(TURN_STEP);
            } else {
                adjustHeading(-TURN_STEP);
            }
        }
    }

    private Double normalizeHeading() {
        Double normalizedHeading = heading - GameState.windDirection;
        normalizedHeading = (double) Math.floorMod(normalizedHeading.longValue(), 360L);
        return normalizedHeading;
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
//        if (colour != null  && position != "-" && legNumber != this.legNumber) {
//            RaceViewController.updateYachtPositionSparkline(this, legNumber);
//        }
        this.legNumber = legNumber;
    }

    public void setEstimateTimeTillNextMark(Long estimateTimeTillNextMark) {
        timeTillNext = estimateTimeTillNextMark;
    }

    public String getEstimateTimeAtFinish() {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return format.format(estimateTimeAtFinish);
    }

    public void setEstimateTimeAtFinish(Long estimateTimeAtFinish) {
        this.estimateTimeAtFinish = estimateTimeAtFinish;
    }

    public Integer getPositionInteger() {
        return positionInt;
    }

    public void setPositionInteger(Integer position) {
        this.positionInt = position;
    }

    public void updateVelocityProperty(double velocity) {
        this.velocityProperty.set(velocity);
    }

    public void setMarkRoundingTime(Long markRoundingTime) {
        this.markRoundTime = markRoundingTime;
    }

    public ReadOnlyDoubleProperty getVelocityProperty() {
        return velocityProperty.getReadOnlyProperty();
    }

    public double getVelocityMMS() {
        return velocity;
    }

    public ReadOnlyLongProperty timeTillNextProperty() {
        return timeTillNextProperty.getReadOnlyProperty();
    }

    public Double getVelocityKnots() {
        return velocity / 1000 * 1.943844492; // TODO: 26/07/17 cir27 - remove magic number
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
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

    public void updateTimeSinceLastMarkProperty(long timeSinceLastMark) {
        this.timeSinceLastMarkProperty.set(timeSinceLastMark);
    }

    public ReadOnlyLongProperty timeSinceLastMarkProperty () {
        return timeSinceLastMarkProperty.getReadOnlyProperty();
    }

    public void setTimeTillNext(Long timeTillNext) {
        this.timeTillNext = timeTillNext;
    }


    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }


    public Double getVelocity() {
        return velocity;
    }

    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

//    public void updateLatitudeProperty (Double lat) {
//        latitudeProperty.set(lat);
//    }
//
//    public void updateLongitudeProperty (double lon) {
//        longitudeProperty.set(lon);
//    }
//
//    public void updateHeadingProperty (double heading) {
//        headingProperty.set(heading);
//    }
//
//    public ReadOnlyDoubleProperty latitudeProperty () {
//        return latitudeProperty.getReadOnlyProperty();
//    }
//
//    public ReadOnlyDoubleProperty longitudeProperty () {
//        return longitudeProperty.getReadOnlyProperty();
//    }
//
//    public ReadOnlyDoubleProperty headingProperty () {
//        return headingProperty;
//    }

    public void updateLocation (double lat, double lon, double heading, double velocity) {
        this.lat = lat;
        this.lon = lon;
        this.heading = heading;
        this.velocity = velocity;
        updateVelocityProperty(velocity);
        for (YachtLocationListener yll : locationListeners) {
            yll.notifyLocation(this, lat, lon, heading, velocity);
        }
    }

    public void addLocationListener (YachtLocationListener listener) {
        locationListeners.add(listener);
    }

    public void addPositionListener (YachtPositionListener listener) {

    }
}
