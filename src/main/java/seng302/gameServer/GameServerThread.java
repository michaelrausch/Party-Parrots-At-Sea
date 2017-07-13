package seng302.gameServer;

import seng302.models.Player;
import seng302.models.Yacht;
import seng302.server.messages.*;
import seng302.server.simulator.Boat;
import seng302.server.simulator.Simulator;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class GameServerThread implements Runnable, Observer, ClientConnectionDelegate{
    
    private static final Integer MAX_NUM_PLAYERS = 10;
    public static final int PORT_NUMBER = 4950;

    private Boolean hosting = true;
    
    private ServerSocketChannel server;
    private long startTime;
    private short seqNum;
    
    private final int HEARTBEAT_PERIOD = 5000;
    private final int RACE_STATUS_PERIOD = 1000/2;
    private final int RACE_START_STATUS_PERIOD = 1000;
    private final int BOAT_LOCATION_PERIOD = 1000/5;
    private final int TIME_TILL_RACE_START = 20*1000;
    private static final int LOG_LEVEL = 1;

    public GameServerThread(String threadName){
        Thread runner = new Thread(this, threadName);
        runner.setDaemon(true);
        seqNum = 0;

        runner.start();
    }

     static void serverLog(String message, int logLevel){
        if(logLevel <= LOG_LEVEL){
            System.out.println("[SERVER] " + message);
        }
    }

    /**
     * Creates and returns an XML Message from the file specified
     * @param fileName The source XML file
     * @param type The XML Message type
     * @return The XML Message
     */
    private Message getXmlMessage(String fileName, XMLMessageSubType type){
        String fileContents = null;

        try {
            InputStream thisStream = this.getClass().getResourceAsStream(fileName);
            fileContents = new String(org.apache.commons.io.IOUtils.toByteArray(thisStream));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            return null;
        }

        if (fileContents != null){
            return new XMLMessage(fileContents, type, seqNum);
        }

        return null;
    }

    /**
     * @return Get a race status message for the current race
     */
    private Message getRaceStatusMessage(){

        List<BoatSubMessage> boatSubMessages = new ArrayList<>();
        BoatStatus boatStatus;
        RaceStatus raceStatus;
        boolean thereAreBoatsNotFinished = false;

        for (Player player : GameState.getPlayers()){
            Yacht y = player.getYacht();

            if (GameState.getCurrentStage() == GameStages.PRE_RACE){
                boatStatus = BoatStatus.PRESTART;
                thereAreBoatsNotFinished = true;
            }
            else if(false){ //@TODO if boat has finished
                 boatStatus = BoatStatus.FINISHED;
            }
            else{
                boatStatus = BoatStatus.PRESTART;
                thereAreBoatsNotFinished = true;
            }

            BoatSubMessage m = new BoatSubMessage(y.getSourceID(), boatStatus, y.getLastMarkRounded().getId(), 0, 0, 1234l, 1234l);
            boatSubMessages.add(m);
        }

        if (thereAreBoatsNotFinished){
            if (GameState.getCurrentStage() == GameStages.RACING){
                raceStatus = RaceStatus.STARTED;
            }
            else{
                long currentTime = System.currentTimeMillis();
                long timeDifference = startTime - currentTime;

                if (timeDifference > 60*3){
                    raceStatus = RaceStatus.PRESTART;
                }
                else if (timeDifference > 60){
                    raceStatus = RaceStatus.WARNING;
                }
                else{
                    raceStatus = RaceStatus.PREPARATORY;
                }
            }
        }
        else{
            raceStatus = RaceStatus.TERMINATED;
        }

        return new RaceStatusMessage(1, raceStatus, startTime, WindDirection.SOUTH,
                100, GameState.getPlayers().size(), RaceType.MATCH_RACE, 1, boatSubMessages);
    }


    /**
     * Starts sending heartbeat messages to the client
     */
    private void startSendingHeartbeats() {
        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Message heartbeat = new Heartbeat(seqNum);

                try {
                    System.out.println("Sending heartbeat");
                    broadcast(heartbeat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, HEARTBEAT_PERIOD);
    }

    /**
     * Start sending race start status messages until race starts
     */
    private void startSendingRaceStartStatusMessages(){
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Message raceStartStatusMessage = new RaceStartStatusMessage(seqNum, startTime , 1,
                        RaceStartNotificationType.SET_RACE_START_TIME);
                try {
                    if (startTime < System.currentTimeMillis() && GameState.getCurrentStage() != GameStages.RACING){
                    }
                    else{
                        System.out.println("Sending race start status");
                        broadcast(raceStartStatusMessage);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, RACE_START_STATUS_PERIOD);
    }

    /**
     * Start sending race start status messages until race starts
     */
    private void startSendingRaceStatusMessages(){

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Message raceStatusMessage = getRaceStatusMessage();
                try {
                    broadcast(raceStatusMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, RACE_STATUS_PERIOD);
    }

    /**
     * Sends the race, boat, and regatta XML files to the client
     */
    private void sendXml(){
        try{
            Message raceData = getXmlMessage("/server_config/race.xml", XMLMessageSubType.RACE);
            Message boatData = getXmlMessage("/server_config/boats.xml", XMLMessageSubType.BOAT);
            Message regatta = getXmlMessage("/server_config/regatta.xml", XMLMessageSubType.REGATTA);

            if (raceData != null){
                System.out.println("Sending RaceXML");
                broadcast(raceData);
            }
            if (boatData != null){
                System.out.println("Sending boatsXML");
                broadcast(boatData);
            }
             if (regatta != null){
                 System.out.println("Sending regattaXML");
                 broadcast(regatta);
            }
        } catch (IOException e) {
            serverLog("Couldn't send an XML Message: " + e.getMessage(), 0);
        }
    }

    /**
     * Send the post-start race course information
     */
    private void sendPostStartCourseXml(){
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Message raceData = getXmlMessage("/server_config/courseLimits.xml", XMLMessageSubType.RACE);
                    if (raceData != null) {
                        System.out.println("Sending courseLimitsXML");
                        broadcast(raceData);
                    }
                }catch (IOException e) {
                    serverLog("Couldn't send an XML Message: " + e.getMessage(), 0);
                }
            }
        },1000);
        //Delays the new course xml data for 25 seconds so the boats are able to pass the starting line
    }

    public void run() {
        ServerListenThread serverListenThread;
        Boolean serverIsSendingMessages = false;

        try{
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress("localhost", PORT_NUMBER));
            serverListenThread = new ServerListenThread(server, this);
            serverListenThread.start();
        }
        catch (IOException e){
            serverLog("Failed to bind socket: " + e.getMessage(), 0);
        }

       while (hosting) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (GameState.getCurrentStage() == GameStages.RACING && !serverIsSendingMessages) {
                serverLog("Race Started", 0);

                startSendingHeartbeats();
                sendXml();
                startSendingRaceStartStatusMessages();
                //startSendingRaceStatusMessages();
                sendPostStartCourseXml();
                serverIsSendingMessages = true;
            }

            else if (GameState.getCurrentStage() == GameStages.FINISHED) {
                serverLog("Race Finished", 0);
            }

            startTime = System.currentTimeMillis() + TIME_TILL_RACE_START;

            }
    }

