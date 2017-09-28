package seng302.gameServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.messages.Message;
import seng302.model.GeoPoint;
import seng302.model.Player;
import seng302.model.PolarTable;
import seng302.model.ServerYacht;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.utilities.GeoUtility;


/**
 * A class describing the overall server, which creates and collects server threads for each client
 * Created by wmu16 on 13/07/17.
 */
public class MainServerThread implements Runnable, ClientConnectionDelegate {

    private static final int PORT = 4942;
    private static int selectedPort = PORT;
    private static final Integer CLIENT_UPDATES_PER_SECOND = 60;
    private Logger logger = LoggerFactory.getLogger(MainServerThread.class);
    private boolean terminated;

    private boolean hasStarted = false;

    private ServerSocket serverSocket = null;
    private ArrayList<ServerToClientThread> serverToClientThreads = new ArrayList<>();
    private RaceXMLData raceXMLData;
    private RegattaXMLData regattaXMLData;

    public MainServerThread() {
        new GameState();
        try {
            serverSocket = new ServerSocket(0);
            selectedPort = serverSocket.getLocalPort();
        } catch (IOException e) {
            logger.trace("IO error in server thread handler upon trying to make new server socket",
                0);
        }
        terminated = false;
        Thread thread = new Thread(this, "MainServer");
        thread.start();
    }

    private void startAdvertisingServer() {
        Integer capacity = GameState.getCapacity();
        Integer numPlayers = GameState.getNumberOfPlayers();
        Integer spacesLeft = capacity - numPlayers;

        // No spaces left on server
        if (spacesLeft < 1) {
            return;
        }

        // Start advertising server
        try {
            ServerAdvertiser.getInstance()
                .setMapName(regattaXMLData.getCourseName())
                .setCapacity(capacity)
                .setNumberOfPlayers(numPlayers - 1)
                .registerGame(selectedPort, regattaXMLData.getRegattaName());
        } catch (IOException e) {
            logger.warn("Could not register server");
        }
    }

    private void startServer() {
        PolarTable.parsePolarFile(getClass().getResourceAsStream("/server_config/acc_polars.csv"));
        MessageFactory.updateXMLGenerator(raceXMLData, regattaXMLData);
        GameState.setRace(raceXMLData);
        MessageFactory.updateBoats(new ArrayList<>(GameState.getYachts().values()));
        startAdvertisingServer();
        GameState.addMessageEventListener(this::broadcastMessage);
        sendSetupMessages();
    }

