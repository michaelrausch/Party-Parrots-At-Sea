package seng302.visualiser;

public interface ServerListenerDelegate {
    void serverDetected(String serverName, String mapName, Integer placesLeft, String serverAddress);
    void serverRemoved(String serverName);
}
