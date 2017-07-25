package seng302.visualiser;

import seng302.model.stream.packets.StreamPacket;

/**
 * Functional interface for receiving packets from client socket.
 */
@FunctionalInterface
public interface ClientSocketListener {
    void newPacket(StreamPacket packet);
}
