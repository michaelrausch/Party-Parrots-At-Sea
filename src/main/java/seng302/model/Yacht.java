package seng302.model;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger logger = LoggerFactory.getLogger(Yacht.class);

    private static final Double ROUNDING_DISTANCE = 50d; // TODO: 3/08/17 wmu16 - Look into this value further


    //BOTH AFAIK
    private String boatType;
    private Integer sourceId;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;

    private Long estimateTimeAtFinish;
    private Integer currentMarkSeqID = 0;
    private Long markRoundTime;
    private Double distanceToCurrentMark;
    private Long timeTillNext;
    private Double heading;
    private Integer legNumber = 0;

    //SERVER SIDE
    public static final Double TURN_STEP = 5.0; //This should be in some utils class somewhere 2bh. Public for tests sake.
    private Double lastHeading;
    private Boolean sailIn;
    private GeoPoint location;
    private Integer boatStatus;
    private Double velocity;
    private Boolean isAuto;
    private Double autoHeading;

    //MARK ROUNDING INFO
    private GeoPoint lastLocation;  //For purposes of mark rounding calculations
    private Boolean hasEnteredRoundingZone; //The distance that the boat must be from the mark to round
    private Boolean hasPassedLine;
    private Boolean hasPassedThroughGate;
    private Boolean finishedRace;

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
        this.isAuto = false;
        this.location = new GeoPoint(57.670341, 11.826856);
        this.lastLocation = location;
        this.heading = 120.0;   //In degrees
        this.velocity = 0d;     //in mms-1

        this.hasEnteredRoundingZone = false;
        this.hasPassedLine = false;
        this.hasPassedThroughGate = false;
        this.finishedRace = false;
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

        runAutoPilot();

        //UPDATE BOAT LOCATION
        lastLocation = location;
        location = GeoUtility.getGeoCoordinate(location, heading, velocity * secondsElapsed);

        //CHECK FOR MARK ROUNDING
        if (!finishedRace) {
            checkForLegProgression();
        }

        // TODO: 3/08/17 wmu16 - Implement line cross check here
    }


    /**
     * Calculates the distance to the next mark (closest of the two if a gate mark). For purposes
     * of mark rounding
     * @return A distance in metres. Returns -1 if there is no next mark
     * @throws IndexOutOfBoundsException If the next mark is null (ie the last mark in the race)
     *         Check first using {@link seng302.model.mark.MarkOrder#isLastMark(Integer)}
     */
    public Double calcDistanceToCurrentMark() throws IndexOutOfBoundsException {
        CompoundMark nextMark = GameState.getMarkOrder().getCurrentMark(currentMarkSeqID);

        if (nextMark.isGate()) {
            Mark sub1 = nextMark.getSubMark(1);
            Mark sub2 = nextMark.getSubMark(2);
            Double distance1 = GeoUtility.getDistance(location, sub1);
            Double distance2 = GeoUtility.getDistance(location, sub2);
            return (distance1 < distance2) ? distance1 : distance2;
        } else {
            return GeoUtility.getDistance(location, nextMark.getSubMark(1));
        }
    }


    /**
     * 4 Different cases of progression in the race
     * 1 - Passing the start line
     * 2 - Passing any in-race Gate
     * 3 - Passing any in-race Mark
     * 4 - Passing the finish line
     */
    private void checkForLegProgression() {
        CompoundMark currentMark = GameState.getMarkOrder().getCurrentMark(currentMarkSeqID);
        if (currentMarkSeqID == 0) {
            checkStartLineCrossing(currentMark);
        } else if (GameState.getMarkOrder().isLastMark(currentMarkSeqID)) {
            checkFinishLineCrossing(currentMark);
        } else if (currentMark.isGate()) {
            checkGateRounding(currentMark);
        } else {
            checkMarkRounding(currentMark);
        }
    }

    /**
     * If we pass the start line gate in the correct direction, progress
     *
     * @param currentMark The current gate
     */
    private void checkStartLineCrossing(CompoundMark currentMark) {
        Mark mark1 = currentMark.getSubMark(1);
        Mark mark2 = currentMark.getSubMark(2);
        CompoundMark nextMark = GameState.getMarkOrder().getNextMark(currentMarkSeqID);

        Integer crossedLine = GeoUtility.checkCrossedLine(mark1, mark2, lastLocation, location);
        if (crossedLine > 0) {
            Boolean isClockwiseCross = GeoUtility.isClockwise(mark1, mark2, nextMark.getMidPoint());
            if (crossedLine == 2 && isClockwiseCross || crossedLine == 1 && !isClockwiseCross) {
                currentMarkSeqID++;
                logMarkRounding(currentMark);
            }
        }
    }


    /**
     * This algorithm checks for mark rounding. And increments the currentMarSeqID number attribute
     * of the yacht if so.
     * A visual representation of this algorithm can be seen on the Wiki under
     * 'mark passing algorithm'
     */
    private void checkMarkRounding(CompoundMark currentMark) {
        distanceToCurrentMark = calcDistanceToCurrentMark();
        GeoPoint nextPoint = GameState.getMarkOrder().getNextMark(currentMarkSeqID).getMidPoint();
        GeoPoint prevPoint = GameState.getMarkOrder().getPreviousMark(currentMarkSeqID)
            .getMidPoint();
        GeoPoint midPoint = GeoUtility.getDirtyMidPoint(nextPoint, prevPoint);

        //1 TEST FOR ENTERING THE ROUNDING DISTANCE
        if (distanceToCurrentMark < ROUNDING_DISTANCE) {
            hasEnteredRoundingZone = true;
        }

        //In case current mark is a gate, loop through all marks just in case
        for (Mark thisCurrentMark : currentMark.getMarks()) {
            if (GeoUtility.isPointInTriangle(lastLocation, location, midPoint, thisCurrentMark)) {
                hasPassedLine = true;
            }
        }

        if (hasPassedLine && hasEnteredRoundingZone) {
            currentMarkSeqID++;
            hasPassedLine = false;
            hasEnteredRoundingZone = false;
            hasPassedThroughGate = false;
            logMarkRounding(currentMark);
        }
    }


    /**
     * Checks if a gate line has been crossed and in the correct direction
     *
     * @param currentMark The current gate
     */
    private void checkGateRounding(CompoundMark currentMark) {
        Mark mark1 = currentMark.getSubMark(1);
        Mark mark2 = currentMark.getSubMark(2);
        CompoundMark prevMark = GameState.getMarkOrder().getPreviousMark(currentMarkSeqID);
        CompoundMark nextMark = GameState.getMarkOrder().getNextMark(currentMarkSeqID);

        Integer crossedLine = GeoUtility.checkCrossedLine(mark1, mark2, lastLocation, location);

        //We have crossed the line
        if (crossedLine > 0) {
            Boolean isClockwiseCross = GeoUtility.isClockwise(mark1, mark2, prevMark.getMidPoint());

            //Check we cross the line in the correct direction
            if (crossedLine == 1 && isClockwiseCross || crossedLine == 2 && !isClockwiseCross) {
                hasPassedThroughGate = true;
            }
        }

        Boolean prevMarkSide = GeoUtility.isClockwise(mark1, mark2, prevMark.getMidPoint());
        Boolean nextMarkSide = GeoUtility.isClockwise(mark1, mark2, nextMark.getMidPoint());

        if (hasPassedThroughGate) {
            //Check if we need to round this gate after passing through
            if (prevMarkSide == nextMarkSide) {
                checkMarkRounding(currentMark);
            } else {
                currentMarkSeqID++;
                logMarkRounding(currentMark);
            }
        }
    }

    /**
     * If we pass the finish gate in the correct direction
     *
     * @param currentMark The current gate
     */
    private void checkFinishLineCrossing(CompoundMark currentMark) {
        Mark mark1 = currentMark.getSubMark(1);
        Mark mark2 = currentMark.getSubMark(2);
        CompoundMark prevMark = GameState.getMarkOrder().getPreviousMark(currentMarkSeqID);

        Integer crossedLine = GeoUtility.checkCrossedLine(mark1, mark2, lastLocation, location);
        if (crossedLine > 0) {
            Boolean isClockwiseCross = GeoUtility.isClockwise(mark1, mark2, prevMark.getMidPoint());
            if (crossedLine == 1 && isClockwiseCross || crossedLine == 2 && !isClockwiseCross) {
                currentMarkSeqID++;
                finishedRace = true;
                logMarkRounding(currentMark);
                logger.debug(sourceId + " finished");
                // TODO: 8/08/17 wmu16 - Do something!
            }
        }
    }


    /**
     * Adjusts the heading of the boat by a given amount, while recording the boats
     * last heading.
     *
     * @param amount the amount by which to adjust the boat heading.
     */
    public void adjustHeading(Double amount) {
        Double newVal = heading + amount;
        lastHeading = heading;
        heading = (double) Math.floorMod(newVal.longValue(), 360L);
    }

    /**
     * Swaps the boats direction from one side of the wind to the other.
     */
    public void tackGybe(Double windDirection) {
        if (isAuto) {
            disableAutoPilot();
        } else {
            Double normalizedHeading = normalizeHeading();
            Double newVal = (-2 * normalizedHeading) + heading;
            Double newHeading = (double) Math.floorMod(newVal.longValue(), 360L);
            setAutoPilot(newHeading);
        }
    }

    /**
     * Enables the boats auto pilot feature, which will move the boat towards a given heading.
     * @param thisHeading The heading to move the boat towards.
     */
    private void setAutoPilot(Double thisHeading) {
        isAuto = true;
        autoHeading = thisHeading;
    }

    /**
     * Disables the auto pilot function.
     */
    public void disableAutoPilot() {
        isAuto = false;
    }

    /**
     * Moves the boat towards the given heading when the auto pilot was set. Disables the auto pilot
     * in the event that the boat is within the range of 1 turn step of its goal.
     */
    public void runAutoPilot() {
        if (isAuto) {
            turnTowardsHeading(autoHeading);
            if (Math.abs(heading - autoHeading)
                <= TURN_STEP) { //Cancel when within 1 turn step of target.
                isAuto = false;
            }
        }
    }

    public void toggleSailIn() {
        sailIn = !sailIn;
    }

    public void turnUpwind() {
        disableAutoPilot();
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
        disableAutoPilot();
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

    /**
     * Takes the VMG from the polartable for upwind or downwind depending on the boats direction,
     * and uses this to calculate a heading to move the yacht towards.
     */
    public void turnToVMG() {
        if (isAuto) {
            disableAutoPilot();
        } else {
            Double normalizedHeading = normalizeHeading();
            Double optimalHeading;
            HashMap<Double, Double> optimalPolarMap;

            if (normalizedHeading >= 90 && normalizedHeading <= 270) { // Downwind
                optimalPolarMap = PolarTable.getOptimalDownwindVMG(GameState.getWindSpeedKnots());
            } else {
                optimalPolarMap = PolarTable.getOptimalUpwindVMG(GameState.getWindSpeedKnots());
            }
            optimalHeading = optimalPolarMap.keySet().iterator().next();

            if (normalizedHeading > 180) {
                optimalHeading = 360 - optimalHeading;
            }

            // Take optimal heading and turn into a boat heading rather than a wind heading.
            optimalHeading =
                optimalHeading + GameState.getWindDirection();

            setAutoPilot(optimalHeading);
        }
    }

    /**
     * Takes a given heading and rotates the boat towards that heading.
     * This does not care about being upwind or downwind, just which direction will reach a given
     * heading faster.
     *
     * @param newHeading The heading to turn the yacht towards.
     */
    private void turnTowardsHeading(Double newHeading) {
        Double newVal = heading - newHeading;
        if (Math.floorMod(newVal.longValue(), 360L) > 180) {
            adjustHeading(TURN_STEP);
        } else {
            adjustHeading(-TURN_STEP);
        }
    }

    /**
     * Returns a heading normalized for the wind direction. Heading direction into the wind is 0,
     * directly away is 180.
     *
     * @return The normalized heading accounting for wind direction.
     */
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

    public CompoundMark getLastMarkRounded() {
        return lastMarkRounded;
    }

    public void setLastMarkRounded(CompoundMark lastMarkRounded) {
        this.lastMarkRounded = lastMarkRounded;
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

    public Double getDistanceToCurrentMark() {
        return distanceToCurrentMark;
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

    private void logMarkRounding(CompoundMark currentMark) {
        String typeString = "mark";
        if (currentMark.isGate()) {
            typeString = "gate";
        }
        logger.debug(
            String.format("BoatID %d passed %s %s with id %d. Now on leg %d",
                sourceId,
                typeString,
                currentMark.getMarks().get(0).getName(),
                currentMark.getId(),
                currentMarkSeqID));
    }

    public void addLocationListener (YachtLocationListener listener) {
        locationListeners.add(listener);
    }
}
