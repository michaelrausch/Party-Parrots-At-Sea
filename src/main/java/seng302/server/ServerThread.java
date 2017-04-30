package seng302.server;

import seng302.server.messages.*;
import seng302.server.simulator.Boat;
import seng302.server.simulator.Simulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ServerThread implements Runnable, Observer {
    private Thread runner;
    private StreamingServerSocket server;
    private long startTime;
    boolean raceStarted =  false;
    boolean raceFinished = false;
    Map<Integer,Boolean> boatsFinished = new HashMap<>();
    private List<Boat> boats;
    private Simulator raceSimulator;

    private final int HEARTBEAT_PERIOD = 5000;
    private final int RACE_STATUS_PERIOD = 1000/2;
    private final int RACE_START_STATUS_PERIOD = 1000/2;
    private final int BOAT_LOCATION_PERIOD = 1000/5;
    private final int PORT_NUMBER = 8085;
    private final int TIME_TILL_RACE_START = 20*1000;
    private static final int LOG_LEVEL = 1;

    public ServerThread(String threadName){
        runner = new Thread(this, threadName);
        serverLog("Spawning Server", 0);
        raceSimulator = new Simulator(BOAT_LOCATION_PERIOD);
        boats = raceSimulator.getBoats();

        for (Boat b : boats){
            boatsFinished.put(b.getSourceID(), false);
        }

        runner.start();
    }

    public static void serverLog(String message, int logLevel){
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
    public Message getXmlMessage(String fileName, XMLMessageSubType type){
        String fileContents = null;

        try {
            fileContents = new String(Files.readAllBytes(Paths.get(this.getClass().getResource(fileName).getPath().substring(1))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e){
            return null;
        }

        if (fileContents != null){
            return new XMLMessage(fileContents, type, server.getSequenceNumber());
        }

        return null;
    }

    /**
     * @return Get a race status message for the current race
     */
    public Message getRaceStatusMessage(){
        List<BoatSubMessage> boatSubMessages = new ArrayList<BoatSubMessage>();
        BoatStatus boatStatus;
        RaceStatus raceStatus;
        boolean thereAreBoatsNotFinished = false;

        for (Boat b : boats){
            if (!raceStarted){
                boatStatus = BoatStatus.PRESTART;
                thereAreBoatsNotFinished = true;
            }
            else if(boatsFinished.get(b.getSourceID())){
                boatStatus = BoatStatus.FINISHED;
            }
            else{
                boatStatus = BoatStatus.PRESTART;
                thereAreBoatsNotFinished = true;
            }

            BoatSubMessage m = new BoatSubMessage(b.getSourceID(), boatStatus, b.getLastPassedCorner().getSeqID(), 0, 0, 0, 0);
            boatSubMessages.add(m);
        }

        if (thereAreBoatsNotFinished){
            if (raceStarted){
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

        return new RaceStatusMessage(1, raceStatus, startTime, WindDirection.EAST,
                100, boats.size(), RaceType.MATCH_RACE, 1, boatSubMessages);
    }

    /**
     * Starts an instance of the race simulator
     */
    private void startRaceSim(){
        serverLog("Starting Race Simulator", 0);
        raceSimulator.addObserver(this);
        new Thread(raceSimulator).start();
    }

    /**
     * Starts sending heartbeat messages to the client
     */
    private void startSendingHeartbeats(){
        serverLog("Sending Heartbeats", 0);
        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                Message heartbeat = new Heartbeat(server.getSequenceNumber());

                try {
                    server.send(heartbeat);
                } catch (IOException e) {
                    System.out.print("");
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
                Message raceStartStatusMessage = new RaceStartStatusMessage(server.getSequenceNumber(), startTime , 1,
                        RaceStartNotificationType.SET_RACE_START_TIME);
                try {
                    if (startTime < System.currentTimeMillis() && !raceStarted){
                        startRaceSim();
                        raceStarted = true;
                        serverLog("Race Started", 0);
                    }
                    else{
                        server.send(raceStartStatusMessage);
                    }

                } catch (IOException e) {
                    System.out.print("");
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
                    server.send(raceStatusMessage);

                } catch (IOException e) {
                    System.out.print("");
                }
            }
        }, 0, RACE_STATUS_PERIOD);
    }

    /**
     * Sends the race, boat, and regatta XML files to the client
     */
    private void sendXml(){
        try{
            Message raceData = getXmlMessage("server_config/race.xml", XMLMessageSubType.RACE);
            Message boatData = getXmlMessage("server_config/boats.xml", XMLMessageSubType.BOAT);
            Message regatta = getXmlMessage("server_config/regatta.xml", XMLMessageSubType.REGATTA);

            if (raceData != null){
                server.send(raceData);
            }

            if (boatData != null){
                server.send(boatData);
            }

             if (regatta != null){
                 server.send(regatta);
            }
        } catch (IOException e) {
            serverLog("Couldn't send an XML Message: " + e.getMessage(), 0);
        }
    }

    public void run() {
        try{
            server = new StreamingServerSocket(PORT_NUMBER);
        }
        catch (IOException e){
            serverLog("Failed to bind socket: " + e.getMessage(), 0);
        }

        // Wait for client to connect
        server.start();

        startTime = System.currentTimeMillis() + TIME_TILL_RACE_START;

        startSendingHeartbeats();
        sendXml();
        startSendingRaceStartStatusMessages();
        startSendingRaceStatusMessages();

        //serverLog("Sending Race Status Messages", 0);

    }

    /**
     * Send a boat location message when they are updated by the simulator
     * @param o .
     * @param arg .
     */
    @Override
    public void update(Observable o, Object arg) {
        // Only send if server started
        if(!server.isStarted()){
            return;
        }

        for (Boat b : ((Simulator) o).getBoats()){
            try {

                Message m = new BoatLocationMessage(b.getSourceID(), 1, b.getLat(),
                        b.getLng(), b.getHeadingCorner().getBearingToNextCorner(),
                        ((long) b.getSpeed()));
                server.send(m);
            } catch (IOException e) {
                serverLog("Couldn't send a boat status message", 1);
            }
            catch (NullPointerException e){
                //raceFinished = true;
                serverLog("Boat " + b.getSourceID() + " finished the race", 1);
                boatsFinished.put(b.getSourceID(), true);
            }
        }
    }
}
