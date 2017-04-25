package seng302.models.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


public class StreamReceiver {
    private InputStream stream;
    private Socket host;
    private  ByteArrayOutputStream crcBuffer;
    public PriorityBlockingQueue<StreamPacket> packetBuffer;

    public StreamReceiver(String hostAddress, int hostPort, PriorityBlockingQueue packetBuffer) {
        try {
            host = new Socket(hostAddress, hostPort);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.packetBuffer = packetBuffer;
    }

    public void connect(){
        try {
            stream = host.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int sync1;
        int sync2;
        boolean moreBytes = true;
        while(moreBytes) {
            try {
                crcBuffer = new ByteArrayOutputStream();
                sync1 = readByte();
                sync2 = readByte();
                //checking if it is the start of the packet
                if(sync1 == 0x47 && sync2 == 0x83) {
                    int type = readByte();
                    //No. of milliseconds since Jan 1st 1970
                    long timeStamp = bytesToLong(getBytes(6));
                    skipBytes(4);
                    long payloadLength = bytesToLong(getBytes(2));
                    byte[] payload = getBytes((int) payloadLength);
                    Checksum checksum = new CRC32();
                    checksum.update(crcBuffer.toByteArray(), 0, crcBuffer.size());
                    long computedCrc = checksum.getValue();
                    long packetCrc = bytesToLong(getBytes(4));
                    if (computedCrc == packetCrc) {
//                        System.out.println("message type: " + type);
//                        System.out.println("timeStamp = " + timeStamp);
//                        System.out.println("payload length: " + payloadLength);
                        packetBuffer.add(new StreamPacket(type, payloadLength, timeStamp, payload));
                    } else {
                        System.err.println("Packet has been dropped");
                    }
                }
            } catch (Exception e) {
                moreBytes = false;
            }

        }
    }

    private int readByte() throws Exception {
        int currentByte = -1;
        try {
            currentByte = stream.read();
            crcBuffer.write(currentByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (currentByte == -1){
            throw new Exception();
        }
        return currentByte;
    }

    private byte[] getBytes(int n) throws Exception{
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++){
            bytes[i] = (byte) readByte();
        }
        return bytes;
    }


    private void skipBytes(long n) throws Exception{
        for (int i=0; i < n; i++){
            readByte();
        }
    }

    /**
     * takes an array of up to 7 bytes and returns a positive
     * long constructed from the input bytes
     *
     * @return a positive long if there is less than 7 bytes -1 otherwise
     */
    private long bytesToLong(byte[] bytes){
        long partialLong = 0;
        int index = 0;
        for (byte b: bytes){
            if (index > 6){
                return -1;
            }
            partialLong = partialLong | (b & 0xFFL) << (index * 8);
            index++;
        }
        return partialLong;
    }


    public static void main(String[] args) {
        PriorityBlockingQueue<StreamPacket> pq = new PriorityBlockingQueue<>(256, new Comparator<StreamPacket>() {
            @Override
            public int compare(StreamPacket s1, StreamPacket s2) {
                return (int) (s1.getTimeStamp() - s2.getTimeStamp());
            }
        });
        StreamReceiver sr = new StreamReceiver("csse-s302staff.canterbury.ac.nz", 4941, pq);
//        StreamReceiver sr = new StreamReceiver("livedata.americascup.com", 4941, pq);
        sr.connect();
    }
}
