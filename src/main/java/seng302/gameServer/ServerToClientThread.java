package seng302.gameServer;


import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.messages.*;
import org.w3c.dom.Document;
import seng302.gameServer.messages.BoatAction;
import seng302.gameServer.messages.ChatterMessage;
import seng302.gameServer.messages.ClientType;
import seng302.gameServer.messages.CustomizeRequestType;
import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.RegistrationResponseMessage;
import seng302.gameServer.messages.RegistrationResponseStatus;
import seng302.model.Player;
import seng302.model.ServerYacht;
import seng302.model.stream.packets.PacketType;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.utilities.StreamParser;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;
import seng302.visualiser.fxObjects.assets_3D.BoatMeshType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * A class describing a single connection to a Client for the purposes of sending and receiving on
 * its own thread. All server threads created and owned by the server thread handler which can
 * trigger client updates on its threads Created by wmu16 on 13/07/17.
 */
public class ServerToClientThread implements Runnable {

    /**
     * Called to notify listeners when this thread receives a connection correctly.
     */
    @FunctionalInterface
    interface ConnectionListener {
        void notifyConnection ();
    }

    // TODO: 17/08/17 this is only temporary disconnects should be handled consistently
    @FunctionalInterface
    interface DisconnectListener {
        void notifyDisconnect (Player player);
    }

    private Logger logger = LoggerFactory.getLogger(ServerToClientThread.class);

    private Thread thread;

    private InputStream is;
    private OutputStream os;
    private Socket socket;

    private ByteArrayOutputStream crcBuffer;

    private Integer seqNo;
    private Integer sourceId;

    private ClientType clientType;
    private Boolean isRegistered = false;
    private Boolean isHost = false;

    private XMLGenerator xmlGenerator;

    private List<ConnectionListener> connectionListeners = new ArrayList<>();
    private DisconnectListener disconnectListener;

    private Player player;

    private SimpleObjectProperty<RaceXMLData> raceXMLProperty = new SimpleObjectProperty<>();
    private SimpleObjectProperty<RegattaXMLData> regattaXMLProperty = new SimpleObjectProperty<>();

    public ServerToClientThread(Socket socket) {
        this.socket = socket;
        seqNo = 0;

        try{
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            return;
        }

        thread = new Thread(this, "ServerToClient");
        thread.start();
    }

    public Integer getSourceId() {
        return sourceId;
    }

    private void setUpPlayer(){
        String shortName = "P" + sourceId;
        String longName = "Player " + sourceId;

        ServerYacht yacht = new ServerYacht(
            BoatMeshType.DINGHY, sourceId, sourceId.toString(), shortName, longName, "NZ");

        player = new Player(socket, yacht);
        GameState.addYacht(sourceId, yacht);
        GameState.addPlayer(player);
    }

    private void completeRegistration(ClientType clientType) throws IOException {
        // Fail if not a player
        if (!clientType.equals(ClientType.PLAYER)){
            RegistrationResponseMessage responseMessage = new RegistrationResponseMessage(0, RegistrationResponseStatus.FAILURE_GENERAL);
            os.write(responseMessage.getBuffer());
            return;
        }

        if (GameState.getPlayers().size() >= GameState.getCapacity()){
            RegistrationResponseMessage responseMessage = new RegistrationResponseMessage(0, RegistrationResponseStatus.FAILURE_FULL);
            os.write(responseMessage.getBuffer());
            return;
        }

        Integer sourceId = GameState.getUniquePlayerID();
        RegistrationResponseMessage responseMessage = new RegistrationResponseMessage(sourceId, RegistrationResponseStatus.SUCCESS_PLAYING);

        this.clientType = clientType;
        this.sourceId = sourceId;
        isRegistered = true;
        os.write(responseMessage.getBuffer());

        setUpPlayer();

        for (ConnectionListener listener : connectionListeners) {
            listener.notifyConnection();
        }
    }

