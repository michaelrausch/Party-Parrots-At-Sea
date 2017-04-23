package seng302.models.parsers;

/**
 * Created by kre39 on 23/04/17.
 */
public class StreamPacket {

    //Change int to an ENUM for the type
    private int type;

    private long messageLength;
    private long timeStamp;
    private byte[] payload;

    public StreamPacket(int type, long messageLength, long timeStamp, byte[] payload) {
        this.type = type;
        this.messageLength = messageLength;
        this.timeStamp = timeStamp;
        this.payload = payload;
    }
}
