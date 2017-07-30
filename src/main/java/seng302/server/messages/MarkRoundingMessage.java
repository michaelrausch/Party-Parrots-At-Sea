package seng302.server.messages;

public class MarkRoundingMessage extends Message{
    private final long MESSAGE_VERSION_NUMBER = 1;
    private final int MESSAGE_SIZE = 21;

    private long time;
    private long ackNumber;
    private long raceId;
    private long sourceId;
    private RoundingBoatStatus boatStatus;
    private RoundingSide roundingSide;
    private long markId;

    /**
     * This message is sent when a boat passes a mark, start line, or finish line
     * The purpose of this is to record the time when yachts cross marks
     */
    public MarkRoundingMessage(int ackNumber, int raceId, int sourceId, RoundingBoatStatus roundingBoatStatus,
                               RoundingSide roundingSide, int markId){
        this.time = System.currentTimeMillis() / 1000L;
        this.ackNumber = ackNumber;
        this.raceId = raceId;
        this.sourceId = sourceId;
        this.boatStatus = roundingBoatStatus;
        this.roundingSide = roundingSide;
        this.markId = markId;

        setHeader(new Header(MessageType.MARK_ROUNDING, 1, (short) getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        putByte((byte) MESSAGE_VERSION_NUMBER);
        putInt((int) time, 6);
        putInt((int) ackNumber, 2);
        putInt((int) raceId, 4);
        putInt((int) sourceId, 4);
        putByte((byte) boatStatus.getCode());
        putByte((byte) roundingSide.getCode());
        putByte((byte) markId);

        writeCRC();
        rewind();
    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE;
    }
}
