package seng302.server;

import seng302.server.messages.*;
import seng302.server.simulator.Boat;
import seng302.server.simulator.Simulator;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ServerThread implements Runnable, Observer {
    private StreamingServerSocket server;
    private long startTime;
    private boolean raceStarted =  false;
    private Map<Integer,Boolean> boatsFinished = new HashMap<>();
    private List<Boat> boats;
    private Simulator raceSimulator;
    private boolean sendingRaceFinishedLocationMessages = true;

    private final int HEARTBEAT_PERIOD = 5000;
    private final int RACE_STATUS_PERIOD = 1000/2;
    private final int RACE_START_STATUS_PERIOD = 1000;
    private final int BOAT_LOCATION_PERIOD = 1000/5;
    private final int PORT_NUMBER = 4949;
    private final int TIME_TILL_RACE_START = 20*1000;
    private static final int LOG_LEVEL = 1;

    public ServerThread(String threadName){
        Thread runner = new Thread(this, threadName);
        runner.setDaemon(true);

        serverLog("Spawning Server", 0);

        raceSimulator = new Simulator(BOAT_LOCATION_PERIOD);
        raceSimulator.addObserver(this);
        // run race simulator, so it can send boats' static location.
        Thread raceSimulatorThread = new Thread(raceSimulator, "Race Simulator");

        boats = raceSimulator.getBoats();

        for (Boat b : boats){
            boatsFinished.put(b.getSourceID(), false);
        }

        runner.start();
        raceSimulatorThread.start();
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
            return new XMLMessage(fileContents, type, server.getSequenceNumber());
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

            BoatSubMessage m = new BoatSubMessage(b.getSourceID(), boatStatus, b.getLastPassedCorner().getSeqID(), 0, 0, b.getEstimatedTimeTillFinish(), b.getEstimatedTimeTillFinish());
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

        return new RaceStatusMessage(1, raceStatus, startTime, WindDirection.SOUTH,
                100, boats.size(), RaceType.MATCH_RACE, 1, boatSubMessages);
    }

    /**
     * Starts an instance of the race simulator
     */
    private void startRaceSim(){
        serverLog("Starting Running Race Simulator", 0);
        // set race started to true, so the simulator will start moving boats
        raceSimulator.setRaceStarted(true);
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
        serverLog("Sending race status messages", 0);
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
            Message raceData = getXmlMessage("/server_config/race.xml", XMLMessageSubType.RACE);
            Message boatData = getXmlMessage("/server_config/boats.xml", XMLMessageSubType.BOAT);
            Message regatta = getXmlMessage("/server_config/regatta.xml", XMLMessageSubType.REGATTA);

            if (raceData != null){
                server.send(raceData);
                serverLog("Sending race data", 0);
            }

            if (boatData != null){
                server.send(boatData);
                serverLog("Sending boat data", 0);
            }

             if (regatta != null){
                 server.send(regatta);
                 serverLog("Sending regatta data", 0);
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
                        server.send(raceData);
                        serverLog("Sending race data", 0);
                    }
                }catch (IOException e) {
                    serverLog("Couldn't send an XML Message: " + e.getMessage(), 0);
                }
            }
        },25000);
        //Delays the new course xml data for 25 seconds so the boats are able to pass the starting line
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
        sendPostStartCourseXml();
    }

    /**
     * Start sending static boat position updates when race has finished
     */
    private void startSendingRaceFinishedBoatPositions(){
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (Boat b : raceSimulator.getBoats()){
                        Message m = new BoatLocationMessage(b.getSourceID(), server.getSequenceNumber(), b.getLat(),
                                b.getLng(), b.getLastPassedCorner().getBearingToNextCorner(),
                                ((long) 0));

                        server.send(m);
                    }

                } catch (IOException e) {
                    System.out.print("");
                }
            }
        }, 0, BOAT_LOCATION_PERIOD);
    }

    /**
     * Send a boat location message when they are updated by the simulator
     * @param o .
     * @param arg .
     */
    @Override
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {
        // Only send if server started
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
                        serverLog("Boat " + boat.getSourceID() + " finished the race", 1);
                    }
                }
                Message m = new BoatLocationMessage(boat.getSourceID(), 1, boat.getLat(),
                        boat.getLng(), boat.getLastPassedCorner().getBearingToNextCorner(),
                        ((long) boat.getSpeed()));
                server.send(m);
            } catch (IOException e) {
                serverLog("Couldn't send a boat status message", 3);
                return;
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        if (numOfBoatsFinished == ((List<Boat>) arg).size()) {
            startSendingRaceFinishedBoatPositions();
        }

    }
}
