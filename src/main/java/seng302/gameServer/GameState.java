package seng302.gameServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.server.messages.BoatAction;
import seng302.gameServer.server.messages.BoatStatus;
import seng302.gameServer.server.messages.MarkRoundingMessage;
import seng302.gameServer.server.messages.MarkType;
import seng302.gameServer.server.messages.Message;
import seng302.gameServer.server.messages.RoundingBoatStatus;
import seng302.model.GeoPoint;
import seng302.model.Player;
import seng302.model.PolarTable;
import seng302.model.ServerYacht;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Mark;
import seng302.model.mark.MarkOrder;
import seng302.utilities.GeoUtility;

/**
 * A Static class to hold information about the current state of the game (model)
 * Also contains logic for updating itself on regular time intervals on its own thread
 * Created by wmu16 on 10/07/17.
 */
public class GameState implements Runnable {

    @FunctionalInterface
    interface MarkPassingListener {
        void markPassing(Message message);
    }

    private Logger logger = LoggerFactory.getLogger(GameState.class);

    private static final Integer STATE_UPDATES_PER_SECOND = 60;
    public static Integer MAX_PLAYERS = 8;
    public static Double ROUNDING_DISTANCE = 50d; // TODO: 14/08/17 wmu16 - Look into this value further

    private static Long previousUpdateTime;
    public static Double windDirection;
    private static Double windSpeed;

    private static String hostIpAddress;
    private static List<Player> players;
    private static Map<Integer, ServerYacht> yachts;
    private static Boolean isRaceStarted;
    private static GameStages currentStage;
    private static MarkOrder markOrder;
    private static long startTime;

    private static List<MarkPassingListener> markListeners;

    private static Map<Player, String> playerStringMap = new HashMap<>();
    /*
        Ideally I would like to make this class an object instantiated by the server and given to
        it's created threads if necessary. Outside of that I think the dependencies on it
        (atm only Yacht & GameClient) can be removed from most other classes. The observable list of
        players could be pulled directly from the server by the GameClient since it instantiates it
        and it is reasonable for it to pull data. The current setup of publicly available statics is
        pretty meh IMO because anything can change it making it unreliable and like people did with
        the old ServerParser class everything that needs shared just gets thrown in the static
        collections and things become a real mess.
     */

    public GameState(String hostIpAddress) {
        windDirection = 180d;
        windSpeed = 10000d;
        this.hostIpAddress = hostIpAddress;
        yachts = new HashMap<>();
        players = new ArrayList<>();
        GameState.hostIpAddress = hostIpAddress;
        ;
        currentStage = GameStages.LOBBYING;
        isRaceStarted = false;
        //set this when game stage changes to prerace
        previousUpdateTime = System.currentTimeMillis();
        markOrder = new MarkOrder(); //This could be instantiated at some point with a select map?
        markListeners = new ArrayList<>();

        new Thread(this).start();   //Run the auto updates on the game state
    }

    public static String getHostIpAddress() {
        return hostIpAddress;
    }

    public static List<Player> getPlayers() {
        return players;
    }

    public static void addPlayer(Player player) {
        players.add(player);
        String playerText = player.getYacht().getSourceId() + " " + player.getYacht().getBoatName()
            + " " + player.getYacht().getCountry();
        playerStringMap.put(player, playerText);
    }
    
    public static void removePlayer(Player player) {
        players.remove(player);
        playerStringMap.remove(player);
    }

    public static void addYacht(Integer sourceId, ServerYacht yacht) {
        yachts.put(sourceId, yacht);
    }

    public static void removeYacht(Integer yachtId) {
        yachts.remove(yachtId);
    }

    public static Boolean getIsRaceStarted() {
        return isRaceStarted;
    }

    public static GameStages getCurrentStage() {
        return currentStage;
    }

    public static void setCurrentStage(GameStages currentStage) {
        if (currentStage == GameStages.RACING){
            startTime = System.currentTimeMillis();
        }

        GameState.currentStage = currentStage;
    }

    public static MarkOrder getMarkOrder() {
        return markOrder;
    }

    public static long getStartTime(){
        return startTime;
    }

    public static Double getWindDirection() {
        return windDirection;
    }

    public static Double getWindSpeedMMS() {
        return windSpeed;
    }

