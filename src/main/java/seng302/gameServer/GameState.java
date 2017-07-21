package seng302.gameServer;

import java.util.*;

import seng302.models.Player;

import seng302.models.Yacht;
import seng302.server.messages.BoatActionType;

/**
 * A Static class to hold information about the current state of the game (model)
 * Created by wmu16 on 10/07/17.
 */
public class GameState {

    private static Long previousUpdateTime;
    private static Double windDirection = 0d;
    private static Double windSpeed = 0d;

    private static String hostIpAddress;
    private static List<Player> players;
    private static Map<Integer, Yacht> yachts;
    private static Boolean isRaceStarted;
    private static GameStages currentStage;
    
    public GameState(String hostIpAddress) {
        GameState.hostIpAddress = hostIpAddress;
        players = new ArrayList<>();
        currentStage = GameStages.LOBBYING;
        isRaceStarted = false;
        //set this when game stage changes to prerace
        previousUpdateTime = System.currentTimeMillis();
        yachts = new HashMap<>();
    }

    public static String getHostIpAddress() {
        return hostIpAddress;
    }

    public static List<Player> getPlayers() {
        return players;
    }
    
    public static void addPlayer(Player player) {
        players.add(player);
    }
    
    public static void removePlayer(Player player) {
        players.remove(player);
    }

    public static void addYacht(Integer sourceId, Yacht yatch) {
        yachts.put(sourceId, yatch);
    }

    public static Boolean getIsRaceStarted() {
        return isRaceStarted;
    }

    public static GameStages getCurrentStage() {
        return currentStage;
    }

    public static void setCurrentStage(GameStages currentStage) {
        GameState.currentStage = currentStage;
    }

    public static Double getWindDirection() {
        return windDirection;
    }

    public static Double getWindSpeed() {
        return windSpeed;
    }

    public static void updateBoat(Integer sourceId, BoatActionType actionType) {
        switch (actionType) {
            case VMG:
                break;
            case SAILS_IN:
                break;
            case SAILS_OUT:
                break;
            case TACK_GYBE:
                break;
            case UPWIND:
                break;
            case DOWNWIND:
                break;
        }
    }

    public static void update() {
        Long timeInterval = System.currentTimeMillis() - previousUpdateTime;
        previousUpdateTime = System.currentTimeMillis();
        for (Yacht yacht : yachts.values()) {
            yacht.update(timeInterval);
        }
    }
}
