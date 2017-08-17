package seng302.gameServer;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.server.messages.BoatAction;
import seng302.gameServer.server.messages.BoatLocationMessage;
import seng302.gameServer.server.messages.ClientType;
import seng302.gameServer.server.messages.CustomizeRequestType;
import seng302.gameServer.server.messages.Message;
import seng302.gameServer.server.messages.RegistrationResponseMessage;
import seng302.gameServer.server.messages.RegistrationResponseStatus;
import seng302.gameServer.server.messages.XMLMessage;
import seng302.gameServer.server.messages.XMLMessageSubType;
import seng302.gameServer.server.messages.YachtEventCodeMessage;
import seng302.model.Player;
import seng302.model.ServerYacht;
import seng302.model.stream.packets.PacketType;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.xml.generator.Race;
import seng302.model.stream.xml.generator.Regatta;
import seng302.utilities.XMLGenerator;

/**
 * A class describing a single connection to a Client for the purposes of sending and receiving on
 * its own thread. All server threads created and owned by the server thread handler which can
 * trigger client updates on its threads Created by wmu16 on 13/07/17.
 */
public class ServerToClientThread implements Runnable, Observer {

    /**
     * Called to notify listeners when this thread receives a connection correctly.
     */
    @FunctionalInterface
    interface ConnectionListener {
        void notifyConnection ();
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

    private XMLGenerator xml;

    private List<ConnectionListener> connectionListeners = new ArrayList<>();

    private ServerYacht yacht;

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

    private void setUpPlayer(){
        BufferedReader fn;
        String fName = "";
        BufferedReader ln;
        String lName = "";

        fn = new BufferedReader(
                new InputStreamReader(
                        ServerToClientThread.class.getResourceAsStream(
                                "/server_config/CSV_Database_of_First_Names.csv"
                        )
                )
        );
        List<String> all = fn.lines().collect(Collectors.toList());
        fName = all.get(ThreadLocalRandom.current().nextInt(0, all.size()));
        ln = new BufferedReader(
                new InputStreamReader(
                        ServerToClientThread.class.getResourceAsStream(
                                "/server_config/CSV_Database_of_Last_Names.csv"
                        )
                )
        );
        all = ln.lines().collect(Collectors.toList());
        lName = all.get(ThreadLocalRandom.current().nextInt(0, all.size()));

        ServerYacht yacht = new ServerYacht(
                "Yacht", sourceId, sourceId.toString(), fName, fName + " " + lName, "NZ"
        );

        yacht.addObserver(this); // TODO: yacht can notify mark rounding message hyi25 13/8/17
        GameState.addYacht(sourceId, yacht);
        GameState.addPlayer(new Player(socket, yacht));
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null) {
            sendMessage((Message) arg);
        } else {
            sendSetupMessages();
        }
    }

    private void completeRegistration(ClientType clientType) throws IOException {
        // Fail if not a player
        if (!clientType.equals(ClientType.PLAYER)){
            RegistrationResponseMessage responseMessage = new RegistrationResponseMessage(0, RegistrationResponseStatus.FAILURE_GENERAL);
            os.write(responseMessage.getBuffer());
            return;
        }

        if (GameState.getPlayers().size() >= GameState.MAX_PLAYERS){
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

        while (socket.isConnected()) {

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
                        switch (PacketType.assignPacketType(type, payload)) {
                            case BOAT_ACTION:
                                BoatAction actionType = ServerPacketParser
                                        .extractBoatAction(
                                                new StreamPacket(type, payloadLength, timeStamp, payload));
                                GameState.updateBoat(sourceId, actionType);
                                break;

                            case RACE_REGISTRATION_REQUEST:
                                ClientType requestedType = ServerPacketParser.extractClientType(
                                        new StreamPacket(type, payloadLength, timeStamp, payload));

                                completeRegistration(requestedType);
                                break;

                            case RACE_CUSTOMIZATION_REQUEST:
                                Long sourceID = Message
                                    .bytesToLong(Arrays.copyOfRange(payload, 0, 3));
                                CustomizeRequestType requestType = ServerPacketParser
                                    .extractCustomizationType(
                                        new StreamPacket(type, payloadLength, timeStamp, payload));
                                GameState.customizePlayer(sourceID, requestType,
                                    Arrays.copyOfRange(payload, 6, payload.length));
                                GameState.setCustomizationFlag();
                                // TODO: 17/08/2017 ajm412: Send a response packet here, not really necessary until we do shapes.
                                break;
                        }
                    } else {
                        logger.warn("Packet has been dropped", 1);
                    }
                }
            } catch (Exception e) {
                closeSocket();
                return;
            }
        }
    }

    public void sendSetupMessages() {
        xml = new XMLGenerator();
        Race race = new Race();

        for (ServerYacht yacht : GameState.getYachts().values()) {
            race.addBoat(yacht);
        }

        //@TODO calculate lat/lng values
        xml.setRegatta(new Regatta("Party Parrot Test Server", "Bermuda Test Course",  57.6679590, 11.8503233));
        xml.setRace(race);

        XMLMessage xmlMessage;
        xmlMessage = new XMLMessage(xml.getRegattaAsXml(), XMLMessageSubType.REGATTA,
                xml.getRegattaAsXml().length());
        sendMessage(xmlMessage);

        xmlMessage = new XMLMessage(xml.getBoatsAsXml(), XMLMessageSubType.BOAT,
                xml.getBoatsAsXml().length());
        sendMessage(xmlMessage);

        xmlMessage = new XMLMessage(xml.getRaceAsXml(), XMLMessageSubType.RACE,
                xml.getRaceAsXml().length());
        sendMessage(xmlMessage);
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("IO error in server thread upon trying to close socket");
        }
    }

    private int readByte() throws Exception {
        int currentByte = -1;
        try {
            // @TODO @FIX ConnectionReset Exception when a client disconnects before it is garbage collected
            currentByte = is.read();
            crcBuffer.write(currentByte);
        } catch (IOException e) {
            e.printStackTrace();
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
            //serverLog("Player " + sourceId + " side socket disconnected", 1);
            return;
        } catch (IOException e) {
            logger.warn("Message send failed", 1);
        }
    }

    private int getSeqNo() {
        seqNo++;
        return seqNo;
    }


    public void sendBoatLocationPackets() {
        ArrayList<ServerYacht> yachts = new ArrayList<>(GameState.getYachts().values());
        for (ServerYacht yacht : yachts) {
            BoatLocationMessage boatLocationMessage =
                new BoatLocationMessage(
                    yacht.getSourceId(),
                    getSeqNo(),
                    yacht.getLocation().getLat(),
                    yacht.getLocation().getLng(),
                    yacht.getHeading(),
                    yacht.getCurrentVelocity().longValue());

            sendMessage(boatLocationMessage);
        }
    }

    public Thread getThread() {
        return thread;
    }

    public Socket getSocket() {
        return socket;
    }

    public ServerYacht getYacht() {
        return yacht;
    }

    public void sendCollisionMessage(Integer yachtId) {
        sendMessage(new YachtEventCodeMessage(yachtId));
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }
}
