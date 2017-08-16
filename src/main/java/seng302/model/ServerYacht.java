package seng302.model;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.GameState;
import seng302.gameServer.messages.BoatStatus;
import seng302.model.mark.Mark;
import seng302.utilities.GeoUtility;

/**
 * Yacht class for the racing boat. <p> Class created to store more variables (eg. boat statuses)
 * compared to the XMLParser boat class, also done outside Boat class because some old variables are
 * not used anymore.
 */
public class ServerYacht extends Observable {

    private Logger logger = LoggerFactory.getLogger(ClientYacht.class);

    public static final Double TURN_STEP = 5.0;

    //Boat info
    private String boatType;
    private Integer sourceId;
    private String hullID; //matches HullNum in the XML spec.
    private String shortName;
    private String boatName;
    private String country;
    private BoatStatus boatStatus;


    //Location
    private Double lastHeading;
    private Boolean sailIn;
    private Double heading;
    private GeoPoint lastLocation;
    private GeoPoint location;
    private Double currentVelocity;
    private Boolean isAuto;
    private Double autoHeading;
    private Integer legNumber;

    //Mark Rounding
    private Integer currentMarkSeqID;
    private Boolean hasEnteredRoundingZone;
    private Mark closestCurrentMark;
    private Boolean hasPassedLine;
    private Boolean hasPassedThroughGate;


    public ServerYacht(String boatType, Integer sourceId, String hullID, String shortName,
        String boatName, String country) {
        this.boatType = boatType;
        this.boatStatus = BoatStatus.PRESTART;
        this.sourceId = sourceId;
        this.hullID = hullID;
        this.shortName = shortName;
        this.boatName = boatName;
        this.country = country;
        this.sailIn = false;
        this.isAuto = false;
        this.location = new GeoPoint(57.67046, 11.83751);
        this.lastLocation = location;
        this.heading = 120.0;   //In degrees
        this.currentVelocity = 0d;     //in mms-1
        this.currentMarkSeqID = 0;
        this.legNumber = 0;

        this.hasEnteredRoundingZone = false;
        this.hasPassedLine = false;
        this.hasPassedThroughGate = false;
    }


    /**
     * Changes the boats current currentVelocity by a set amount, positive or negative
     *
     * @param velocityChange The ammount to change the currentVelocity by, in mms-1
     */
    public void changeVelocity(Double velocityChange) {
        currentVelocity += velocityChange;
    }

    /**
     * Updates the boat to a new GeoPoint whilst preserving the last location
     *
     * @param secondsElapsed The seconds elapsed since the last update of this yacht
     */
    public void updateLocation(Double secondsElapsed) {
        lastLocation = location;
        location = GeoUtility.getGeoCoordinate(location, heading, currentVelocity * secondsElapsed);
    }

    public void setLocation(GeoPoint geoPoint) {
        location = geoPoint;
    }

    /**
     * Add ServerToClientThread as the observer, this observer pattern mainly server for the boat
     * rounding package.
     */
    @Override
    public void addObserver(Observer o) {
        super.addObserver(o);
    }

    /**
     * Adjusts the heading of the boat by a given amount, while recording the boats last heading.
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
     *
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
     * Takes a given heading and rotates the boat towards that heading. This does not care about
     * being upwind or downwind, just which direction will reach a given heading faster.
     *
     * @param newHeading The heading to turn the yacht towards.
     */
    private void turnTowardsHeading(Double newHeading) {
        Double newVal = heading - newHeading;
        if (Math.floorMod(newVal.longValue(), 360L) > 180) {
            adjustHeading(TURN_STEP / 5);
        } else {
            adjustHeading(-TURN_STEP / 5);
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

    public Integer getSourceId() {
        //@TODO Remove and merge with Creating Game Loop
        if (sourceId == null) {
            return 0;
        }
        return sourceId;
    }

    // TODO: 15/08/17 This method is implicitly called from the XML generator for boats DO NOT DELETE
    public String getHullID() {
        if (hullID == null) {
            return "";
        }
        return hullID;
    }

    // TODO: 15/08/17 This method is implicitly called from the XML generator for boats DO NOT DELETE
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


    public GeoPoint getLocation() {
        return location;
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

    public Double getCurrentVelocity() {
        return currentVelocity;
    }

    public void setCurrentVelocity(Double currentVelocity) {
        this.currentVelocity = currentVelocity;
    }

    public Integer getCurrentMarkSeqID() {
        return currentMarkSeqID;
    }

    public GeoPoint getLastLocation() {
        return lastLocation;
    }

    public Mark getClosestCurrentMark() {
        return closestCurrentMark;
    }

    public void setClosestCurrentMark(Mark closestCurrentMark) {
        this.closestCurrentMark = closestCurrentMark;
    }

    public void setHasEnteredRoundingZone(Boolean hasEnteredRoundingZone) {
        this.hasEnteredRoundingZone = hasEnteredRoundingZone;
    }

    public void setHasPassedLine(Boolean hasPassedLine) {
        this.hasPassedLine = hasPassedLine;
    }

    public void setHasPassedThroughGate(Boolean hasPassedThroughGate) {
        this.hasPassedThroughGate = hasPassedThroughGate;
    }

    public BoatStatus getBoatStatus() {
        return boatStatus;
    }

    public void setBoatStatus(BoatStatus boatStatus) {
        this.boatStatus = boatStatus;
    }

    public void incrementMarkSeqID() {
        currentMarkSeqID++;
    }

    public Boolean hasEnteredRoundingZone() {
        return hasEnteredRoundingZone;
    }

    public Boolean hasPassedThroughGate() {
        return hasPassedThroughGate;
    }

    public Boolean hasPassedLine() {
        return hasPassedLine;
    }

    public void incrementLegNumber() {
        legNumber++;
    }

    public Integer getLegNumber() {
        return legNumber;
    }

}
