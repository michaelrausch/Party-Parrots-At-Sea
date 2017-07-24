package seng302.model.stream;

import seng302.model.stream.packets.StreamPacket;

import java.io.*;
import java.net.Socket;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;


public class StreamReceiver extends Thread {
    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket host;
    private ByteArrayOutputStream crcBuffer;
    private Thread t;
    private String threadName;
    public static PriorityBlockingQueue<StreamPacket> packetBuffer;
    private static boolean moreBytes;

    public StreamReceiver(String hostAddress, int hostPort, String threadName) {
        this.threadName = threadName;
        this.setDaemon(true);
        try {
            host = new Socket(hostAddress, hostPort);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run(){
        PriorityBlockingQueue<StreamPacket> pq = new PriorityBlockingQueue<>(256, new Comparator<StreamPacket>() {
            @Override
            public int compare(StreamPacket s1, StreamPacket s2) {
                return (int) (s1.getTimeStamp() - s2.getTimeStamp());
            }
        });
        packetBuffer = pq;
        connect();
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }


    public StreamReceiver(Socket host,  PriorityBlockingQueue packetBuffer){
        this.host=host;
        this.packetBuffer = packetBuffer;
    }


    public void connect(){

//        int sync1;
//        int sync2;
//        moreBytes = true;
//        while(moreBytes) {
//            try {
//                crcBuffer = new ByteArrayOutputStream();
//                sync1 = readByte();
//                sync2 = readByte();
//                //checking if it is the start of the packet
//                if(sync1 == 0x47 && sync2 == 0x83) {
//                    int type = readByte();
//                    //No. of milliseconds since Jan 1st 1970
//                    long timeStamp = bytesToLong(getBytes(6));
//                    skipBytes(4);
//                    long payloadLength = bytesToLong(getBytes(2));
//                    byte[] payload = getBytes((int) payloadLength);
//                    Checksum checksum = new CRC32();
//                    checksum.update(crcBuffer.toByteArray(), 0, crcBuffer.size());
//                    long computedCrc = checksum.getValue();
//                    long packetCrc = bytesToLong(getBytes(4));
//                    if (computedCrc == packetCrc) {
//                        packetBuffer.add(new StreamPacket(type, payloadLength, timeStamp, payload));
//                    } else {
//                        System.err.println("Packet has been dropped");
//                    }
//                }
//            } catch (Exception e) {
//                moreBytes = false;
//            }
//        }
    }

    private int readByte() throws Exception {
        int currentByte = -1;
        try {
            currentByte = inputStream.read();
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

    public static void main(String[] args) {

        StreamReceiver sr = new StreamReceiver("csse-s302staff.canterbury.ac.nz", 4941,"TestThread1");
        //StreamReceiver sr = new StreamReceiver("livedata.americascup.com", 4941, "TestThread2");
        sr.start();

    }

    public static void noMoreBytes(){
        moreBytes = false;
    }
}
