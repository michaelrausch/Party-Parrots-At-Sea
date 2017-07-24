package seng302.gameServer;


import com.sun.xml.internal.bind.v2.TODO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.apache.commons.io.IOUtils;
import seng302.models.Player;
import seng302.models.Yacht;
import seng302.models.stream.packets.PacketType;
import seng302.models.stream.packets.StreamPacket;
import seng302.models.xml.Race;
import seng302.models.xml.Regatta;
import seng302.models.xml.XMLGenerator;
import seng302.server.messages.BoatActionType;
import seng302.server.messages.BoatLocationMessage;
import seng302.server.messages.BoatStatus;
import seng302.server.messages.BoatSubMessage;
import seng302.server.messages.Message;

import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import seng302.server.messages.RaceStatus;
import seng302.server.messages.RaceStatusMessage;
import seng302.server.messages.RaceType;
import seng302.server.messages.WindDirection;
import seng302.server.messages.XMLMessage;
import seng302.server.messages.XMLMessageSubType;
import seng302.server.messages.XMLMessage;
import seng302.server.messages.XMLMessageSubType;
import seng302.utilities.GeoPoint;

/**
 * A class describing a single connection to a Client for the purposes of sending and receiving on its own thread.
 * All server threads created and owned by the server thread handler which can trigger client updates on its threads
 * Created by wmu16 on 13/07/17.
 */
public class ServerToClientThread implements Runnable, Observer {

    private static final Integer LOG_LEVEL = 1;
    private static final Integer MAX_ID_ATTEMPTS = 10;

    private Thread thread;

    private InputStream is;
    private OutputStream os;
    private Socket socket;

    private  ByteArrayOutputStream crcBuffer;

    private Boolean userIdentified = false;
    private Boolean connected = true;
    private Boolean updateClient = true;
    private Boolean initialisedRace = true;

    private Integer seqNo;
    private Integer sourceId;

    private XMLGenerator xml;

    public ServerToClientThread(Socket socket) {
        this.socket = socket;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("IO error in server thread upon grabbing streams");
        }
        //Attempt threeway handshake with connection
        sourceId = GameState.getUniquePlayerID();
        if (threeWayHandshake(sourceId)) {
            serverLog("Successful handshake. Client allocated id: " + sourceId, 1);
            Yacht yacht = new Yacht("Yacht", sourceId, sourceId.toString(), "Kap", "Kappa", "NZ");
//        Yacht yacht = new Yacht("Kappa", "Kap", new GeoPoint(57.6708220, 11.8321340), 90.0);
            GameState.addYacht(sourceId, yacht);
            GameState.addPlayer(new Player(socket, yacht));
        } else {
            serverLog("Unsuccessful handshake. Connection rejected", 1);
            closeSocket();
            return;
        }

