package seng302.visualiser;

import seng302.gameServer.ServerDescription;

import java.util.List;

public interface ServerListenerDelegate {
    void serverRemoved(List<ServerDescription> currentServers);
    void serverDetected(ServerDescription serverDescription, List<ServerDescription> servers);
}