    public void run() {
        int sync1;
        int sync2;
        // TODO: 14/07/17 wmu16 - Work out how to fix this while loop

        while (socket.isConnected() && !socket.isClosed()) {
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
                        StreamPacket packet = new StreamPacket(type, payloadLength, timeStamp, payload);
                        switch (PacketType.assignPacketType(type, payload)) {
                            case BOAT_ACTION:
                                BoatAction actionType = ServerPacketParser.extractBoatAction(packet);
                                GameState.updateBoat(sourceId, actionType);
                                break;

                            case RACE_REGISTRATION_REQUEST:
                                ClientType requestedType = ServerPacketParser
                                    .extractClientType(packet);
                                completeRegistration(requestedType);
                                break;
                            case CHATTER_TEXT:
                                ChatterMessage chatterMessage = ServerPacketParser
                                    .extractChatterText(packet);
                                GameState.processChatter(chatterMessage, isHost);
                                break;
                            case RACE_CUSTOMIZATION_REQUEST:
                                Long sourceID = Message.bytesToLong(
                                    Arrays.copyOfRange(payload, 0, 3)
                                );
                                CustomizeRequestType requestType = ServerPacketParser
                                    .extractCustomizationType(packet);

                                GameState.customizePlayer(sourceID, requestType,
                                    Arrays.copyOfRange(payload, 6, payload.length)
                                );
                                GameState.setCustomizationFlag();
                                // TODO: 17/08/2017 ajm412: Send a response packet here, not really necessary until we do shapes.
                                break;
                            case RACE_XML:
                                Document document = StreamParser.extractXmlMessage(packet);
                                raceXMLProperty.set(
                                    XMLParser.parseRace(document)
                                );
                                GameState.setMaxPlayers(XMLParser.getMaxPlayers(document));
                                GameState.setTokensEnabled(XMLParser.tokensEnabled(document));
                                break;
                            case REGATTA_XML:
                                regattaXMLProperty.set(
                                    XMLParser.parseRegatta(
                                        StreamParser.extractXmlMessage(packet)
                                    )
                                );
                                break;

                        }
                    } else {
                        logger.warn("Packet has been dropped", 1);
                    }
                }
            } catch (Exception e) {
                closeSocket();
                GameState.setPlayerHasLeftFlag(true);
                return;
            }
        }
        GameState.setPlayerHasLeftFlag(true);
        logger.warn("Closed serverToClientThread" + thread, 1);
    }

    public void sendSetupMessages() {
        sendMessage(MessageFactory.getRegattaXML());
        sendMessage(MessageFactory.getBoatXML());
        sendMessage(MessageFactory.getRaceXML());
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("IO error in server thread upon trying to close socket");
        }
    }

    public Boolean isSocketOpen() {
        return !socket.isClosed();
    }

    private int readByte() throws Exception {
        int currentByte = -1;
        try {
            currentByte = is.read();
            crcBuffer.write(currentByte);
        } catch (SocketException se) {
            disconnectListener.notifyDisconnect(this.player);
        } catch (IOException e) {
            disconnectListener.notifyDisconnect(this.player);
            logger.warn("Socket read failed", 1);
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

    public void sendMessage(Message message) {
        try {
            os.write(message.getBuffer());
        } catch (SocketException e) {
            logger.warn("Player " + sourceId + " side socket disconnected", 1);
        } catch (IOException e) {
            logger.warn("Message send failed", 1);
        }
    }

    private int getSeqNo() {
        seqNo++;
        return seqNo;
    }

    public Thread getThread() {
        return thread;
    }

    public Socket getSocket() {
        return socket;
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    public void terminate () {
        try {
            socket.close();
        } catch (IOException ioe) {
            logger.warn("IOException attempting to terminate serverToClientThread " + this.thread);
        }
    }

    public void addDisconnectListener(DisconnectListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }

    public void setAsHost() {
        isHost = true;
    }

    public SimpleObjectProperty<RaceXMLData> raceXMLProperty() {
        return raceXMLProperty;
    }

    public SimpleObjectProperty<RegattaXMLData> regattaXMLProperty() {
        return regattaXMLProperty;
    }
}
