package seng302.server.messages;

import java.io.DataOutputStream;
import java.io.IOException;

public class BoatLocationMessage extends Message {
    private final int MESSAGE_SIZE = 56;

    private long messageVersionNumber;
    private long time;
    private long sourceId;
    private long sequenceNum;
    private DeviceType deviceType;
    private long latitude;
    private long longitude;
    private long altitude;
    private long heading;
    private long pitch;
    private long roll;
    private long boatSpeed;
    private long COG;
    private long SOG;
    private long apparentWindSpeed;
    private long apparentWindAngle;
    private long trueWindSpeed;
    private long trueWindDirection;
    private long trueWindAngle;
    private long currentDrift;
    private long currentSet;
    private long rudderAngle;

    /**
     * Describes the location, altitude and sensor data from the boat.
     * @param sourceId ID of the boat
     * @param sequenceNum Sequence number of the message
     * @param latitude The boats latitude
     * @param longitude The boats longitude
     * @param heading The boats heading
     * @param boatSpeed The boats speed
     */
    public BoatLocationMessage(int sourceId, int sequenceNum, long latitude, long longitude, long heading, long boatSpeed){
        messageVersionNumber = 1;
        time = System.currentTimeMillis() / 1000L;
        this.sourceId = sourceId;
        this.sequenceNum = sequenceNum;
        this.deviceType = DeviceType.RACING_YACHT;
        this.latitude = -latitude;
        this.longitude = longitude;
        this.altitude = 0;
        this.heading = heading;
        this.pitch = 0;
        this.roll = 0;
        this.boatSpeed = boatSpeed;
        this.COG = 0;
        this.SOG = 0;
        this.apparentWindSpeed = 0;
        this.apparentWindAngle = 0;
        this.trueWindSpeed = 0;
        this.trueWindDirection = 0;
        this.trueWindAngle = 0;
        this.currentDrift = 0;
        this.currentSet = 0;
        this.rudderAngle = 0;

        setHeader(new Header(MessageType.BOAT_LOCATION, 1, (short) getSize()));
    }

    @Override
    public int getSize() {
        return 56;
    }

    @Override
    public void send(DataOutputStream outputStream) {
        allocateBuffer();
        writeHeaderToBuffer();

        putByte((byte) messageVersionNumber);
        putInt((int) time, 6);
        putInt((int) sourceId, 4);
        putUnsignedInt((int) sequenceNum, 4);
        putByte((byte) deviceType.getCode());
        putInt((int) latitude, 4);
        putInt((int) longitude, 4);
        putInt((int) altitude, 4);
        putUnsignedInt((int) heading, 2);
        putInt((int) pitch, 2);
        putInt((int) roll, 2);
        putUnsignedInt((int) boatSpeed, 2);
        putUnsignedInt((int) COG, 2);
        putUnsignedInt((int) SOG, 2);
        putUnsignedInt((int) apparentWindSpeed, 2);
        putInt((int) apparentWindAngle, 2);
        putUnsignedInt((int) trueWindSpeed, 2);
        putUnsignedInt((int) trueWindDirection, 2);
        putInt((int) trueWindAngle, 2);
        putUnsignedInt((int) currentDrift, 2);
        putUnsignedInt((int) currentSet, 2);
        putInt((int) rudderAngle, 2);


        writeCRC();
        try {
            outputStream.write(getBuffer().array());
        } catch (IOException e) {
            System.out.print("");
        }
    }
}