    public static Double getWindSpeedKnots() {
        return GeoUtility.mmsToKnots(windSpeed); // TODO: 26/07/17 cir27 - remove magic numbers
    }

    public static Map<Integer, ServerYacht> getYachts() {
        return yachts;
    }


    /**
     * Generates a new ID based off the size of current players + 1
     *
     * @return a playerID to be allocated to a new connetion
     */
    public static Integer getUniquePlayerID() {
        // TODO: 22/07/17 wmu16 - This may not be robust enough and may have to be improved on.
        return yachts.size() + 1;
    }


    /**
     * A thread to have the game state update itself at certain intervals
     */
    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(1000 / STATE_UPDATES_PER_SECOND);
            } catch (InterruptedException e) {
                System.out.println("[GameState] interrupted exception");
            }
            if (currentStage == GameStages.PRE_RACE) {
                update();
            }

            if (currentStage == GameStages.RACING) {
                update();
            }
        }
    }

    public static void updateBoat(Integer sourceId, BoatAction actionType) {
        ServerYacht playerYacht = yachts.get(sourceId);
        switch (actionType) {
            case VMG:
                playerYacht.turnToVMG();
                break;
            case SAILS_IN:
                playerYacht.toggleSailIn();
                break;
            case SAILS_OUT:
                playerYacht.toggleSailIn();
                break;
            case TACK_GYBE:
                playerYacht.tackGybe(windDirection);
                break;
            case UPWIND:
                playerYacht.turnUpwind();
                break;
            case DOWNWIND:
                playerYacht.turnDownwind();
                break;
        }
    }


    /**
     * Called periodically in this GameState thread to update the GameState values
     */
    public void update() {
        Double timeInterval = (System.currentTimeMillis() - previousUpdateTime) / 1000000.0;
        previousUpdateTime = System.currentTimeMillis();
        for (ServerYacht yacht : yachts.values()) {
            updateVelocity(yacht);
            yacht.runAutoPilot();
            yacht.updateLocation(timeInterval);
            if (!yacht.getFinishedRace()) {
                checkForLegProgression(yacht);
            }
        }
    }


    private void updateVelocity(ServerYacht yacht) {
        Double velocity = yacht.getCurrentVelocity();
        Double trueWindAngle = Math.abs(windDirection - yacht.getHeading());
        Double boatSpeedInKnots = PolarTable.getBoatSpeed(getWindSpeedKnots(), trueWindAngle);
        Double maxBoatSpeed = GeoUtility.knotsToMMS(boatSpeedInKnots);
        // TODO: 15/08/17 remove magic numbers from these equations.
        if (yacht.getSailIn()) {
            if (velocity < maxBoatSpeed - 500) {
                yacht.changeVelocity(maxBoatSpeed / 100);
            } else if (velocity > maxBoatSpeed + 500) {
                yacht.changeVelocity(-maxBoatSpeed / 100);
            } else {
                yacht.setCurrentVelocity(maxBoatSpeed);
            }
        } else {
            if (velocity > 3000) {
                yacht.changeVelocity(-velocity / 200);
            } else if (velocity > 100) {
                yacht.changeVelocity(-velocity / 50);
            } else if (velocity <= 100){
                yacht.setCurrentVelocity(0d);
            }
        }
    }


    /**
     * Calculates the distance to the next mark (closest of the two if a gate mark). For purposes of
     * mark rounding
     *
     * @return A distance in metres. Returns -1 if there is no next mark
     * @throws IndexOutOfBoundsException If the next mark is null (ie the last mark in the race)
     * Check first using {@link seng302.model.mark.MarkOrder#isLastMark(Integer)}
     */
    private Double calcDistanceToCurrentMark(ServerYacht yacht) throws IndexOutOfBoundsException {
        Integer currentMarkSeqID = yacht.getCurrentMarkSeqID();
        CompoundMark currentMark = markOrder.getCurrentMark(currentMarkSeqID);
        GeoPoint location = yacht.getLocation();

        if (currentMark.isGate()) {
            Mark sub1 = currentMark.getSubMark(1);
            Mark sub2 = currentMark.getSubMark(2);
            Double distance1 = GeoUtility.getDistance(location, sub1);
            Double distance2 = GeoUtility.getDistance(location, sub2);
            if (distance1 < distance2) {
                yacht.setClosestCurrentMark(sub1);
                return distance1;
            } else {
                yacht.setClosestCurrentMark(sub2);
                return distance2;
            }
        } else {
            yacht.setClosestCurrentMark(currentMark.getSubMark(1));
            return GeoUtility.getDistance(location, currentMark.getSubMark(1));
        }
    }


    /**
     * 4 Different cases of progression in the race 1 - Passing the start line 2 - Passing any
     * in-race Gate 3 - Passing any in-race Mark 4 - Passing the finish line
     * @param yacht the current yacht to check for progression
     */
    private void checkForLegProgression(ServerYacht yacht) {
        Integer currentMarkSeqID = yacht.getCurrentMarkSeqID();
        CompoundMark currentMark = markOrder.getCurrentMark(currentMarkSeqID);

        Boolean hasProgressed;
        if (currentMarkSeqID == 0) {
            hasProgressed = checkStartLineCrossing(yacht);
        } else if (markOrder.isLastMark(currentMarkSeqID)) {
            hasProgressed = checkFinishLineCrossing(yacht);
        } else if (currentMark.isGate()) {
            hasProgressed = checkGateRounding(yacht);
        } else {
            hasProgressed = checkMarkRounding(yacht);
        }

        if (hasProgressed) {
            sendMarkRoundingMessage(yacht);
            logMarkRounding(yacht);
            yacht.setHasPassedLine(false);
            yacht.setHasEnteredRoundingZone(false);
            yacht.setHasPassedThroughGate(false);
            if (!markOrder.isLastMark(currentMarkSeqID)) {
                yacht.incrementMarkSeqID();
            }
        }
    }

    /**
     * If we pass the start line gate in the correct direction, progress
     *
     * @param yacht The current yacht to check for
     */
    private Boolean checkStartLineCrossing(ServerYacht yacht) {
        Integer currentMarkSeqID = yacht.getCurrentMarkSeqID();
        CompoundMark currentMark = markOrder.getCurrentMark(currentMarkSeqID);
        GeoPoint lastLocation = yacht.getLastLocation();
        GeoPoint location = yacht.getLocation();

        Mark mark1 = currentMark.getSubMark(1);
        Mark mark2 = currentMark.getSubMark(2);
        CompoundMark nextMark = markOrder.getNextMark(currentMarkSeqID);

        Integer crossedLine = GeoUtility.checkCrossedLine(mark1, mark2, lastLocation, location);
        if (crossedLine > 0) {
            Boolean isClockwiseCross = GeoUtility.isClockwise(mark1, mark2, nextMark.getMidPoint());
            if (crossedLine == 2 && isClockwiseCross || crossedLine == 1 && !isClockwiseCross) {
                yacht.setClosestCurrentMark(mark1);
                yacht.setBoatStatus(BoatStatus.RACING);
                return true;
            }
        }

        return false;
    }


    /**
     * This algorithm checks for mark rounding. And increments the currentMarSeqID number attribute
     * of the yacht if so. A visual representation of this algorithm can be seen on the Wiki under
     * 'mark passing algorithm'
     *
     * @param yacht The current yacht to check for
     */
    private Boolean checkMarkRounding(ServerYacht yacht) {
        Integer currentMarkSeqID = yacht.getCurrentMarkSeqID();
        CompoundMark currentMark = markOrder.getCurrentMark(currentMarkSeqID);
        GeoPoint lastLocation = yacht.getLastLocation();
        GeoPoint location = yacht.getLocation();
        GeoPoint nextPoint = markOrder.getNextMark(currentMarkSeqID).getMidPoint();
        GeoPoint prevPoint = markOrder.getPreviousMark(currentMarkSeqID).getMidPoint();
        GeoPoint midPoint = GeoUtility.getDirtyMidPoint(nextPoint, prevPoint);

        if (calcDistanceToCurrentMark(yacht) < ROUNDING_DISTANCE) {
            yacht.setHasEnteredRoundingZone(true);
        }

        //In case current mark is a gate, loop through all marks just in case
        for (Mark thisCurrentMark : currentMark.getMarks()) {
            if (GeoUtility.isPointInTriangle(lastLocation, location, midPoint, thisCurrentMark)) {
                yacht.setHasPassedLine(true);
            }
        }

        return yacht.hasPassedLine() && yacht.hasEnteredRoundingZone();
    }


    /**
     * Checks if a gate line has been crossed and in the correct direction
     *
     * @param yacht The current yacht to check for
     */
    private Boolean checkGateRounding(ServerYacht yacht) {
        Integer currentMarkSeqID = yacht.getCurrentMarkSeqID();
        CompoundMark currentMark = markOrder.getCurrentMark(currentMarkSeqID);
        GeoPoint lastLocation = yacht.getLastLocation();
        GeoPoint location = yacht.getLocation();

        Mark mark1 = currentMark.getSubMark(1);
        Mark mark2 = currentMark.getSubMark(2);
        CompoundMark prevMark = markOrder.getPreviousMark(currentMarkSeqID);
        CompoundMark nextMark = markOrder.getNextMark(currentMarkSeqID);

        Integer crossedLine = GeoUtility.checkCrossedLine(mark1, mark2, lastLocation, location);

        //We have crossed the line
        if (crossedLine > 0) {
            Boolean isClockwiseCross = GeoUtility.isClockwise(mark1, mark2, prevMark.getMidPoint());

            //Check we cross the line in the correct direction
            if (crossedLine == 1 && isClockwiseCross || crossedLine == 2 && !isClockwiseCross) {
                yacht.setHasPassedThroughGate(true);
            }
        }

        Boolean prevMarkSide = GeoUtility.isClockwise(mark1, mark2, prevMark.getMidPoint());
        Boolean nextMarkSide = GeoUtility.isClockwise(mark1, mark2, nextMark.getMidPoint());

        if (yacht.hasPassedThroughGate()) {
            //Check if we need to round this gate after passing through
            if (prevMarkSide == nextMarkSide) {
                return checkMarkRounding(yacht);
            } else {
                return true;
            }
        }

        return false;
    }

    /**
     * If we pass the finish gate in the correct direction
     *
     * @param yacht The current yacht to check for
     */
    private Boolean checkFinishLineCrossing(ServerYacht yacht) {
        Integer currentMarkSeqID = yacht.getCurrentMarkSeqID();
        CompoundMark currentMark = markOrder.getCurrentMark(currentMarkSeqID);
        GeoPoint lastLocation = yacht.getLastLocation();
        GeoPoint location = yacht.getLocation();

        Mark mark1 = currentMark.getSubMark(1);
        Mark mark2 = currentMark.getSubMark(2);
        CompoundMark prevMark = markOrder.getPreviousMark(currentMarkSeqID);

        Integer crossedLine = GeoUtility.checkCrossedLine(mark1, mark2, lastLocation, location);
        if (crossedLine > 0) {
            Boolean isClockwiseCross = GeoUtility.isClockwise(mark1, mark2, prevMark.getMidPoint());
            if (crossedLine == 1 && isClockwiseCross || crossedLine == 2 && !isClockwiseCross) {
                yacht.setClosestCurrentMark(mark1);
                yacht.setIsFinished(true);
                yacht.setBoatStatus(BoatStatus.FINISHED);
                return true;
            }
        }

        return false;
    }

    private void sendMarkRoundingMessage(ServerYacht yacht) {
        Integer sourceID = yacht.getSourceId();
        Integer currentMarkSeqID = yacht.getCurrentMarkSeqID();
        CompoundMark currentMark = markOrder.getCurrentMark(currentMarkSeqID);
        MarkType markType = (currentMark.isGate()) ? MarkType.GATE : MarkType.ROUNDING_MARK;
        Mark roundingMark = yacht.getClosestCurrentMark();

        // TODO: 13/8/17 figure out the rounding side, rounded mark source ID and boat status.
        Message markRoundingMessage = new MarkRoundingMessage(0, 0,
            sourceID, RoundingBoatStatus.RACING, roundingMark.getRoundingSide(), markType,
            roundingMark.getSourceID());

        for (MarkPassingListener mpl : markListeners) {
            mpl.markPassing(markRoundingMessage);
        }
    }


    private void logMarkRounding(ServerYacht yacht) {
        Mark roundingMark = yacht.getClosestCurrentMark();
        logger.debug(
            String.format("Yacht srcID(%d) passed Mark srcID(%d)", yacht.getSourceId(),
                roundingMark.getSourceID()));
    }


    public static void addMarkPassListener(MarkPassingListener listener) {
        markListeners.add(listener);
    }
}