        seqNo = 0;
        thread = new Thread(this);
        thread.start();
    }

    static void serverLog(String message, int logLevel){
        if(logLevel <= LOG_LEVEL){
            System.out.println("[SERVER] " + message);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        sendSetupMessages();
    }

    public void run() {
        int sync1;
        int sync2;
        // TODO: 14/07/17 wmu16 - Work out how to fix this while loop

        // used by ryan to simulate sending boats.xml
//        InputStream inputStream = getClass().getResourceAsStream("/server_config/boats1.xml");
//        StringWriter writer = new StringWriter();
//        try {
//            IOUtils.copy(inputStream, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String xml = writer.toString();
//        Message message = new XMLMessage(xml, XMLMessageSubType.BOAT, 0);
//        sendMessage(message);
//        System.out.println("[server] send message 1 " + message);
//
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        inputStream = getClass().getResourceAsStream("/server_config/boats.xml");
//        writer = new StringWriter();
//        try {
//            IOUtils.copy(inputStream, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        xml = writer.toString();
//        message = new XMLMessage(xml, XMLMessageSubType.BOAT, 0);
//        sendMessage(message);
//        System.out.println("[server] send message 2 " + message);
//
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        inputStream = getClass().getResourceAsStream("/server_config/boats2.xml");
//        writer = new StringWriter();
//        try {
//            IOUtils.copy(inputStream, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        xml = writer.toString();
//        message = new XMLMessage(xml, XMLMessageSubType.BOAT, 0);
//        sendMessage(message);
//        System.out.println("[server] send message 3 " + message);
//
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        inputStream = getClass().getResourceAsStream("/server_config/boats.xml");
//        writer = new StringWriter();
//        try {
//            IOUtils.copy(inputStream, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        xml = writer.toString();
//        message = new XMLMessage(xml, XMLMessageSubType.BOAT, 0);
//        sendMessage(message);
//        System.out.println("[server] send message 4 " + message);
//        sendMessage(getRaceStatusMessage());
//        System.out.println("sent race status");
        //-------

        while(true) {

            try {
                if (initialisedRace) {
                    sendSetupMessages();
                    initialisedRace = false;
                }

                //Perform a write if it is time to as delegated by the MainServerThread
                if (updateClient) {
                    // TODO: 13/07/17 wmu16 - Write out game state - some function that would write all appropriate messages to this output stream
                    sendBoatLocationPackets();
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
                        //System.out.println("RECEIVED A PACKET");
                        switch (PacketType.assignPacketType(type)) {
                            case BOAT_ACTION:
                                BoatActionType actionType = ServerPacketParser
                                    .extractBoatAction(
                                        new StreamPacket(type, payloadLength, timeStamp, payload));
                                GameState.updateBoat(sourceId, actionType);
                                break;
                        }
                    } else {
                        serverLog("Packet has been dropped", 1);
                    }
                }
            } catch (Exception e) {
                // TODO: 24/07/17 zyt10 - fix a logic here when a client disconnected 
                serverLog("ERROR OCCURRED, CLOSING SERVER CONNECTION: " + socket.getRemoteSocketAddress().toString(), 1);
                closeSocket();
                return;
            }
        }

    }

    private void sendSetupMessages() {
        xml = new XMLGenerator();
        Race race = new Race();

        for (Player player : GameState.getPlayers()){
            race.addBoat(player.getYacht());
        }

        //@TODO calculate lat/lng values
        xml.setRegatta(new Regatta("RaceVision Test Game", 57.6679590, 11.8503233));
        xml.setRace(race);

        XMLMessage xmlMessage = new XMLMessage(xml.getRegattaAsXml(), XMLMessageSubType.REGATTA, xml.getRegattaAsXml().length());
        sendMessage(xmlMessage);

        xmlMessage = new XMLMessage(xml.getBoatsAsXml(), XMLMessageSubType.BOAT, xml.getBoatsAsXml().length());
        sendMessage(xmlMessage);

        xmlMessage = new XMLMessage(xml.getRaceAsXml(), XMLMessageSubType.RACE, xml.getRaceAsXml().length());
        sendMessage(xmlMessage);
        System.out.println("Sent xml messages for " + thread.getName());

    }

    public void updateClient() {
        sendBoatLocationPackets();
        updateClient = true;
    }


    /**
     * Tries to confirm the connection just accepted.
     * Sends ID, expects that ID echoed for confirmation,
     * if so, sends a confirmation packet back to that connection
     * Creates a player instance with that ID and this thread and adds it to the GameState
     * If not, close the socket and end the threads execution
     * @param id the id to try and assign to the connection
     * @return A boolean indicating if it was a successful handshake
     */
    private Boolean threeWayHandshake(Integer id) {
        Integer confirmationID = null;
        Integer identificationAttempt = 0;
        while (!userIdentified) {
            try {
                os.write(id);                                         //Send out new ID looking for echo
                confirmationID = is.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (id.equals(confirmationID)) {                          //ID is echoed back. Connection is a client
                return true;
            } else if (identificationAttempt > MAX_ID_ATTEMPTS) {     //No response. not a client. tidy up and go home.
                return false;
            }
        identificationAttempt++;
        }

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

    public void sendMessage(Message message){
        try {
            os.write(message.getBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getSeqNo(){
        seqNo++;
        return seqNo;
    }


    private void sendBoatLocationPackets(){
        ArrayList<Yacht> yachts = new ArrayList<>(GameState.getYachts().values());
        for (Yacht yacht: yachts){
//            System.out.println("[SERVER] Lat: " + yacht.getLocation().getLat() + " Lon: " + yacht.getLocation().getLng());
            BoatLocationMessage boatLocationMessage =
                    new BoatLocationMessage(
                            sourceId,
                            getSeqNo(),
                            yacht.getLocation().getLat(),
                            yacht.getLocation().getLng(),
                            yacht.getHeading(),
                            (long) yacht.getVelocity());

            sendMessage(boatLocationMessage);
        }
    }

    public Thread getThread() {
        return thread;
    }

    public void sendRaceStatusMessage(){
        // variables taken from GameServerThread
        int TIME_TILL_RACE_START = 20*1000;
        long startTime = System.currentTimeMillis() + TIME_TILL_RACE_START;

        List<BoatSubMessage> boatSubMessages = new ArrayList<>();
        BoatStatus boatStatus;
        RaceStatus raceStatus;

        for (Player player : GameState.getPlayers()){
            Yacht y = player.getYacht();

            if (GameState.getCurrentStage() == GameStages.PRE_RACE){
                boatStatus = BoatStatus.PRESTART;
            }
            else if(GameState.getCurrentStage() == GameStages.RACING){
                boatStatus = BoatStatus.RACING;
            } else {
                boatStatus = BoatStatus.UNDEFINED;
            }

            BoatSubMessage m = new BoatSubMessage(y.getSourceId(), boatStatus, 0, 0, 0, 1234l, 1234l);
            boatSubMessages.add(m);
        }

        if (GameState.getCurrentStage() == GameStages.RACING){
            raceStatus = RaceStatus.STARTED;
        } else {
            raceStatus = RaceStatus.WARNING;
        }

        sendMessage(new RaceStatusMessage(1, raceStatus, startTime, WindDirection.SOUTH,
            100, GameState.getPlayers().size(), RaceType.MATCH_RACE, 1, boatSubMessages));
    }
}
