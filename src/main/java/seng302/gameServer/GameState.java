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
    public static Double windDirection;
    private static Double windSpeed;

    private static String hostIpAddress;
    private static List<Player> players;
    private static Map<Integer, Yacht> yachts;
    private static Boolean isRaceStarted;
    private static GameStages currentStage;
    
    public GameState(String hostIpAddress) {
        windDirection = 170d;
        windSpeed = 10000d;
        yachts = new HashMap<>();
        players = new ArrayList<>();


        GameState.hostIpAddress = hostIpAddress;
        players = new ArrayList<>();
        currentStage = GameStages.LOBBYING;
        isRaceStarted = false;
        yachts = new HashMap<>();
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

    public static void addYacht(Integer sourceId, Yacht yacht) {
        yachts.put(sourceId, yacht);
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

    public static Map<Integer, Yacht> getYachts() {
        return yachts;
    }

    public static void updateBoat(Integer sourceId, BoatActionType actionType) {
        Yacht playerYacht = yachts.get(sourceId);
//        System.out.println("-----------------------");
        switch (actionType) {
            case VMG:
//                System.out.println("Snapping to VMG");
                // TODO: 22/07/17 wmu16 - Add in the vmg calculation code here
                break;
            case SAILS_IN:
                playerYacht.toggleSailIn();
//                System.out.println("Toggling Sails");
                break;
            case SAILS_OUT:
                playerYacht.toggleSailIn();
//                System.out.println("Toggling Sails");
                break;
            case TACK_GYBE:
                playerYacht.tackGybe(windDirection);
//                System.out.println("Tack/Gybe");
                break;
            case UPWIND:
                playerYacht.turnUpwind();
//                System.out.println("Moving upwind");
                break;
            case DOWNWIND:
                playerYacht.turnDownwind();
//                System.out.println("Moving downwind");
                break;
        }

//        System.out.println("-----------------------");
//        System.out.println("Heading: " + playerYacht.getHeading());
//        System.out.println("Sails are in: " + playerYacht.getSailIn());
//        System.out.println("Lat: " + playerYacht.getLocation().getLat());
//        System.out.println("Lng: " + playerYacht.getLocation().getLng());
//        System.out.println("-----------------------\n");
    }

    public static void update() {

        Long timeInterval = System.currentTimeMillis() - previousUpdateTime;
        previousUpdateTime = System.currentTimeMillis();
        for (Yacht yacht : yachts.values()) {
            yacht.update(timeInterval);
        }
    }


    /**
     * Generates a new ID based off the size of current players + 1
     * @return a playerID to be allocated to a new connetion
     */
    public static Integer getUniquePlayerID() {
        // TODO: 22/07/17 wmu16 - This may not be robust enough and may have to be improved on.
        return yachts.size() + 1;
    }
}
