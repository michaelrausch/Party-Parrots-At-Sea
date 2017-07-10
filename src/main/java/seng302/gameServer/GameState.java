package seng302.gameServer;

import seng302.models.Player;

import java.util.ArrayList;

/**
 * A Static class to hold information about the current state of the game (model)
 * Created by wmu16 on 10/07/17.
 */
public class GameState {

    public static final Integer MAX_NUM_PLAYERS = 10;
    private static String hostIpAddress;
    private static ArrayList<Player> players;
    private static Boolean isRaceStarted;
    
    public GameState(String hostIpAddress) {
        GameState.hostIpAddress = hostIpAddress;
        players = new ArrayList<>();
        isRaceStarted = false;
    }

    public static String getHostIpAddress() {
        return hostIpAddress;
    }

    public static ArrayList<Player> getPlayers() {
        return players;
    }
    
    public static void addPlayer(Player player) {
        players.add(player);
    }
    
    public static void removePlayer(Player player) {
        players.remove(player);
    }

    public static Boolean getIsRaceStarted() {
        return isRaceStarted;
    }


    /**
     * This iterates through all players and updates each players info to its new state based on its current data
     */
    private void update(){
        for(Player player : players) {
            // TODO: 10/07/17 wmu16 - Update all player info 
        }
    }
    
    
    
    
}
