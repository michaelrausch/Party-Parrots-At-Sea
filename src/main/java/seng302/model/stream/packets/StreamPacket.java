package seng302.model.stream.packets;

/**
 * Created by kre39 on 23/04/17.
 */
public class StreamPacket {

    //Change int to an ENUM for the type
    private PacketType type;

    private long messageLength;
    private long timeStamp;
    private byte[] payload;

    public StreamPacket(int type, long messageLength, long timeStamp, byte[] payload) {
        this.type = PacketType.assignPacketType(type);
        this.messageLength = messageLength;
        this.timeStamp = timeStamp;
        this.payload = payload;
    }

    public PacketType getType() {
        return type;
    }

    public long getMessageLength() {
        return messageLength;
    }

    public byte[] getPayload() {
        return payload;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
