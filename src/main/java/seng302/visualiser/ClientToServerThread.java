package seng302.visualiser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.server.messages.*;
import seng302.model.stream.packets.PacketType;
import seng302.model.stream.packets.StreamPacket;

/**
 * A class describing a single connection to a Server for the purposes of sending and receiving on
 * its own thread.
 */
public class ClientToServerThread implements Runnable {

    /**
     * Functional interface for receiving packets from client socket.
     */
    @FunctionalInterface
    public interface ClientSocketListener {
        void newPacket();
    }

    private class ByteReadException extends Exception {
        private ByteReadException(String message) {
            super(message);
        }
    }

    private static final int LOG_LEVEL = 1;

    private Queue<StreamPacket> streamPackets = new ConcurrentLinkedQueue<>();
    private List<ClientSocketListener> listeners = new ArrayList<>();
    private Thread thread;

    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private Logger logger = LoggerFactory.getLogger(ClientToServerThread.class);

    private int clientId = -1;

//    private Boolean updateClient = true;
    private ByteArrayOutputStream crcBuffer;
    private boolean socketOpen = true;

    /**
     * Constructor for ClientToServerThread which takes in ipAddress and portNumber and attempts to
     * connect to the specified ipAddress and port.
     *
     * Upon successful socket connection, threeWayHandshake will be preformed and the instance will
     * be put on a thread and run immediately.
     *
     * @param ipAddress a string of ip address to be connected to
     * @param portNumber an integer port number
     * @throws IOException SocketConnection if fail to connect to ip address and port number
     * combination
     */
    public ClientToServerThread(String ipAddress, Integer portNumber) throws IOException {
        socket = new Socket(ipAddress, portNumber);
        is = socket.getInputStream();
        os = socket.getOutputStream();

        sendRegistrationRequest();

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
        while(socketOpen) {
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
                        if (streamPackets.size() > 0) {
                            streamPackets.add(new StreamPacket(type, payloadLength, timeStamp, payload));
                        } else {
                            if (PacketType.RACE_REGISTRATION_RESPONSE == PacketType.assignPacketType(type, payload)){
                                processRegistrationResponse(new StreamPacket(type, payloadLength, timeStamp, payload));
                            }
                            else {
                                if (clientId == -1) continue; // Do not continue if not registered
                                streamPackets.add(new StreamPacket(type, payloadLength, timeStamp, payload));
                                for (ClientSocketListener csl : listeners)
                                    csl.newPacket();
                            }
                        }
                    } else {
                        clientLog("Packet has been dropped", 1);
                    }
                }
            } catch (ByteReadException e) {
                closeSocket();
                Platform.runLater(() -> {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setHeaderText("Host has disconnected");
                    alert.setContentText("Cannot find Server");
                    alert.showAndWait();
                });
                clientLog(e.getMessage(), 1);
                return;
            }
//            System.out.println("streamPackets = " + streamPackets.size());
        }
        closeSocket();
        clientLog("Closed connection to Server", 0);
    }


    /**
     * Sends a request to the server asking for a source ID
     */
    private void sendRegistrationRequest() {
        RegistrationRequestMessage requestMessage = new RegistrationRequestMessage(ClientType.PLAYER);

        try {
            os.write(requestMessage.getBuffer());
        } catch (IOException e) {
            logger.error("Could not send registration request. Exiting");
            System.exit(1);
        }
    }

    /**
     * Accepts a response to the registration request message, and updates the client OR quits
     * @param packet The registration requests packet
     */
    private void processRegistrationResponse(StreamPacket packet){
        int sourceId = (int) Message.bytesToLong(Arrays.copyOfRange(packet.getPayload(), 0, 3));
        int statusCode = (int) Message.bytesToLong(Arrays.copyOfRange(packet.getPayload(), 4,5));

        RegistrationResponseStatus status = RegistrationResponseStatus.getResponseStatus(statusCode);

        if (status.equals(RegistrationResponseStatus.SUCCESS_PLAYING)){
            clientId = sourceId;

            return;
        }

        logger.error("Server Denied Connection, Exiting");

        final String alertErrorText;

        if (status.equals(RegistrationResponseStatus.FAILURE_FULL)){
            alertErrorText = "Server is full";
        }
        else{
            alertErrorText = "Could not connect to server";
        }

        Platform.runLater(() -> {
            new Alert(AlertType.ERROR, alertErrorText, ButtonType.OK).showAndWait();
            System.exit(1);
        });
    }


    /**
     * Send the post-start race course information
     * @param boatActionMessage The message to send
     */
    public void sendBoatActionMessage(BoatActionMessage boatActionMessage) {
        if (clientId == -1) return;

        try {
            os.write(boatActionMessage.getBuffer());
        } catch (IOException e) {
            clientLog("Could not write to server", 1);
        }
    }


    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            clientLog("Failed to close the socket", 1);
        }
    }

    public void setSocketToClose () {
        socketOpen = false;
    }

    public Queue<StreamPacket> getPacketQueue () {
        return streamPackets;
    }

    public void addStreamObserver (ClientSocketListener streamListener) {
        listeners.add(streamListener);
    }

    public void removeStreamObserver (ClientSocketListener streamListener) {
        listeners.remove(streamListener);
    }

    private int readByte() throws ByteReadException {
        int currentByte = -1;
        try {
            currentByte = is.read();
            crcBuffer.write(currentByte);
        } catch (IOException e) {
            clientLog("Read byte failed", 1);
        }
        if (currentByte == -1) {
            throw new ByteReadException("InputStream reach end of stream");
        }
        return currentByte;
    }

    private byte[] getBytes(int n) throws ByteReadException {
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++) {
            bytes[i] = (byte) readByte();
        }
        return bytes;
    }

    private void skipBytes(long n) throws ByteReadException {
        for (int i = 0; i < n; i++) {
            readByte();
        }
    }

    public Thread getThread() {
        return thread;
    }

    public int getClientId () {
        return clientId;
    }
}
