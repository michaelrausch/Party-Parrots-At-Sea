package seng302.server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.CRC32;

public class RaceStatusMessage extends Message{
    private final MessageType MESSAGE_TYPE = MessageType.RACE_STATUS;
    private final int MESSAGE_VERSION = 2; //Always set to 1
    private final int MESSAGE_BASE_SIZE = 24;

    // fields
    private long currentTime;
    private long raceId;
    private RaceStatus raceStatus;
    private long expectedStartTime;
    private WindDirection raceWindDirection;
    private long windSpeed;
    private long numBoatsInRace;
    private RaceType raceType;
    private List<BoatSubMessage> boats;
    private CRC32 crc;

    /**
     * A message containing the current status of the race
     * @param raceId The ID of the current race
     * @param raceStatus The status of the race
     * @param expectedStartTime The expected start time
     * @param raceWindDirection The wind direction (north, east, south)
     * @param windSpeed The wind speed in mm/sec
     * @param numBoatsInRace The number of boats in the race
     * @param raceType The race type (Match/fleet)
     * @param sourceId The source of this message
     * @param boats A list of boat status sub messages
     */
    public RaceStatusMessage(long raceId, RaceStatus raceStatus, long expectedStartTime, WindDirection raceWindDirection,
                             long windSpeed, long numBoatsInRace, RaceType raceType, long sourceId, List<BoatSubMessage> boats){
        currentTime = System.currentTimeMillis() / 1000L;
        this.raceId = raceId;
        this.raceStatus = raceStatus;
        this.expectedStartTime = expectedStartTime;
        this.raceWindDirection = raceWindDirection;
        this.windSpeed = windSpeed;
        this.numBoatsInRace = numBoatsInRace;
        this.raceType = raceType;
        this.boats = boats;
        crc = new CRC32();

        setHeader(new Header(MESSAGE_TYPE, (int) sourceId, (short) getSize()));
    }

    /**
     * @return the size of this message in bytes
     */
    @Override
    public int getSize() {
        return MESSAGE_BASE_SIZE + (20 * (int) numBoatsInRace);
    }

    /**
     * Send this message as a stream of bytes
     * @param outputStream The output stream to send the message
     */
    @Override
    public void send(DataOutputStream outputStream) {
        ByteBuffer buff = ByteBuffer.allocate(Header.getSize() + getSize() + 4/*CRC*/);

        buff.put(getHeader().getByteBuffer());
        buff.position(Header.getSize());

        // Version Number, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)MESSAGE_VERSION).array());
        buff.position(Header.getSize() + 1);

        // Current time, 2 bytes
        buff.put(ByteBuffer.allocate(6).putInt((int)currentTime).array());
        buff.position(Header.getSize() + 7);

        // Race id, 4 bytes
        buff.put(ByteBuffer.allocate(4).putInt((int)raceId).array());
        buff.position(Header.getSize() + 11);

        // Race status, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)raceStatus.getCode()).array());
        buff.position(Header.getSize() + 12);

        // Expected start time, 6 bytes
        buff.put(ByteBuffer.allocate(6).putInt((int)expectedStartTime).array());
        buff.position(Header.getSize() + 18);

        // Wind direction, 2 bytes
        buff.put(ByteBuffer.allocate(2).putShort((short)raceWindDirection.getCode()).array());
        buff.position(Header.getSize() + 20);

        // Wind Speed, 2 bytes
        buff.put(ByteBuffer.allocate(2).putShort((short)windSpeed).array());
        buff.position(Header.getSize() + 22);

        // Number of boats, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)numBoatsInRace).array());
        buff.position(Header.getSize() + 23);

        // Race Type, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)raceType.getCode()).array());
        buff.position(Header.getSize() + 24);

        int buffPosition = Header.getSize() + 24;

        for (BoatSubMessage boatSubMessage : boats){
            buff.put(boatSubMessage.getByteBuffer());
            buffPosition += boatSubMessage.getSize();
            buff.position(buffPosition);
        }

        // calculate CRC
        crc.update(buff.array());

        // Add CRC to message
        buff.put(ByteBuffer.allocate(4).putInt((short)crc.getValue()).array());

        // Send
        try {
            outputStream.write(buff.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
