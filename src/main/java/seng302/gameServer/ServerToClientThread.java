package seng302.gameServer;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import seng302.gameServer.server.messages.*;
import seng302.model.Player;
import seng302.model.Yacht;
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

    private static final Integer LOG_LEVEL = 1;
    private static final Integer MAX_ID_ATTEMPTS = 10;

    private Thread thread;

    private InputStream is;
    private OutputStream os;
    private Socket socket;

    private ByteArrayOutputStream crcBuffer;

    private Boolean userIdentified = false;
    private Boolean connected = true;
    private Boolean updateClient = true;
//    private Boolean initialisedRace = true;

    private Integer seqNo;
    private Integer sourceId;

    private ClientType clientType;
    private Boolean isRegistered = false;

    private XMLGenerator xml;

    private static final int PRESTART_TIME = 60 * -1000;
    private static final int WARNING_TIME = 30 * -1000;
    private static final int PREPATORY_TIME = 10 * -1000;


    public ServerToClientThread(Socket socket) {
        this.socket = socket;
        seqNo = 0;

        try{
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            return;
        }

        thread = new Thread(this);
        thread.start();
    }

    private void setUpYacht(){
        BufferedReader fn;
        String fName = "";
        BufferedReader ln;
        String lName = "";
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
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
        } catch (IOException e) {
            serverLog("IO error in server thread upon grabbing streams", 1);
        }

        Yacht yacht = new Yacht(
                "Yacht", sourceId, sourceId.toString(), fName, fName + " " + lName, "NZ"
        );

        GameState.addYacht(sourceId, yacht);
        GameState.addPlayer(new Player(socket, yacht));
    }

    static void serverLog(String message, int logLevel) {
        if (logLevel <= LOG_LEVEL) {
            System.out.println(
                "[SERVER " + LocalDateTime.now().toLocalTime().toString() + "] " + message);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        sendSetupMessages();
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
        setUpYacht();

        isRegistered = true;
        os.write(responseMessage.getBuffer());
        sendSetupMessages();

    }

    public void run() {
        int sync1;
        int sync2;
        // TODO: 14/07/17 wmu16 - Work out how to fix this while loop


        while (socket.isConnected()) {

            try {
                //Perform a write if it is time to as delegated by the MainServerThread
                if (updateClient) {
                    // TODO: 13/07/17 wmu16 - Write out game state - some function that would write all appropriate messages to this output stream
//                    ChatterMessage chatterMessage = new ChatterMessage(4, 14, "Hello, it's me");
//                    sendMessage(chatterMessage);
//                try {
//                    GameState.outputState(os);
//                } catch (IOException e) {
//                    System.out.println("IO error in server thread upon writing to output stream");
//                }
//                    sendBoatLocationPackets();
                    updateClient = false;
                }

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
                        //System.out.println("RECEIVED A PACKET");
                        switch (PacketType.assignPacketType(type, payload)) {
                            case BOAT_ACTION:
                                BoatActionType actionType = ServerPacketParser
                                        .extractBoatAction(
                                                new StreamPacket(type, payloadLength, timeStamp, payload));
                                GameState.updateBoat(sourceId, actionType);
                                break;

                            case RACE_REGISTRATION_REQUEST:
                                ClientType requestedType = ServerPacketParser.extractClientType(
                                        new StreamPacket(type, payloadLength, timeStamp, payload));

                                completeRegistration(requestedType);
                                break;
                        }
                    } else {
                        serverLog("Packet has been dropped", 1);
                    }
                }
            } catch (Exception e) {
                closeSocket();
                return;
            }
        }
    }

    private void sendSetupMessages() {
        xml = new XMLGenerator();
        Race race = new Race();

        for (Yacht yacht : GameState.getYachts().values()) {
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

    public void updateClient() {
        sendRaceStatusMessage();
        sendBoatLocationPackets();
        updateClient = true;
    }


    /**
     * Tries to confirm the connection just accepted.
     * Sends ID, expects that ID echoed for confirmation,
     * if so, sends a confirmation packet back to that connection
     * Creates a player instance with that ID and this thread and adds it to the GameState
     * If not, close the socket and end the threads execution
     *
     * @param id the id to try and assign to the connection
     * @return A boolean indicating if it was a successful handshake
     */
    private Boolean threeWayHandshake(Integer id) {

        return true;
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
            serverLog("Socket read failed", 1);
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
            serverLog("Message send failed", 1);
        }
    }

    private int getSeqNo() {
        seqNo++;
        return seqNo;
    }


    private void sendBoatLocationPackets() {
        ArrayList<Yacht> yachts = new ArrayList<>(GameState.getYachts().values());
        for (Yacht yacht : yachts) {
//            System.out.println("[SERVER] Lat: " + yacht.getLocation().getLat() + " Lon: " + yacht.getLocation().getLng());
            BoatLocationMessage boatLocationMessage =
                new BoatLocationMessage(
                    yacht.getSourceId(),
                    getSeqNo(),
                    yacht.getLocation().getLat(),
                    yacht.getLocation().getLng(),
                    yacht.getHeading(),
                    yacht.getVelocity().longValue());

            sendMessage(boatLocationMessage);
        }
    }

    public Thread getThread() {
        return thread;
    }

    public void sendRaceStatusMessage() {
        // variables taken from GameServerThread


        List<BoatSubMessage> boatSubMessages = new ArrayList<>();
        BoatStatus boatStatus;
        RaceStatus raceStatus;

        for (Player player : GameState.getPlayers()) {
            Yacht y = player.getYacht();

            if (GameState.getCurrentStage() == GameStages.PRE_RACE) {
                boatStatus = BoatStatus.PRESTART;
            } else if (GameState.getCurrentStage() == GameStages.RACING) {
                boatStatus = BoatStatus.RACING;
            } else {
                boatStatus = BoatStatus.UNDEFINED;
            }

            BoatSubMessage m = new BoatSubMessage(y.getSourceId(), boatStatus, 0, 0, 0, 1234l,
                1234l);
            boatSubMessages.add(m);
        }

        long timeTillStart = System.currentTimeMillis() - GameState.getStartTime();

        if (GameState.getCurrentStage() == GameStages.LOBBYING) {
            raceStatus = RaceStatus.PRESTART;
        } else if (GameState.getCurrentStage() == GameStages.PRE_RACE) {
            raceStatus = RaceStatus.PRESTART;

            if (timeTillStart > WARNING_TIME) {
                raceStatus = RaceStatus.WARNING;
            }

            if (timeTillStart > PREPATORY_TIME) {
                raceStatus = RaceStatus.PREPARATORY;
            }
        } else {
            raceStatus = RaceStatus.STARTED;
        }

        System.out.println("raceStatus.ger = " + raceStatus.getCode());

        sendMessage(new RaceStatusMessage(1, raceStatus, GameState.getStartTime(), GameState.getWindDirection(),
            GameState.getWindSpeedMMS().longValue(), GameState.getPlayers().size(),
            RaceType.MATCH_RACE, 1, boatSubMessages));

        if (GameState.getCurrentStage() == GameStages.PRE_RACE || GameState.getCurrentStage() == GameStages.LOBBYING){
            Long raceStartTime = GameState.getStartTime();

            sendMessage(new RaceStartStatusMessage(1, raceStartTime ,
                    1, RaceStartNotificationType.SET_RACE_START_TIME));
        }

    }

    public Socket getSocket() {
        return socket;
    }
}
