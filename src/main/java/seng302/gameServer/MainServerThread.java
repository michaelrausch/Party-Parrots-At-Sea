package seng302.gameServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import seng302.model.GeoPoint;
import seng302.model.Player;
import seng302.model.Yacht;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Mark;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.utilities.GeoUtility;
import seng302.visualiser.GameClient;

/**
 * A class describing the overall server, which creates and collects server threads for each client
 * Created by wmu16 on 13/07/17.
 */
public class MainServerThread extends Observable implements Runnable, ClientConnectionDelegate{

    private static final int PORT = 4942;
    private static final Integer CLIENT_UPDATES_PER_SECOND = 10;
    private static final int LOG_LEVEL = 1;
    private boolean terminated;

    private Thread thread;

    private ServerSocket serverSocket = null;
    private ArrayList<ServerToClientThread> serverToClientThreads = new ArrayList<>();

    private GameClient gameClient;

    public MainServerThread() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            serverLog("IO error in server thread handler upon trying to make new server socket", 0);
        }

        terminated = false;
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
        while (!terminated) {
            try {
                Thread.sleep(1000 / CLIENT_UPDATES_PER_SECOND);
            } catch (InterruptedException e) {
                serverLog("Interrupted exception in Main Server Thread thread sleep", 1);
            }

            if (GameState.getCurrentStage() == GameStages.PRE_RACE) {
                updateClients();
            }

            //RACING
            if (GameState.getCurrentStage() == GameStages.RACING) {
                updateClients();
            }

            //FINISHED
            else if (GameState.getCurrentStage() == GameStages.FINISHED) {

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
        initialiseBoatPosition();

        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {

                for (ServerToClientThread serverToClientThread : serverToClientThreads) {
                    serverToClientThread.sendRaceStatusMessage();
                }
            }
        }, 0, 500);
    }

    public void terminate() {
        terminated = true;
    }

    /**
     * Pass GameClient to main server thread so it can access the properties inside.
     *
     * @param gameClient gameClient
     */
    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    /**
     * Initialise boats to specific spaced out geopoint behind starting line.
     */
    private void initialiseBoatPosition() {
        System.out.println("ran");
        RaceXMLData raceXMLData = gameClient.getCourseData();
        CompoundMark cm = raceXMLData.getCompoundMarks().get(1);
        GeoPoint geoPoint1 = new GeoPoint(cm.getMarks().get(0).getLat(), cm.getMarks().get(0).getLng());
        GeoPoint geoPoint2 = new GeoPoint(cm.getMarks().get(1).getLat(), cm.getMarks().get(1).getLng());
        Double perpendicularAngle = GeoUtility.getBearing(geoPoint1, geoPoint2);

        Double x = geoPoint1.getLat() + Math.sin(perpendicularAngle) * 1000;
        Double y = geoPoint1.getLng() + Math.cos(perpendicularAngle) * 1000;

        ServerToClientThread stct0 = serverToClientThreads.get(0);
        Yacht yacht0 = GameState.getYachts().get(stct0.getYacht().getSourceId());
        ServerToClientThread stct1 = serverToClientThreads.get(1);
        Yacht yacht1 = GameState.getYachts().get(stct1.getYacht().getSourceId());
        yacht1.updateLocation(x,y, yacht1.getHeading(), yacht1.getVelocity());

        System.out.println(yacht0.getLat() + " " + yacht0.getLon());
        System.out.println(yacht1.getLat() + " " + yacht1.getLon());

        for (Yacht yacht : GameState.getYachts().values()) {
            System.out.println("GS: " + yacht.getLat() + " " + yacht.getLon());
        }
    }
}
