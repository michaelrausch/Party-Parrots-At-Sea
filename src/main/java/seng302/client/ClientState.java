package seng302.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import seng302.models.Yacht;

/**
 * Used by the client to store static variables to be used in game.
 */
public class ClientState {

    private static String hostIp = "";
    private static Boolean isHost = false;
    private static Boolean raceStarted = false;
    private static Boolean connectedToHost = false;
    private static Map<Integer, Yacht> boats = new ConcurrentHashMap<>();
    private static Boolean boatsUpdated = true;
    private static String clientSourceId = "";

    public static String getHostIp() {
        return hostIp;
    }

    public static void setHostIp(String hostIp) {
        ClientState.hostIp = hostIp;
    }

    public static Boolean isHost() {
        return isHost;
    }

    public static void setHost(Boolean isHost) {
        ClientState.isHost = isHost;
    }

    public static Boolean isRaceStarted() {
        return raceStarted;
    }

    public static void setRaceStarted(Boolean raceStarted) {
        ClientState.raceStarted = raceStarted;
    }

    public static Boolean isConnectedToHost() {
        return connectedToHost;
    }

    public static void setConnectedToHost(Boolean connectedToHost) {
        ClientState.connectedToHost = connectedToHost;
    }

    public static Map<Integer, Yacht> getBoats() {
        return boats;
    }

    public static Boolean isBoatsUpdated() {
        return boatsUpdated;
    }

    public static void setBoatsUpdated(Boolean boatsUpdated) {
        ClientState.boatsUpdated = boatsUpdated;
    }

    public static String getClientSourceId() {
        return clientSourceId;
    }

    public static void setClientSourceId(String clientSourceId) {
        ClientState.clientSourceId = clientSourceId;
    }

    public static void setBoats(Map<Integer, Yacht> boats) {
        ClientState.boats = boats;
    }
}
