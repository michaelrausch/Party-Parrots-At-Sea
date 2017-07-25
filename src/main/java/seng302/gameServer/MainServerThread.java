package seng302.gameServer;

import java.time.LocalDateTime;
import java.util.Observable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import seng302.model.Player;
import seng302.model.stream.PacketBufferDelegate;
import seng302.model.stream.packets.StreamPacket;

/**
 * A class describing the overall server, which creates and collects server threads for each client
 * Created by wmu16 on 13/07/17.
 */
public class MainServerThread extends Observable implements Runnable, PacketBufferDelegate, ClientConnectionDelegate{

    private static final int PORT = 4942;
    private static final Integer MAX_NUM_PLAYERS = 3;
    private static final Integer UPDATES_PER_SECOND = 2;
    private static final int LOG_LEVEL = 1;

    private Thread thread;

    private ServerSocket serverSocket = null;
    private Socket socket;
    private ArrayList<ServerToClientThread> serverToClientThreads = new ArrayList<>();

    private PriorityBlockingQueue<StreamPacket> packetBuffer;


    public MainServerThread() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            serverLog("IO error in server thread handler upon trying to make new server socket", 0);
        }

        packetBuffer = new PriorityBlockingQueue<>();

        thread = new Thread(this);
        thread.start();
    }


    public void run() {
        ServerListenThread serverListenThread;
        HeartbeatThread heartbeatThread;

        serverListenThread = new ServerListenThread(serverSocket, this);
        heartbeatThread = new HeartbeatThread(this);

        heartbeatThread.start();
        serverListenThread.start();


        //You should handle interrupts in some way, so that the thread won't keep on forever if you exit the app.
        while (!thread.isInterrupted()) {
            try {
                Thread.sleep(1000 / UPDATES_PER_SECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (GameState.getCurrentStage() == GameStages.PRE_RACE) {
                GameState.update();
            }

            //RACING
            if (GameState.getCurrentStage() == GameStages.RACING) {
                GameState.update();
                updateClients();
            }

            //FINISHED
            else if (GameState.getCurrentStage() == GameStages.FINISHED) {

            }

            while (!packetBuffer.isEmpty()){
                try {
                    StreamPacket packet = packetBuffer.take();
//                    ClientPacketParser.parsePacket(packet);
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }

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
            System.out.println("[SERVER " + LocalDateTime.now().toLocalTime().toString() + "] " + message);
        }
    }

    @Override
    public boolean addToBuffer(StreamPacket streamPacket) {
        return packetBuffer.add(streamPacket);
    }

    /**
     * A client has tried to connect to the server
     * @param serverToClientThread The player that connected
     */
    @Override
    public void clientConnected(ServerToClientThread serverToClientThread) {
        serverLog("Player Connected From " + serverToClientThread.getThread().getName(), 0);
        serverToClientThreads.add(serverToClientThread);
        this.addObserver(serverToClientThread);
        setChanged();
        notifyObservers();
    }

    /**
     * A player has left the game, remove the player from the GameState
     * @param player The player that left
     */
    @Override
    public void clientDisconnected(Player player) {
        try {
            player.getSocket().close();
        } catch (Exception e) {
            serverLog("Cannot disconnect the socket for the disconnected player.", 0);
        }
        serverLog("Player " + player.getYacht().getSourceId() + "'s socket disconnected", 0);
        GameState.removeYacht(player.getYacht().getSourceId());
        GameState.removePlayer(player);
        for (ServerToClientThread serverToClientThread : serverToClientThreads) {
            if (serverToClientThread.getSocket() == player.getSocket()) {
                this.deleteObserver(serverToClientThread);
            }
        }
        setChanged();
        notifyObservers();
    }

    public void startGame() {
        for (ServerToClientThread serverToClientThread : serverToClientThreads) {
            serverToClientThread.sendRaceStatusMessage();
        }
    }

    public void shutDown() {
        for (ServerToClientThread serverToClientThread : serverToClientThreads) {
            try {
                serverToClientThread.getSocket().close();
            } catch (IOException ioe) {
                serverLog("Failed to close socket " + serverToClientThread.getSocket().toString(), 0);
            }
        }
        serverToClientThreads = null;
        thread = null;
    }
}
