package seng302.gameServer;


import java.util.ArrayList;
import java.util.Random;
import seng302.client.ClientPacketParser;
import seng302.models.Player;
import seng302.models.Yacht;
import seng302.models.stream.packets.PacketType;
import seng302.models.stream.packets.StreamPacket;
import seng302.server.messages.ChatterMessage;
import seng302.server.messages.Heartbeat;
import seng302.server.messages.BoatActionType;
import seng302.server.messages.Message;
import seng302.models.xml.Race;
import seng302.models.xml.Regatta;
import seng302.models.xml.XMLGenerator;
import seng302.server.messages.*;
import seng302.models.xml.XMLGenerator;
import seng302.server.messages.*;

import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import seng302.utilities.GeoPoint;

/**
 * A class describing a single connection to a Client for the purposes of sending and receiving on its own thread.
 * All server threads created and owned by the server thread handler which can trigger client updates on its threads
 * Created by wmu16 on 13/07/17.
 */
public class ServerToClientThread extends Thread {
    private static final Integer MAX_ID_ATTEMPTS = 10;

    private InputStream is;
    private OutputStream os;
    private Socket socket;

    private  ByteArrayOutputStream crcBuffer;

    private Boolean userIdentified = false;
    private Boolean connected = true;
    private Boolean updateClient = true;
    private Boolean intiialisedRace = false;

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
//                threeWayHandshake();
        GameState.addPlayer(new Player(socket));
        Random rand = new Random();
        sourceId = rand.nextInt(100000);
        GameState.addYacht(sourceId, new Yacht("Kappa", "Kap", new GeoPoint(0.0, 0.0), 0.0));
        seqNo = 0;
    }

    public void run() {
        int sync1;
        int sync2;
        // TODO: 14/07/17 wmu16 - Work out how to fix this while loop
        while(true) {
            //System.out.print(".");

            try {
                if (intiialisedRace) {
                    sendSetupMessages();
                }
                
                //Perform a write if it is time to as delegated by the MainServerThread
                if (updateClient) {
                    // TODO: 13/07/17 wmu16 - Write out game state - some function that would write all appropriate messages to this output stream
                    ChatterMessage chatterMessage = new ChatterMessage(4, 14, "Hello, it's me");
                    sendMessage(chatterMessage);
                    sendBoatLocationPackets();
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
                        System.err.println("Packet has been dropped");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        xml.setRegatta(new Regatta("RaceVision Test Game", 0d, 0d));
        xml.setRace(race);

        XMLMessage xmlMessage = new XMLMessage(xml.getRegattaAsXml(), XMLMessageSubType.REGATTA, xml.getRegattaAsXml().length());
        sendMessage(xmlMessage);

        xmlMessage = new XMLMessage(xml.getBoatsAsXml(), XMLMessageSubType.BOAT, xml.getBoatsAsXml().length());
        sendMessage(xmlMessage);

        xmlMessage = new XMLMessage(xml.getRaceAsXml(), XMLMessageSubType.RACE, xml.getRaceAsXml().length());
        sendMessage(xmlMessage);
    }

    public void updateClient() {
        updateClient = true;
    }


    /**
     * Tries to confirm the connection just accepted.
     * Sends ID, expects that ID echoed for confirmation,
     * if so, sends a confirmation packet back to that connection
     * Creates a player instance with that ID and this thread and adds it to the GameState
     * If not, close the socket and end the threads execution
     */
    private void threeWayHandshake() {
//        // TODO: 13/07/17 Finish using AC35
//        Integer playerID = GameState.getUniquePlayerID();
//        Integer confirmationID = null;
//        Integer identificationAttempt = 0
//        while (!userIdentified) {
//            os.write(playerID);                                       //Send out new ID looking for echo
//            confirmationID = is.read();
//            if (playerID == idConfirmation) {                         //ID is echoed back. Connection is a client
//                os.write(  some determined confirmation message  );   //Confirm to client
//                GameState.addPlayer(new Player(playerID, this));      //Create a player in game state for client
//                userIdentified = true;
//            } else if (identificationAttempt > MAX_ID_ATTEMPTS) {     //No response. not a client. tidy up and go home.
//                closeSocket();
//                return;
//            }
//        identificationAttempt++;
//        }
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("IO error in server thread upon trying to close socket");
        }
    }


    public void initialiseRace(){
        intiialisedRace = true;
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
            BoatLocationMessage boatLocationMessage = new BoatLocationMessage(sourceId,getSeqNo(), yacht.getLocation().getLat(), yacht.getLocation().getLng(), yacht.getHeading(), (long) yacht.getVelocity());
            sendMessage(boatLocationMessage);
        }
    }
}
