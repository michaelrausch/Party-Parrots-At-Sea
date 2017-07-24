package seng302.client;

import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * Created by zyt10 on 21/07/17.
 */
public class ClientState {

    private static String hostIp = "";
    private static Boolean isHost = false;
    private static Boolean raceStarted = false;
    private static Boolean connectedToHost = false;

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
}
