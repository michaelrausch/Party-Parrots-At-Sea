package seng302.model;

import static seng302.utilities.GeoUtility.getGeoCoordinate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.scene.paint.Color;
import seng302.gameServer.GameState;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Mark;
import seng302.utilities.GeoUtility;

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

    private static final Double ROUNDING_DISTANCE = 15d; // TODO: 3/08/17 wmu16 - Look into this value further
    public static final Double MARK_COLLISION_DISTANCE = ROUNDING_DISTANCE - 8d;
    private static final Double BOUNCE_FACTOR = 0.0001;
    private static final Integer COLLISION_UPDATE_INTERVAL = 100;

    //BOTH AFAIK
    private String boatType;
    private Integer sourceId;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;

    private Long estimateTimeAtFinish;
    private Long lastMark;
    private Long markRoundTime;
    private Double distanceToNextMark;
    private Long timeTillNext;
    private CompoundMark nextMark;
    private Double heading;
    private Integer legNumber = 0;

    //SERVER SIDE
    private final Double TURN_STEP = 5.0;
    private Double lastHeading;
    private Boolean sailIn;
    private GeoPoint location;
    private Integer boatStatus;
    private Double velocity;
    //MARK ROUNDING INFO
    private GeoPoint lastLocation;  //For purposes of mark rounding calculations
    private Boolean hasEnteredRoundingZone; //The distance that the boat must be from the mark to round
    private Boolean hasPassedFirstLine; //The line extrapolated from the next mark to the current mark
    private Boolean hasPassedSecondLine; //The line extrapolated from the last mark to the current mark
    private Long lastCollisionUpdate;

    //CLIENT SIDE
    private List<YachtLocationListener> locationListeners = new ArrayList<>();
    private ReadOnlyDoubleWrapper velocityProperty = new ReadOnlyDoubleWrapper();
    private ReadOnlyLongWrapper timeTillNextProperty = new ReadOnlyLongWrapper();
    private ReadOnlyLongWrapper timeSinceLastMarkProperty = new ReadOnlyLongWrapper();
    private CompoundMark lastMarkRounded;
    private Integer positionInt = 0;
    private Color colour;

    public Yacht(String boatType, Integer sourceId, String hullID, String shortName,
        String boatName, String country) {
        this.boatType = boatType;
        this.sourceId = sourceId;
        this.hullID = hullID;
        this.shortName = shortName;
        this.boatName = boatName;
        this.country = country;
        this.sailIn = false;
        this.location = new GeoPoint(57.670341, 11.826856);
        this.lastLocation = location;
        this.heading = 120.0;   //In degrees
        this.velocity = 0d;     //in mms-1

        this.hasEnteredRoundingZone = false;
        this.hasPassedFirstLine = false;
        this.hasPassedSecondLine = false;
    }

    public Mark markCollidedWith(){
        Set<Mark> marksInRace = GameState.getMarks();

        for (Mark mark : marksInRace){
            if (GeoUtility.getDistance(getLocation(), new GeoPoint(mark.getLat(), mark.getLng())) <=  MARK_COLLISION_DISTANCE){
                return mark;
            }
        }

        return null;
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

        Double metersCovered = velocity * secondsElapsed;
        GeoPoint calculatedPoint = getGeoCoordinate(location, heading, metersCovered);

        if (shouldDoCollisionUpdate()){
            Yacht collidedYacht = checkCollision(calculatedPoint);

            if (collidedYacht != null || markCollidedWith() != null) {
                location = calculateBounceBack(new GeoPoint(markCollidedWith().getLat(), markCollidedWith().getLng()));
            } else {
                location = calculatedPoint;
            }

            lastCollisionUpdate = System.currentTimeMillis();
        }
        else {
            location = calculatedPoint;
        }

        //CHECK FOR MARK ROUNDING
        distanceToNextMark = calcDistanceToNextMark();
        if (distanceToNextMark < ROUNDING_DISTANCE) {
            hasEnteredRoundingZone = true;
        }

        // TODO: 3/08/17 wmu16 - Implement line cross check here
    }

    /**
     * @return true if COLLISION_UPDATE_INTERVAL has elapsed since the last collision update
     */
    private Boolean shouldDoCollisionUpdate(){
        if (lastCollisionUpdate == null) {
            lastCollisionUpdate = System.currentTimeMillis();
        }

        return System.currentTimeMillis() - lastCollisionUpdate > COLLISION_UPDATE_INTERVAL;
    }

    /**
     * Calculate the new position of the boat after it has had a collision
     * @return The boats new position
     */
    private GeoPoint calculateBounceBack(GeoPoint collidedWith){
        Double lat = location.getLat();
        Double lon = location.getLng();

        Double heading = GeoUtility.getBearing(location, collidedWith);


        if (heading >= 0 && heading <= 180){
            lat -= BOUNCE_FACTOR;
        }
        else{
            lat += BOUNCE_FACTOR;
        }

        if (heading >= 90 && heading <= 360-90){
            lon += BOUNCE_FACTOR;
        }
        else{
            lon -= BOUNCE_FACTOR;
        }

        return new GeoPoint(lat, lon);
    }


    /**
     * Calculates the distance to the next mark (closest of the two if a gate mark).
     *
     * @return A distance in metres. Returns -1 if there is no next mark
     */
    public Double calcDistanceToNextMark() {
        if (nextMark == null) {
            return -1d;
        } else if (nextMark.isGate()) {
            Mark sub1 = nextMark.getSubMark(1);
            Mark sub2 = nextMark.getSubMark(2);
            Double distance1 = GeoUtility.getDistance(location, sub1);
            Double distance2 = GeoUtility.getDistance(location, sub2);
            return (distance1 < distance2) ? distance1 : distance2;
        } else {
            return GeoUtility.getDistance(location, nextMark.getSubMark(1));
        }
    }

    public void adjustHeading(Double amount) {
        Double newVal = heading + amount;
        lastHeading = heading;
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
        if (sourceId == null) {
            return 0;
        }
        return sourceId;
    }

    public String getHullID() {
        if (hullID == null) {
            return "";
        }
        return hullID;
    }

    public String getShortName() {
        return shortName;
    }

    public String getBoatName() {
        return boatName;
    }

    public String getCountry() {
        if (country == null) {
            return "";
        }
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

    public CompoundMark getLastMarkRounded() {
        return lastMarkRounded;
    }

    public void setLastMarkRounded(CompoundMark lastMarkRounded) {
        this.lastMarkRounded = lastMarkRounded;
    }

    public void setNextMark(CompoundMark nextMark) {
        this.nextMark = nextMark;
    }

    public CompoundMark getNextMark() {
        return nextMark;
    }

    public GeoPoint getLocation() {
        return location;
    }

    /**
     * Sets the current location of the boat in lat and long whilst preserving the last location
     *
     * @param lat Latitude
     * @param lng Longitude
     */
    public void setLocation(Double lat, Double lng) {
        lastLocation.setLat(location.getLat());
        lastLocation.setLng(location.getLng());
        location.setLat(lat);
        location.setLng(lng);
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

    public void updateTimeSinceLastMarkProperty(long timeSinceLastMark) {
        this.timeSinceLastMarkProperty.set(timeSinceLastMark);
    }

    public ReadOnlyLongProperty timeSinceLastMarkProperty() {
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

    public Double getDistanceToNextMark() {
        return distanceToNextMark;
    }

    public void updateLocation(double lat, double lng, double heading, double velocity) {
        setLocation(lat, lng);
        this.heading = heading;
        this.velocity = velocity;
        updateVelocityProperty(velocity);
        for (YachtLocationListener yll : locationListeners) {
            yll.notifyLocation(this, lat, lng, heading, velocity);
        }
    }

    public void addLocationListener(YachtLocationListener listener) {
        locationListeners.add(listener);
    }

    public void setLocation(GeoPoint geoPoint) {
        location = geoPoint;
    }

    /**
     * Collision detection which iterates through all the yachts and check if any yacht collided
     * with this yacht. Return collided yacht or null if no collision.
     *
     * @param calculatedPoint point will the yacht will move next
     * @return yacht which collided with this yacht
     */
    private Yacht checkCollision(GeoPoint calculatedPoint) {
        Double COLLISIONFACTOR = 25.0;  // Collision detection in meters

        for (Yacht yacht : GameState.getYachts().values()) {
            if (yacht != this) {
                Double distance = GeoUtility.getDistance(yacht.getLocation(), calculatedPoint);
                if (distance < COLLISIONFACTOR) {
                    return yacht;
                }
            }
        }
        return null;
    }
}
