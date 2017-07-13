package seng302.server;

import java.io.IOException;
import seng302.server.messages.BoatActionMessage;

/**
 * Created by kre39 on 13/07/17.
 */
public class ClientTransmitterThread implements Runnable {
    private StreamingServerSocket server;
    private final int PORT_NUMBER = 4951;
    private static final int LOG_LEVEL = 1;

    public ClientTransmitterThread(String threadName){
        Thread runner = new Thread(this, threadName);
        runner.setDaemon(true);
        runner.start();

    }

    static void serverLog(String message, int logLevel){
        if(logLevel <= LOG_LEVEL){
            System.out.println("[SERVER] " + message);
        }
    }

    public void run() {
        try{
            // Needs to connect to the server: Currently no server is being connect so the boat action keys are not being sent
            server = new StreamingServerSocket(PORT_NUMBER);
        }
        catch (IOException e){
            serverLog("Failed to bind socket: " + e.getMessage(), 0);
        }

        // Wait for client to connect
        server.start();

    }

    /**
     * Send the post-start race course information
     */
    public void sendBoatActionMessage(BoatActionMessage boatActionMessage) {
        try {
            server.send(boatActionMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 }