//    /**
//     * Start sending static boat position updates when race has finished
//     */
//    private void startSendingRaceFinishedBoatPositions(){
//        Timer t = new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    for (Boat b : raceSimulator.getBoats()){
//                        Message m = new BoatLocationMessage(b.getSourceID(), seqNum, b.getLat(),
//                                b.getLng(), b.getLastPassedCorner().getBearingToNextCorner(),
//                                ((long) 0));
//
//                        server.send(m);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, 0, BOAT_LOCATION_PERIOD);
//    }

    /**
     * A client has tried to connect to the server
     * @param player The player that connected
     */
    @Override
    public void clientConnected(Player player) {
        if (GameState.getPlayers().size() < MAX_NUM_PLAYERS && GameState.getCurrentStage() == GameStages.LOBBYING) {
            System.out.println("");
            serverLog("Player Connected", 0);
            GameState.addPlayer(player);
        }
        sendXml();
    }

    /**
     * Listens for a connection and upon finding one, creates a Player object and adds it to the universal GameState
     */
    private void acceptConnection() {
        try {
            SocketChannel thisClient = server.accept();
            if (thisClient.socket() != null){
                Player thisPlayer = new Player(thisClient);
                GameState.addPlayer(thisPlayer);
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }


    void unicast(Message message, SocketChannel client) throws IOException {
        message.send(client);       // TODO: 11/07/17 Do we incement seqNum for individual messages? 
    }


    void broadcast(Message message) throws IOException{
        for(Player player : GameState.getPlayers()) {
            System.out.println("Sending message seqNo[" + seqNum + "] to Player: " + player.toString());
            message.send(player.getSocketChannel());
        }
        seqNum++;       // TODO: 11/07/17 Do we increment seqNum for every message or for the one message to everyone 
    }


    
    /**
     * Send a boat location message when they are updated by the simulator
     * @param o .
     * @param arg .
     */
    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {
        /* Only send if server started
        // TODO: I don't understand why i need to check server is null or not ... confused - haoming 2/5/17
        if(server == null || !server.isStarted()){
            return;
        }

        int numOfBoatsFinished = 0;
        for (Boat boat : (List<Boat>) arg){
            try {
                if (boat.isFinished()) {
                    numOfBoatsFinished ++;
                    if (!boatsFinished.get(boat.getSourceID())) {
                        boatsFinished.put(boat.getSourceID(), true);
                    }
                }
                Message m = new BoatLocationMessage(boat.getSourceID(), 1, boat.getLat(),
                        boat.getLng(), boat.getLastPassedCorner().getBearingToNextCorner(),
                        ((long) boat.getSpeed()));
                broadcast(m);
            } catch (IOException e) {
                serverLog("Couldn't send a boat status message", 3);
                return;
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }*/
        }



//        if (numOfBoatsFinished == ((List<Boat>) arg).size()) {
//            startSendingRaceFinishedBoatPositions();
//        }

    //}

    public void terminateGame() {
        try {
            //TODO: for now, I just close the socket, but i think we should terminate the whole thread instead. -hyi25 13 July
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
