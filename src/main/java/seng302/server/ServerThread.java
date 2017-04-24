package seng302.server;

import seng302.server.messages.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerThread implements Runnable{
    private Thread runner;
    private StreamingServerSocket server;
    private final int HEARTBEAT_PERIOD = 5000;
    private final int RACE_STATUS_PERIOD = 1000;
    private final int PORT_NUMBER = 8085;

    public ServerThread(String threadName){
        runner = new Thread(this, threadName);
        System.out.println("Spawning Server Thread");
        runner.start();
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

        if (fileContents != null){
            return new XMLMessage(fileContents, type, server.getSequenceNumber());
        }

        return null;
    }

    /**
     * @return A sample race status message
     */
    public Message getTestRaceStatusMessage(){
        BoatSubMessage boat1 = new BoatSubMessage(1, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        BoatSubMessage boat2 = new BoatSubMessage(2, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);

        BoatSubMessage boat3 = new BoatSubMessage(3, BoatStatus.PRESTART, 0, 0, 0,
                10000, 10000);


        List<BoatSubMessage> boats = new ArrayList<BoatSubMessage>();
        boats.add(boat1);
        boats.add(boat2);
        boats.add(boat3);

        return new RaceStatusMessage(1, RaceStatus.PRESTART, 1000, WindDirection.EAST,
                100, 3, RaceType.MATCH_RACE, 1, boats);
    }

    public void run() {
        try{
            server = new StreamingServerSocket(PORT_NUMBER);
        }
        catch (IOException e){
            System.err.println("Failed to bind socket: " + e.getMessage());
        }

        server.start();

        try {
            // Load and send race XML data
            Message raceData = getXmlMessage("/server_config/race.xml", XMLMessageSubType.RACE);
            Message boatData = getXmlMessage("/server_config/boats.xml", XMLMessageSubType.BOAT);
            Message regatta = getXmlMessage("/server_config/regatta.xml", XMLMessageSubType.REGATTA);

            if (raceData != null){
                server.send(raceData);
            }

            if (boatData != null){
                server.send(boatData);
            }

            if (regatta != null){
                server.send(regatta);
            }

            // Timer to send the heartbeat
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message hb = new Heartbeat(server.getSequenceNumber());
                    try {
                        server.send(hb);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, HEARTBEAT_PERIOD);

            // Timer to send the race status messages
            Timer t1 = new Timer();
            t1.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message statusMessage = getTestRaceStatusMessage();

                    try {
                        server.send(statusMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 100, RACE_STATUS_PERIOD);

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
