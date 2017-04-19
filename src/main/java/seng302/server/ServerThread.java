package seng302.server;

import seng302.server.messages.Heartbeat;
import seng302.server.messages.Message;
import seng302.server.messages.XMLMessage;
import seng302.server.messages.XMLMessageSubType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

public class ServerThread implements Runnable{
    private Thread runner;
    private StreamingServerSocket server;
    private final int HEARTBEAT_PERIOD = 5000;
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

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
