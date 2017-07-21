package seng302.models.stream;

import seng302.models.stream.packets.StreamPacket;

public interface PacketBufferDelegate {
    boolean addToBuffer(StreamPacket streamPacket);
}
