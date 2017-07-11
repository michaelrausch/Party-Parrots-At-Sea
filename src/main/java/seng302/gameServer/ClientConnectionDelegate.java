package seng302.gameServer;

import seng302.models.Player;

public interface ClientConnectionDelegate {
    void clientConnected(Player player);
}
