package seng302.gameServer;

import seng302.client.ClientPacketParser;
import seng302.models.Player;
import seng302.models.stream.PacketBufferDelegate;
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
public class MainServerThread extends Thread implements PacketBufferDelegate, ClientConnectionDelegate{

    private static final int PORT = 4950;
    private static final Integer MAX_NUM_PLAYERS = 3;
    private static final int LOG_LEVEL = 1;

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
        ServerListenThread serverListenThread;
        HeartbeatThread heartbeatThread;

        serverListenThread = new ServerListenThread(serverSocket, this);
        heartbeatThread = new HeartbeatThread(this);

        heartbeatThread.start();
        serverListenThread.start();

        //You should handle interrupts in some way, so that the thread won't keep on forever if you exit the app.
        while (!isInterrupted()) {
            try {
                Thread.sleep(1000 / 60);    //60 times per second we should calculate the game state
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (GameState.getCurrentStage() == GameStages.PRE_RACE) {
                GameState.update();
            }
            //RACING
            if (GameState.getCurrentStage() == GameStages.RACING) {
                GameState.update();
            }


            //FINISHED
            else if (GameState.getCurrentStage() == GameStages.FINISHED) {

            }

            updateClients();

            while (!packetBuffer.isEmpty()){
                System.out.println("WHATUPPP");
                try {
                    StreamPacket packet = packetBuffer.take();
                    ClientPacketParser.parsePacket(packet);
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


    static void serverLog(String message, int logLevel){
        if(logLevel <= LOG_LEVEL){
            System.out.println("[SERVER] " + message);
        }
    }

    @Override
    public boolean addToBuffer(StreamPacket streamPacket) {
        System.out.println("HEY HI");
        return packetBuffer.add(streamPacket);
    }

    /**
     * A client has tried to connect to the server
     * @param serverToClientThread The player that connected
     */
    @Override
    public void clientConnected(ServerToClientThread serverToClientThread) {
        serverLog("Player Connected From " + serverToClientThread.getName(), 0);
        serverToClientThreads.add(serverToClientThread);

    }

    /**
     * A player has left the game, remove the player from the GameState
     * @param player The player that left
     */
    @Override
    public void clientDisconnected(Player player) {
        serverLog("Player disconnected", 0);
        GameState.removePlayer(player);
//        sendXml();
    }

}
