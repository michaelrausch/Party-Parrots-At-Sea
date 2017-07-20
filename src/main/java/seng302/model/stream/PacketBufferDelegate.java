package seng302.model.stream;

import seng302.model.stream.packets.StreamPacket;

public interface PacketBufferDelegate {
    boolean addToBuffer(StreamPacket streamPacket);
}
