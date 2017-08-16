package seng302.gameServer.messages;

import java.nio.ByteBuffer;

/**
 * The status of each boat, sent within a race status message
 */
public class BoatSubMessage{
    private final int MESSAGE_SIZE = 20;

    private long sourceId;
    private BoatStatus boatStatus;
    private long legNumber;
    private long numberPenaltiesAwarded;
    private long numberPenaltiesServed;
    private long estimatedTimeAtNextMark;
    private long estimatedTimeAtFinish;
    private ByteBuffer buff = ByteBuffer.allocate(getSize());
    private int buffPos = 0;

    /**
     * Boat Sub message from section 4.2 of the AC35 streaming data interface spec
     * @param sourceId The source ID of the boat
     * @param boatStatus The boats status
     * @param legNumber The leg the boat is on (0= prestart, 1=start to first mark etc)
     * @param numberPenaltiesAwarded The number of penalties awarded to the boat
     * @param numberPenaltiesServed The number of penalties served to the boat
     * @param estimatedTimeAtFinish The estimated time (UTC) the boat will finish the race
     * @param estimatedTimeAtNextMark The estimated time (UTC) the boat will arrive at the next mark
     */
    public BoatSubMessage(long sourceId, BoatStatus boatStatus, long legNumber, long numberPenaltiesAwarded, long numberPenaltiesServed,
                   long estimatedTimeAtFinish, long estimatedTimeAtNextMark){
        this.sourceId = sourceId;
        this.boatStatus = boatStatus;
        this.legNumber = legNumber;
        this.numberPenaltiesAwarded = numberPenaltiesAwarded;
        this.numberPenaltiesServed = numberPenaltiesServed;
        this.estimatedTimeAtFinish = estimatedTimeAtFinish;
        this.estimatedTimeAtNextMark = estimatedTimeAtNextMark;
    }

    /**
     * @return The size of this message in bytes
     */
    public int getSize(){
        return MESSAGE_SIZE;
    }

    private void putInBuffer(byte[] bytes, long val){
        byte[] tmp = bytes.clone();
        Message.reverse(tmp);

        buff.put(tmp);
        buffPos += tmp.length;
        buff.position(buffPos);
    }

    /**
     * @return a ByteBuffer containing this boat status message
     */
    public ByteBuffer getByteBuffer(){
        // Source ID, 4 bytes
        putInBuffer(Message.intToByteArray(sourceId, 4), 4);

        // Boat Status, 1 byte
        putInBuffer(ByteBuffer.allocate(1).put((byte) (boatStatus.getCode() & 0xff)).array(), 1);

        // Leg number, 1 byte
        putInBuffer(ByteBuffer.allocate(1).put((byte) (legNumber & 0xff)).array(), 1);

        // Number of penalties awarded, 1 byte
        putInBuffer(ByteBuffer.allocate(1).put((byte) (numberPenaltiesAwarded & 0xff)).array(), 1);

        // Number of penalties served, 1 byte
        putInBuffer(ByteBuffer.allocate(1).put((byte) (numberPenaltiesServed & 0xff)).array(), 1);

        // Estimated time at next mark, 6 bytes
        putInBuffer(Message.intToByteArray((int) estimatedTimeAtNextMark, 6),6);

        // Estimated time at finish, 6 bytes
        putInBuffer(Message.intToByteArray((int) estimatedTimeAtFinish, 6), 6);

        return buff;
    }
}
