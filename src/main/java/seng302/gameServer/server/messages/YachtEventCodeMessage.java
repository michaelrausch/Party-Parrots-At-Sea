package seng302.gameServer.server.messages;

/**
 * Created by zyt10 on 10/08/17.
 */
public class YachtEventCodeMessage extends Message {

    private final MessageType MESSAGE_TYPE = MessageType.YACHT_EVENT_CODE;
    private final int MESSAGE_VERSION = 1; //Always set to 1
    private final int MESSAGE_SIZE = 22;

    // Message fields
    private long timeStamp;
    private long ack = 0x00; //Unused
    private int raceId;
    private int destSourceId;
    private int incidentId;
    private int eventId;


    public YachtEventCodeMessage(Integer subjectId) {
        timeStamp = System.currentTimeMillis() / 1000L;
        ack = 0;
        raceId = 1;
        destSourceId = subjectId;  // collision boat source id
        incidentId = 0;
        eventId = 33;

        setHeader(new Header(MESSAGE_TYPE, 0x01, (short) getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        // Write message fields
        putUnsignedByte((byte) MESSAGE_VERSION);
        putInt((int) timeStamp, 6);
        putInt((int) ack, 2);
        putInt((int) raceId, 4);
        putInt((int) destSourceId, 4);
        putInt((int) incidentId, 4);
        putInt((int) eventId, 1);

        writeCRC();
        rewind();
    }

    /**
     * @return The length of this message
     */
    public int getSize() {
        return MESSAGE_SIZE;
    }
}
