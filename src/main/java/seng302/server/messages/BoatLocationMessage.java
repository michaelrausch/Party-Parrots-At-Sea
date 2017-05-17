package seng302.server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class BoatLocationMessage extends Message {
    private final int MESSAGE_SIZE = 56;

    private long messageVersionNumber;
    private long time;
    private long sourceId;
    private long sequenceNum;
    private DeviceType deviceType;
    private double latitude;
    private double longitude;
    private long altitude;
    private Double heading;
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
    public BoatLocationMessage(int sourceId, int sequenceNum, double latitude, double longitude, double heading, long boatSpeed){
        boatSpeed /= 10;
        messageVersionNumber = 1;
        time = System.currentTimeMillis();
        this.sourceId = sourceId;
        this.sequenceNum = sequenceNum;
        this.deviceType = DeviceType.RACING_YACHT;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = 0;
        this.heading = heading;
        this.pitch = 0;
        this.roll = 0;
        this.boatSpeed = boatSpeed;
        this.COG = 2;
        this.SOG = boatSpeed ;
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

    /**
     * Convert binary latitude or longitude to floating point number
     * @param binaryPackedLatLon Binary packed lat OR lon
     * @return Floating point lat/lon
     */
    public static double binaryPackedToLatLon(long binaryPackedLatLon){
        return (double)binaryPackedLatLon * 180.0 / 2147483648.0;
    }

    /**
     * Convert binary packed heading to floating point number
     * @param binaryPackedHeading Binary packed heading
     * @return heading as a decimal
     */
    public static double binaryPackedHeadingToDouble(long binaryPackedHeading){
        return (double)binaryPackedHeading * 360.0 / 65536.0;
    }

    /**
     * Convert binary packed wind angle to floating point number
     * @param binaryPackedWindAngle Binary packed wind angle
     * @return wind angle as a decimal
     */
    public static double binaryPackedWindAngleToDouble(long binaryPackedWindAngle){
        return (double)binaryPackedWindAngle*180.0/32768.0;
    }

    /**
     * Convert a latitude or longitude to a binary packed long
     * @param latLon A floating point latitude/longitude
     * @return A binary packed lat/lon
     */
    public static long latLonToBinaryPackedLong(double latLon){
        return (long)((536870912 * latLon) / 45);
    }

    /**
     * Convert a heading to a binary packed long
     * @param heading A floating point heading
     * @return A binary packed heading
     */
    public static long headingToBinaryPackedLong(double heading){
        return (long)((8192*heading)/45);
    }

    /**
     * Convert a wind angle to a binary packed long
     * @param windAngle Floating point wind angle
     * @return A binary packed wind angle
     */
    public static long windAngleToBinaryPackedLong(double windAngle){
        return (long)((8192*windAngle)/45);
    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE;
    }


    @Override
    public void send(SocketChannel outputStream) throws IOException{
        allocateBuffer();
        writeHeaderToBuffer();

        long headingToSend = (long)((heading/360.0) * 65535.0);

        putByte((byte) messageVersionNumber);
        putInt(time, 6);
        putInt((int) sourceId, 4);
        putUnsignedInt((int) sequenceNum, 4);
        putByte((byte) deviceType.getCode());
        putInt((int) latLonToBinaryPackedLong(latitude), 4);
        putInt((int) latLonToBinaryPackedLong(longitude), 4);
        putInt((int) altitude, 4);
        putInt(headingToSend, 2);
        putInt((int) pitch, 2);
        putInt((int) roll, 2);
        putInt((int) boatSpeed, 2);
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
        rewind();

        outputStream.write(getBuffer());
    }
}
