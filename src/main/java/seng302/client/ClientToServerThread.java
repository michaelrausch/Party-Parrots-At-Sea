package seng302.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import seng302.models.stream.packets.StreamPacket;
import seng302.server.messages.BoatActionMessage;
import seng302.server.messages.Message;

/**
 * A class describing a single connection to a Server for the purposes of sending and receiving on
 * its own thread.
 */
public class ClientToServerThread implements Runnable {

    private static final int LOG_LEVEL = 1;

    private Thread thread;

    private Integer ourID;

    private Socket socket;
    private InputStream is;
    private OutputStream os;

    private Boolean updateClient = true;
    private ByteArrayOutputStream crcBuffer;

    /**
     * Constructor for ClientToServerThread which takes in ipAddress and portNumber and attempts to
     * connect to the specified ipAddress and port.
     *
     * Upon successful socket connection, threeWayHandshake will be preformed and the instance will
     * be put on a thread and run immediately.
     *
     * @param ipAddress a string of ip address to be connected to
     * @param portNumber an integer port number
     * @throws Exception SocketConnection if fail to connect to ip address and port number
     * combination
     */
    public ClientToServerThread(String ipAddress, Integer portNumber) throws Exception {
        socket = new Socket(ipAddress, portNumber);
        is = socket.getInputStream();
        os = socket.getOutputStream();

        Integer allocatedID = threeWayHandshake();
        if (allocatedID != null) {
            ourID = allocatedID;
            clientLog("Successful handshake. Allocated ID: " + ourID, 0);
            ClientState.setClientSourceId(String.valueOf(ourID));
        } else {
            clientLog("Unsuccessful handshake", 1);
            closeSocket();
            return;
        }

        thread = new Thread(this);
        thread.start();
    }

    /**
     * Prints out log messages and the time happened.
     * Only perform task if log level is below LOG_LEVEL variable.
     *
     * @param message a string of message to be printed out
     * @param logLevel an int for log level
     */
    static void clientLog(String message, int logLevel) {
        if (logLevel <= LOG_LEVEL) {
            System.out.println(
                "[CLIENT " + LocalDateTime.now().toLocalTime().toString() + "] " + message);
        }
    }

    /**
     * Perform the thread loop. It exits the loop if ClientState connected to host
     * variable is false.
     */
    public void run() {
        int sync1;
        int sync2;
        // TODO: 14/07/17 wmu16 - Work out how to fix this while loop
        while (ClientState.isConnectedToHost()) {
            try {
                crcBuffer = new ByteArrayOutputStream();
                sync1 = readByte();
                sync2 = readByte();
                //checking if it is the start of the packet
                if (sync1 == 0x47 && sync2 == 0x83) {
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
                    } else {
                        clientLog("Packet has been dropped", 1);
                    }
                }
            } catch (Exception e) {
                closeSocket();
                Platform.runLater(new Runnable() {
                                      @Override
                                      public void run() {
                                          Alert alert = new Alert(AlertType.ERROR);
                                          alert.setHeaderText("Host has disconnected");
                                          alert.setContentText("Cannot find Server");
                                          alert.showAndWait();
                                      }
                                  });
                    clientLog("Disconnected from server", 1);
                return;
            }
        }
        closeSocket();
        clientLog("Disconnected from server", 0);
    }


    /**
     * Listens for an allocated sourceID and returns it to the server
     *
     * @return the sourceID allocated to us by the server
     */
    private Integer threeWayHandshake() {
        Integer ourSourceID = null;
        while (true) {
            try {
                ourSourceID = is.read();
            } catch (IOException e) {
                clientLog("Three way handshake failed", 1);

            }
            if (ourSourceID != null) {
                try {
                    os.write(ourSourceID);
                    return ourSourceID;
                } catch (IOException e) {
                    clientLog("Three way handshake failed", 1);
                    return null;
                }
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
            clientLog("Could not write to server", 1);
        }
    }


    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            clientLog("Failed to close the socket", 1);
        }
    }


    private int readByte() throws Exception {
        int currentByte = -1;
        try {
            currentByte = is.read();
            crcBuffer.write(currentByte);
        } catch (IOException e) {
            clientLog("Read byte failed", 1);
        }
        if (currentByte == -1) {
            throw new Exception();
        }
        return currentByte;
    }

    private byte[] getBytes(int n) throws Exception {
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++) {
            bytes[i] = (byte) readByte();
        }
        return bytes;
    }

    private void skipBytes(long n) throws Exception {
        for (int i = 0; i < n; i++) {
            readByte();
        }
    }

    public Thread getThread() {
        return thread;
    }
}
