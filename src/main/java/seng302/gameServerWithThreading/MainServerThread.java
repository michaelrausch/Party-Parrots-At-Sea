package seng302.gameServerWithThreading;

import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.models.stream.PacketBufferDelegate;
import seng302.models.stream.StreamParser;
import seng302.models.stream.packets.StreamPacket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A class describing the overall server, which creates and collects server threads for each client
 * Created by wmu16 on 13/07/17.
 */
public class MainServerThread extends Thread implements PacketBufferDelegate{

    private static final int PORT = 4950;
    private static final Integer MAX_NUM_PLAYERS = 1;

    private ServerSocket serverSocket = null;
    private Socket socket;
    private ArrayList<ServerToClientThread> serverToClientThreads = new ArrayList<>();

    private PriorityBlockingQueue<StreamPacket> packetBuffer;


    public MainServerThread() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("IO error in server thread handler upon trying to make new server socket");
        }

        packetBuffer = new PriorityBlockingQueue<>();
    }


    public void run() {
        //You should handle interrupts in some way, so that the thread won't keep on forever if you exit the app.
        while (!isInterrupted()) {
            try {
                Thread.sleep(1000 / 60);    //60 times per second we should calculate the game state
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //LOBBYING
            if (GameState.getCurrentStage() == GameStages.LOBBYING && GameState.getPlayers().size() < MAX_NUM_PLAYERS) {
                try {
                    // TODO: 14/07/17 wmu16 - Get out of blocking call somehow after a time
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    System.out.println("IO error in server thread handler upon trying to accept connection");
                }
                ServerToClientThread thread = new ServerToClientThread(socket, this);
                serverToClientThreads.add(thread);
                thread.start();
            }

            //RACING
            else if (GameState.getCurrentStage() == GameStages.RACING) {

            }


            //FINISHED
            else if (GameState.getCurrentStage() == GameStages.FINISHED) {

            }

//            updateClients();

            while (!packetBuffer.isEmpty()){
                System.out.println("WHATUPPP");
                try {
                    StreamPacket packet = packetBuffer.take();
                    StreamParser.parsePacket(packet);
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }

        System.out.println("WHOOPSIES");


        // TODO: 14/07/17 wmu16 - Send out disconnect packet to clients
        try {
            serverSocket.close();
            return;
        } catch (IOException e) {
            System.out.println("IO error in server thread handler upon closing socket");
        }
    }

    public void updateClients() {
        for (ServerToClientThread serverToClientThread : serverToClientThreads) {
            serverToClientThread.updateClient();
        }
    }

    @Override
    public boolean addToBuffer(StreamPacket streamPacket) {
        System.out.println("HEY HI");
        return packetBuffer.add(streamPacket);
    }
}
