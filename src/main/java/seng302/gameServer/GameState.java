package seng302.gameServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import seng302.client.ClientPacketParser;
import seng302.models.Player;
import seng302.models.Yacht;
import seng302.server.messages.BoatActionType;

/**
 * A Static class to hold information about the current state of the game (model)
 * Created by wmu16 on 10/07/17.
 */
public class GameState implements Runnable {

    private static Integer STATE_UPDATES_PER_SECOND = 60;

    private static Long previousUpdateTime;
    public static Double windDirection;
    private static Double windSpeed;

    private static String hostIpAddress;
    private static List<Player> players;
    private static Map<Integer, Yacht> yachts;
    private static Boolean isRaceStarted;
    private static GameStages currentStage;

    private static long startTime = System.currentTimeMillis();


    public GameState(String hostIpAddress) {
        windDirection = 180d;
        windSpeed = 10000d;
        this.hostIpAddress = hostIpAddress;
        players = new ArrayList<>();
        currentStage = GameStages.LOBBYING;
        isRaceStarted = false;
        yachts = new HashMap<>();
        //set this when game stage changes to prerace
        previousUpdateTime = System.currentTimeMillis();
        yachts = new HashMap<>();

        new Thread(this).start();

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
        return windSpeed / 1000 * ClientPacketParser.MS_TO_KNOTS;
    }

    public static Map<Integer, Yacht> getYachts() {
        return yachts;
    }

    public static void updateBoat(Integer sourceId, BoatActionType actionType) {
        Yacht playerYacht = yachts.get(sourceId);
//        System.out.println("-----------------------");
        switch (actionType) {
            case VMG:
                playerYacht.turnToVMG();
//                System.out.println("Snapping to VMG");
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

//        printBoatStatus(playerYacht);
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


    /**
     * A thread to have the game state update itself at certain intervals
     */
    @Override
    public void run() {

        while(true) {
            try {
                Thread.sleep(1000 / STATE_UPDATES_PER_SECOND);
            } catch (InterruptedException e) {
                System.out.println("[GameState] interrupted exception");
            }
            if (currentStage == GameStages.PRE_RACE) {
                update();
            }

            //RACING
            if (currentStage == GameStages.RACING) {
                update();
            }
        }
    }

    private static void printBoatStatus(Yacht playerYacht) {
        System.out.println("-----------------------");
        System.out.println("Sails are in: " + playerYacht.getSailIn());
        System.out.println("Heading: " + playerYacht.getHeading());
        System.out.println("Velocity: " + playerYacht.getVelocityMMS() / 1000);
        System.out.println("Lat: " + playerYacht.getLocation().getLat());
        System.out.println("Lng: " + playerYacht.getLocation().getLng());
        System.out.println("-----------------------\n");
    }
}
