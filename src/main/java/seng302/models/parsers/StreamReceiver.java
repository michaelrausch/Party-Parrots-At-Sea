package seng302.models.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


public class StreamReceiver {

    private static ByteArrayOutputStream buffer;
    private static InputStream stream = null;
    private static boolean reading = true;
    private static Collection<StreamPacket> priorityQue = new ArrayList<>();

    private static void skipBytes(long n){
        for (int i=0; i < n; i++){
            readByte();
        }
    }

    private static int readByte() {
        int currentByte = -1;
        try {
            currentByte = stream.read();
            buffer.write(currentByte);
            if (currentByte == -1){
                reading = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentByte;
    }

    private static void runTest() {

        Socket host = null;
//        String hostAddress = "livedata.americascup.com";
        String hostAddress = "csse-s302staff.canterbury.ac.nz";
        int hostPort = 4941;

        try {
            host = new Socket(hostAddress, hostPort);
            stream = host.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int sync1;
        int sync2;
        //currently "reading" will not break the program nicely (because there are multiple readBytes within the while loop)
        while(reading) {
            buffer = new ByteArrayOutputStream();
            sync1 = readByte();
//            System.out.println("sync1 = " + Integer.toBinaryString(sync1));
            sync2 = readByte();
            //checking if it is the start of the packet
            if(sync1 == 0x47 && sync2 == 0x83) {
                int type = readByte();
//                System.out.println("message type: " + type);
                byte[] timeStampBytes = getBytes(6);
                skipBytes(4);

//                byte[] b = new byte[2];
//                try {
//                    stream.read(b);
//                } catch (IOException e){
//                    e.printStackTrace();
//                }
//                System.out.println("b = " + Integer.toBinaryString(b[0]));
//                System.out.println(timeStamp);
                long timeStamp = 0;
                long multiplier=1;
                for(int i = 0;i < 6;i++) {
                    timeStamp += timeStampBytes[i]*multiplier;
                    multiplier *= 256;
                }
                long payloadLength = bytesToLong(getBytes(2));
                //No. of milliseconds since Jan 1st 1970
                System.out.println("timeStamp = " + timeStamp);
//                System.out.println("payload length: " + payloadLength);
                priorityQue.add(new StreamPacket(type, payloadLength, timeStamp, getBytes((int)payloadLength)));
                Checksum checksum = new CRC32();
                checksum.update(buffer.toByteArray(), 0, buffer.size());
//                System.out.println(checksum.getValue());
                long crc = bytesToLong(getBytes(4));
//                System.out.println(crc);
            }
        }

        try {
            if (host != null) {
                host.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static byte[] getBytes(int n){
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++){
            bytes[i] = (byte) readByte();
//            System.out.println(Integer.toBinaryString(bytes[i]));
//            System.out.println(bytes[i]);
        }
        return bytes;
    }

    /**
     * takes an array of up to 4 bytes and returns a positive
     * long constructed from the input bytes
     *
     * (note it is assumed the bytes coming in need to be reversed like those from a stream)
     *
     * @return a positive long if there is less than 4 bytes -1 otherwise
     */
    private static long bytesToLong(byte[] bytes){
        long partialLong = 0;
        int index = 0;
        for (byte b: bytes){
            if (index > 3){
                return -1;
            }
            partialLong = partialLong | (b & 0xFFL) << (index * 8);
            index++;
        }
        return partialLong;
    }


    public static void main(String[] args) {

        runTest();

    }

}