    public void run() {

        new HeartbeatThread(this);
        new ServerListenThread(serverSocket, this);

        hasStarted = true;

        //You should handle interrupts in some way, so that the thread won't keep on forever if you exit the app.
        while (!terminated) {
            if (GameState.getPlayerHasLeftFlag()) {
                for (ServerToClientThread stc : serverToClientThreads) {
                    if (!stc.isSocketOpen()) {
                        GameState.getYachts().remove(stc.getSourceId());
                        sendSetupMessages();
                        try {
                            stc.getSocket().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                GameState.setPlayerHasLeftFlag(false);
            }
            try {
                Thread.sleep(1000 / CLIENT_UPDATES_PER_SECOND);
            } catch (InterruptedException e) {
                logger.trace("Interrupted exception in Main Server Thread thread sleep", 1);
            }
            if (GameState.getCurrentStage() == GameStages.LOBBYING && GameState
                .getCustomizationFlag()) {
                MessageFactory.updateBoats(new ArrayList<>(GameState.getYachts().values()));
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
                    Thread.sleep(
                        1000); //Hackish fix to make sure all threads have sent closing RaceStatus
                    terminate();
                } catch (InterruptedException ie) {
                    logger.trace("Thread interrupted while waiting to terminate clients", 1);
                }
            }
        }
        try {
            synchronized (this) {
                for (ServerToClientThread serverToClientThread : serverToClientThreads) {
                    serverToClientThread.terminate();
                }
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
        MessageFactory.updateBoats(new ArrayList<>(GameState.getYachts().values()));
        broadcastMessage(MessageFactory.getRaceXML());
        broadcastMessage(MessageFactory.getRegattaXML());
        broadcastMessage(MessageFactory.getBoatXML());
    }

    private void broadcastMessage(Message message) {
        for (ServerToClientThread serverToClientThread : serverToClientThreads) {
            serverToClientThread.sendMessage(message);
        }
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
            serverToClientThread.raceXMLProperty().addListener((obs, oldVal, race) -> {
                if (race != null) {
                    raceXMLData = race;
                }
                if (regattaXMLData != null) {
                    startServer();
                }
            });
            serverToClientThread.regattaXMLProperty().addListener((obs, oldVal, regatta) -> {
                if (regatta != null) {
                    regattaXMLData = regatta;
                }
                if (raceXMLData != null) {
                    startServer();
                }
            });
        }

        serverToClientThreads.add(serverToClientThread);

        try {
            ServerAdvertiser.getInstance().setNumberOfPlayers(GameState.getNumberOfPlayers());
        } catch (IOException e) {
            logger.warn("Couldn't update advertisement");
        }

        while (regattaXMLData == null && raceXMLData == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
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
            } else if (GameState.getCurrentStage() != GameStages.RACING) {
                serverToClientThread.sendSetupMessages();
            }
        }

        serverToClientThreads.remove(closedConnection);

        try {
            ServerAdvertiser.getInstance().setNumberOfPlayers(GameState.getNumberOfPlayers());
        } catch (IOException e) {
            logger.warn("Couldn't update advertisement");
        }

        if (closedConnection != null) {
            closedConnection.terminate();
        }
    }

    public void startGame() {
        try {
            ServerAdvertiser.getInstance().unregister();
        } catch (IOException e) {
            logger.warn("Error unregistering server");
        }

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

    public void terminate() {
        terminated = true;
    }

    /**
     * Initialise boats to specific spaced out geopoints behind starting line.
     */
    private void initialiseBoatPositions() {

        final double DISTANCE_TO_START = 75d;
        final double YACHT_SEPARATION = 20d;

        //Length of start line
        double startLineLength = GeoUtility.getDistance(
            GameState.getMarkOrder().getMarkOrder().get(0).getSubMark(1),
            GameState.getMarkOrder().getMarkOrder().get(0).getSubMark(2)
        );

        //How many yachts can fit along the start line
        int spacesAlongLine = (int) Math.round(startLineLength / YACHT_SEPARATION);

        //Angle of start line
        double startMarkToMarkAngle = GeoUtility.getBearing(
            GameState.getMarkOrder().getMarkOrder().get(0).getSubMark(1),
            GameState.getMarkOrder().getMarkOrder().get(0).getSubMark(2)
        );

        //angle from first mark to the start
        double angleToStart = GeoUtility.getBearing(
            GameState.getMarkOrder().getMarkOrder().get(1).getMidPoint(),
            GameState.getMarkOrder().getMarkOrder().get(0).getMidPoint()
        );

        double angleFromStart = GeoUtility.getBearing(
            GameState.getMarkOrder().getMarkOrder().get(0).getMidPoint(),
            GameState.getMarkOrder().getMarkOrder().get(1).getMidPoint()
        );

        GeoPoint startingPoint = GeoUtility.getGeoCoordinate(
            GameState.getMarkOrder().getMarkOrder().get(0).getMidPoint(),
            angleToStart, DISTANCE_TO_START
        );

        List<ServerYacht> randomisedYachts = new ArrayList<>(GameState.getYachts().values());
        Collections.shuffle(randomisedYachts);
        while (randomisedYachts.size() > 0) {

            int numYachtsInLine =
                spacesAlongLine > randomisedYachts.size() ? randomisedYachts.size()
                    : spacesAlongLine;
            double yachtSpace = numYachtsInLine * YACHT_SEPARATION / 2;

            GeoPoint firstYachtPoint = GeoUtility.getGeoCoordinate(
                startingPoint, startMarkToMarkAngle + 180, yachtSpace
            );

            for (int i = 0; i < numYachtsInLine; i++) {
                randomisedYachts.get(0).setHeading(angleFromStart);
                randomisedYachts.get(0).setLocation(firstYachtPoint);
                firstYachtPoint = GeoUtility.getGeoCoordinate(
                    firstYachtPoint, startMarkToMarkAngle, yachtSpace
                );
                randomisedYachts.remove(0);
            }

            startingPoint = GeoUtility.getGeoCoordinate(
                startingPoint, angleToStart, DISTANCE_TO_START
            );
        }
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public int getPortNumber() {
        return selectedPort;
    }
}
