package seng302.visualiser;

import seng302.model.stream.packets.StreamPacket;

/**
 * Created by cir27 on 21/07/17.
 */
@FunctionalInterface
public interface ClientSocketListener {
    void newPacket(StreamPacket packet);
}
