package seng302.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import seng302.models.stream.packets.StreamPacket;
import seng302.server.messages.BoatActionMessage;
import seng302.server.messages.Message;

/**
 * Created by kre39 on 13/07/17.
 */
public class ClientToServerThread extends Thread {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private static final int LOG_LEVEL = 1;

    private Boolean updateClient = true;
    private  ByteArrayOutputStream crcBuffer;

    public ClientToServerThread(String ipAddress, Integer portNumber){
        try {
            socket = new Socket(ipAddress, portNumber);
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static void clientLog(String message, int logLevel){
        if(logLevel <= LOG_LEVEL){
            System.out.println("[CLIENT] " + message);
        }
    }

    public void run() {
        int sync1;
        int sync2;
        // TODO: 14/07/17 wmu16 - Work out how to fix this while loop
        while(true) {
            try {
                //Perform a write if it is time to as delegated by the MainServerThread
                if (updateClient) {
                    // TODO: 13/07/17 wmu16 - Write out game state - some function that would write all appropriate messages to this output stream
//                try {
//                    GameState.outputState(os);
//                } catch (IOException e) {
//                    System.out.println("IO error in server thread upon writing to output stream");
//                }
                    updateClient = false;
                }

                crcBuffer = new ByteArrayOutputStream();
                sync1 = readByte();
                sync2 = readByte();
                //checking if it is the start of the packet
                if(sync1 == 0x47 && sync2 == 0x83) {
                    int type = readByte();
                    //No. of milliseconds since Jan 1st 1970
                    long timeStamp = Message.bytesToLong(getBytes(6));
                    skipBytes(4);
                    long payloadLength = Message.bytesToLong(getBytes(2));
                    byte[] payload = getBytes((int) payloadLength);
                    Checksum checksum = new CRC32();
                    checksum.update(crcBuffer.toByteArray(), 0, crcBuffer.size());
                    long computedCrc = checksum.getValue();
                    long packetCrc = Message.bytesToLong(getBytes(4));
                    if (computedCrc == packetCrc) {
                        ClientPacketParser
                            .parsePacket(new StreamPacket(type, payloadLength, timeStamp, payload));
                        // TODO: 17/07/17 wmu16 - Fix this or maybe we dont need to go through the main server at all!?!?
//                        packetBufferDelegate.addToBuffer(new StreamPacket(type, payloadLength, timeStamp, payload));
                    } else {
                        System.err.println("Packet has been dropped");
                    }
                }
            } catch (Exception e) {
                closeSocket();
                return;
            }
        }

    }

    /**
     * Send the post-start race course information
     */
    public void sendBoatActionMessage(BoatActionMessage boatActionMessage) {
        try {
            os.write(boatActionMessage.getBuffer());
        } catch (IOException e) {
            clientLog("COULD NOT WRITE TO SERVER", 0);
            e.printStackTrace();
        }
    }


    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("IO error in server thread upon trying to close socket");
        }
    }


    private int readByte() throws Exception {
        int currentByte = -1;
        try {
            currentByte = is.read();
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
 }
