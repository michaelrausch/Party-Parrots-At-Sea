package seng302.gameServer.messages;

public class RaceStartStatusMessage extends Message {
    private final int MESSAGE_SIZE = 20;

    private long version;
    private long timeStamp;
    private long ackNumber;
    private long raceStartTime;
    private long raceId;
    private RaceStartNotificationType notificationType;

    /**
     * Message sent to clients with the expected start time of the race
     * @param ackNumber  Sequence number of message.
     * @param raceStartTime Expected race start time
     * @param raceId Race ID#
     * @param notificationType Type of this notification
     */
    public RaceStartStatusMessage(long ackNumber, long raceStartTime, long raceId, RaceStartNotificationType notificationType){
        this.version = 1;
        this.timeStamp = System.currentTimeMillis() / 1000L;
        this.ackNumber = ackNumber;
        this.raceStartTime = raceStartTime;
        this.notificationType = notificationType;
        this.raceId = raceId;

        setHeader(new Header(MessageType.RACE_START_STATUS, 1, (short) getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        putUnsignedByte((byte) version);
        putInt((int) timeStamp, 6);
        putInt((int) ackNumber, 2);
        putInt((int) raceStartTime, 6);
        putInt((int) raceId, 4);
        putUnsignedByte((byte) notificationType.getType());

        writeCRC();
        rewind();
    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE;
    }

}
