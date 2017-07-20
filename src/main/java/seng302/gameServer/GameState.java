package seng302.gameServer;

import seng302.model.Player;

import java.util.ArrayList;

/**
 * A Static class to hold information about the current state of the game (model)
 * Created by wmu16 on 10/07/17.
 */
public class GameState {

    private static String hostIpAddress;
    private static ArrayList<Player> players;
    private static Boolean isRaceStarted;
    private static GameStages currentStage;
    
    public GameState(String hostIpAddress) {
        GameState.hostIpAddress = hostIpAddress;
        players = new ArrayList<>();
        currentStage = GameStages.LOBBYING;
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

    public static GameStages getCurrentStage() {
        return currentStage;
    }

    public static void setCurrentStage(GameStages currentStage) {
        GameState.currentStage = currentStage;
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
