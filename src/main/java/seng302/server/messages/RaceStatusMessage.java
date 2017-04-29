package seng302.server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.zip.CRC32;

public class RaceStatusMessage extends Message{
    private final MessageType MESSAGE_TYPE = MessageType.RACE_STATUS;
    private final int MESSAGE_VERSION = 2; //Always set to 1
    private final int MESSAGE_BASE_SIZE = 24;

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
        return MESSAGE_BASE_SIZE + (20 * ((int) numBoatsInRace));
    }

    /**
     * Send this message as a stream of bytes
     * @param outputStream The output stream to send the message
     */
    @Override
    public void send(SocketChannel outputStream) throws IOException {
        allocateBuffer();
        writeHeaderToBuffer();

        putByte((byte) MESSAGE_VERSION);
        putInt((int) currentTime, 6);
        putInt((int) raceId, 4);
        putByte((byte) raceStatus.getCode());
        putInt((int) expectedStartTime, 6);
        putInt((int) raceWindDirection.getCode(), 2);
        putInt((int) windSpeed, 2);
        putByte((byte) numBoatsInRace);
        putByte((byte) raceType.getCode());

        for (BoatSubMessage boatSubMessage : boats){
            putBytes(boatSubMessage.getByteBuffer(), boatSubMessage.getSize());
        }

        writeCRC();

        outputStream.write(getBuffer());
    }
}
