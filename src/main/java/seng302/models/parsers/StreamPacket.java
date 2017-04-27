package seng302.models.parsers;

/**
 * Created by kre39 on 23/04/17.
 */
public class StreamPacket {

    //Change int to an ENUM for the type
    private PacketType type;

    private long messageLength;
    private long timeStamp;
    private byte[] payload;

    StreamPacket(int type, long messageLength, long timeStamp, byte[] payload) {
        this.type = PacketType.assignPacketType(type);
        this.messageLength = messageLength;
        this.timeStamp = timeStamp;
        this.payload = payload;
        //System.out.println("type = " + this.type.toString());
        //switch the packet type to deal with what ever specific packet you want to deal with
        if (this.type == PacketType.XML_MESSAGE){
            //System.out.println("--------");
            System.out.println(new String(payload));
            StreamParser.parsePacket(this);
        }
    }

    PacketType getType() {
        return type;
    }

    public long getMessageLength() {
        return messageLength;
    }

    byte[] getPayload() {
        return payload;
    }

    long getTimeStamp() {
        return timeStamp;
    }
}
