package seng302.gameServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.messages.*;
import seng302.model.GeoPoint;
import seng302.model.Player;
import seng302.model.PolarTable;
import seng302.model.ServerYacht;
import seng302.model.mark.CompoundMark;
import seng302.model.token.Token;
import seng302.model.token.TokenType;
import seng302.utilities.GeoUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

/**
 * A class describing the overall server, which creates and collects server threads for each client
 * Created by wmu16 on 13/07/17.
 */
public class MainServerThread implements Runnable, ClientConnectionDelegate {

    private Logger logger = LoggerFactory.getLogger(MainServerThread.class);

    private static final int PORT = 4942;
    private static final Integer CLIENT_UPDATES_PER_SECOND = 60;

    private static final int MAX_WIND_SPEED = 12000;
    private static final int MIN_WIND_SPEED = 8000;

    private boolean terminated;

    private Thread thread;

    private ServerSocket serverSocket = null;
    private ArrayList<ServerToClientThread> serverToClientThreads = new ArrayList<>();

    public MainServerThread() {
        new GameState("localhost");
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            logger.trace("IO error in server thread handler upon trying to make new server socket",
                0);
        }
        PolarTable.parsePolarFile(getClass().getResourceAsStream("/config/acc_polars.csv"));
        GameState.addMessageEventListener(this::broadcastMessage);
        terminated = false;
        thread = new Thread(this, "MainServer");
        startUpdatingWind();
        startSpawningTokens();
        thread.start();
    }


    public void run() {

        new HeartbeatThread(this);
        new ServerListenThread(serverSocket, this);

        //You should handle interrupts in some way, so that the thread won't keep on forever if you exit the app.
        while (!terminated) {
            try {
                Thread.sleep(1000 / CLIENT_UPDATES_PER_SECOND);
            } catch (InterruptedException e) {
                logger.trace("Interrupted exception in Main Server Thread thread sleep", 1);
            }
            if (GameState.getCurrentStage() == GameStages.LOBBYING && GameState
                .getCustomizationFlag()) {
                // TODO: 16/08/17 ajm412: This can probably be done in a nicer way via those fancy functional interfaces.
                sendSetupMessages();
                GameState.resetCustomizationFlag();
            }

            if (GameState.getCurrentStage() == GameStages.PRE_RACE) {
                sendBoatLocations();
            }

            //RACING
            if (GameState.getCurrentStage() == GameStages.RACING) {
                sendBoatLocations();
            }

            //FINISHED
            else if (GameState.getCurrentStage() == GameStages.FINISHED) {
                broadcastMessage(MessageFactory.getRaceStatusMessage());
                try {
                    Thread.sleep(1000); //Hackish fix to make sure all threads have sent closing RaceStatus
                    terminate();
                } catch (InterruptedException ie) {
                    logger.trace("Thread interrupted while waiting to terminate clients", 1);
                }
            }
        }
        try {
            for (ServerToClientThread serverToClientThread : serverToClientThreads) {
                serverToClientThread.terminate();
            }
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("IO error in server thread handler upon closing socket");
        }
    }

    private void sendBoatLocations() {
        for (ServerYacht serverYacht : GameState.getYachts().values()) {
            broadcastMessage(MessageFactory.getBoatLocationMessage(serverYacht));
        }
    }

    private void sendSetupMessages() {
        broadcastMessage(MessageFactory.getRaceXML());
        broadcastMessage(MessageFactory.getRegattaXML());
        broadcastMessage(MessageFactory.getBoatXML());
    }

    private void broadcastMessage(Message message) {
        for (ServerToClientThread serverToClientThread : serverToClientThreads) {
            serverToClientThread.sendMessage(message);
        }
    }

    private static void updateWind(){
        Integer direction = GameState.getWindDirection().intValue();
        Integer windSpeed = GameState.getWindSpeedMMS().intValue();

        Random random = new Random();

        if (Math.floorMod(random.nextInt(), 2) == 0){
            direction += random.nextInt(4);
            windSpeed += random.nextInt(20) + 50;
        }
        else{
            direction -= random.nextInt(4);
            windSpeed -= random.nextInt(20) + 50;
        }

        direction = Math.floorMod(direction, 360);

        if (windSpeed > MAX_WIND_SPEED){
            windSpeed -= random.nextInt(1000);
        }

        if (windSpeed <= MIN_WIND_SPEED){
            windSpeed += random.nextInt(1000);
        }

        GameState.setWindSpeed(Double.valueOf(windSpeed));
        GameState.setWindDirection(direction.doubleValue());
    }

    // TODO: 29/08/17 wmu16 - This should not be in one function (init and a scheduling update)
    public void startGame() {
        initialiseBoatPositions();
        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                broadcastMessage(MessageFactory.getRaceStatusMessage());
                if (GameState.getCurrentStage() == GameStages.PRE_RACE
                    || GameState.getCurrentStage() == GameStages.LOBBYING) {
                    broadcastMessage(MessageFactory.getRaceStartStatusMessage());
                }
            }
        }, 0, 500);
    }


    // TODO: 29/08/17 wmu16 - This sort of update should be in game state
    private static void startUpdatingWind(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateWind();
            }
        }, 0, 500);
    }

    /**
     * Start spawning coins every 60s after the first minute
     */
    private void startSpawningTokens() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                GameState.spawnNewToken();
                broadcastMessage(MessageFactory.getRaceXML());
            }
        }, 0, 60000);
    }

    /**
     * A client has tried to connect to the server
     *
     * @param serverToClientThread The player that connected
     */
    @Override
    public void clientConnected(ServerToClientThread serverToClientThread) {
        logger.debug("Player Connected From " + serverToClientThread.getThread().getName(), 0);
        if (serverToClientThreads.size() == 0) { //Sets first client as host.
            serverToClientThread.setAsHost();
        }
        serverToClientThreads.add(serverToClientThread);
        serverToClientThread.addConnectionListener(this::sendSetupMessages);
        serverToClientThread.addDisconnectListener(this::clientDisconnected);
    }

    /**
     * A player has left the game, remove the player from the GameState
     *
     * @param player The player that left
     */
    @Override
    public void clientDisconnected(Player player) {
        logger.debug("Player " + player.getYacht().getSourceId() + "'s socket disconnected", 0);
        GameState.removeYacht(player.getYacht().getSourceId());
        GameState.removePlayer(player);
        ServerToClientThread closedConnection = null;
        for (ServerToClientThread serverToClientThread : serverToClientThreads) {
            if (serverToClientThread.getSocket() == player.getSocket()) {
                closedConnection = serverToClientThread;
            }
        }

        serverToClientThreads.remove(closedConnection);
        closedConnection.terminate();

        if (GameState.getCurrentStage() == GameStages.LOBBYING) {
            sendSetupMessages();
        }
    }

    public void terminate() {
        terminated = true;
    }

    /**
     * Initialise boats to specific spaced out geopoints behind starting line.
     */
    private void initialiseBoatPositions() {
        CompoundMark cm = GameState.getMarkOrder().getMarkOrder().get(0);
        GeoPoint startMark1 = cm.getSubMark(1);
        GeoPoint startMark2 = cm.getSubMark(2);

        // Calculating midpoint
        Double perpendicularAngle = GeoUtility.getBearing(startMark1, startMark2);
        Double length = GeoUtility.getDistance(startMark1, startMark2);
        GeoPoint midpoint = GeoUtility.getGeoCoordinate(startMark1, perpendicularAngle, length / 2);

        // Setting each boats position side by side
        double DISTANCE_FACTOR = 50.0;  // distance apart in meters
        int boatIndex = 0;
        for (ServerYacht yacht : GameState.getYachts().values()) {
            int distanceApart = boatIndex / 2;

            if (boatIndex % 2 == 1 && boatIndex != 0) {
                distanceApart++;
                distanceApart *= -1;
            }

            GeoPoint spawnMark = GeoUtility
                .getGeoCoordinate(midpoint, perpendicularAngle, distanceApart * DISTANCE_FACTOR);

            if (yacht.getHeading() < perpendicularAngle) {
                spawnMark = GeoUtility
                    .getGeoCoordinate(spawnMark, perpendicularAngle + 90, DISTANCE_FACTOR);
            } else {
                spawnMark = GeoUtility
                    .getGeoCoordinate(spawnMark, perpendicularAngle + 270, DISTANCE_FACTOR);
            }

            yacht.setLocation(spawnMark);
            boatIndex++;
        }
    }
}
