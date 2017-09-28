package seng302.visualiser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.ServerDescription;
import seng302.gameServer.messages.BoatAction;
import seng302.gameServer.messages.BoatStatus;
import seng302.gameServer.messages.YachtEventType;
import seng302.model.ClientYacht;
import seng302.model.GameKeyBind;
import seng302.model.KeyAction;
import seng302.model.RaceState;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.parser.MarkRoundingData;
import seng302.model.stream.parser.PositionUpdateData;
import seng302.model.stream.parser.PositionUpdateData.DeviceType;
import seng302.model.stream.parser.RaceStatusData;
import seng302.model.stream.parser.YachtEventData;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.model.token.TokenType;
import seng302.utilities.Sounds;
import seng302.utilities.StreamParser;
import seng302.utilities.XMLGenerator;
import seng302.utilities.XMLParser;
import seng302.visualiser.controllers.LobbyController;
import seng302.visualiser.controllers.RaceViewController;
import seng302.visualiser.controllers.ViewManager;
import seng302.visualiser.controllers.dialogs.PopupDialogController;

/**
 * This class is a client side instance of a yacht racing game in JavaFX. The game is instantiated
 * with a JavaFX Pane to insert itself into.
 */
public class GameClient {

    private ClientToServerThread socketThread;
    private MainServerThread server;

    private RaceViewController raceView;

    private Map<Integer, ClientYacht> allBoatsMap;
    private RegattaXMLData regattaData;
    private RaceXMLData courseData;
    private RaceState raceState = new RaceState();
    private LobbyController lobbyController;
    private RaceViewController raceViewController;

    private ArrayList<ClientYacht> finishedBoats = new ArrayList<>();

    private GameKeyBind gameKeyBind; // all the key binding setting.

    private ObservableList<String> clientLobbyList = FXCollections.observableArrayList();

    /**
     * Create an instance of the game client. Does not do anything until run with runAsClient()
     * runAsHost().
     */
    public GameClient() {
        this.gameKeyBind = GameKeyBind.getInstance();
    }

    /**
     * Connect to a game at the given address and starts the visualiser.
     * @param ipAddress IP to connect to.
     * @param portNumber Port to connect to.
     */
    public boolean runAsClient(String ipAddress, Integer portNumber) {
        try {
            startClientToServerThread(ipAddress, portNumber);
            socketThread.addDisconnectionListener((cause) -> {
                showConnectionError(cause);
                tearDownConnection();
            });
            socketThread.addStreamObserver(this::parsePackets);

            ViewManager.getInstance().setPlayerList(clientLobbyList);

            int triesLeft = 10;

            while (regattaData == null && triesLeft >= 0){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    ;
                }
                triesLeft--;
            }

            if (triesLeft < 1){
                return false;
            }

            ViewManager.getInstance().setProperty("serverName", regattaData.getRegattaName());
            ViewManager.getInstance().setProperty("mapName", regattaData.getCourseName());

            getServerThread().setConnectionErrorListener((eMessage) -> ViewManager.getInstance().showErrorSnackBar(eMessage));

            this.lobbyController = ViewManager.getInstance().goToLobby(true);

        } catch (IOException ioe) {
            ViewManager.getInstance().showErrorSnackBar("There are no servers currently available.");
        }

        return true;
    }

    /**
     * Connect to a game as the host at the given address and starts the visualiser.
     */
    public ServerDescription runAsHost(
        String serverName, Integer maxPlayers, String race,
        Integer numLegs, Boolean tokensEnabled
    ) {
        XMLGenerator.setDefaultRaceName(serverName);

        server = new MainServerThread();

        while (!server.hasStarted()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            startClientToServerThread("localhost", server.getPortNumber());
        } catch (IOException e) {
            showConnectionError("Cannot connect to server as host");
        }

        // Wait for C2S thread
        while (!socketThread.hasStarted()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        socketThread.sendXML(race, serverName, numLegs, maxPlayers, tokensEnabled);

        int triesLeft = 15;

        while (regattaData == null && triesLeft > 0){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            triesLeft--;
        }

        if (triesLeft <= 0){
            showConnectionError("Could not launch server");
            return null;
        }

        this.lobbyController = ViewManager.getInstance().goToLobby(false);

        lobbyController.setPortNumber(""+server.getPortNumber());

        ViewManager.getInstance().setPlayerList(clientLobbyList);
        return new ServerDescription(serverName, regattaData.getCourseName(), GameState.getNumberOfPlayers(), GameState.getCapacity(),
            "localhost", server.getPortNumber());
    }

    private void tearDownConnection() {
        socketThread.setSocketToClose();
        if (server != null) {
            server.terminate();
            server = null;
        }
    }


    private void showConnectionError (String message) {
        Platform.runLater(() -> {
            PopupDialogController controller = ViewManager.getInstance().showPopupDialog();
            controller.setHeader("Oops");
            controller.setContent(message);
            controller.setOptionButtonText("GO HOME");
            controller
                .setOptionButtonEventHandler(event -> ViewManager.getInstance().goToStartView());
        });
    }

    private void startClientToServerThread (String ipAddress, int portNumber) throws IOException {
        socketThread = new ClientToServerThread(ipAddress, portNumber);
        socketThread.addStreamObserver(this::parsePackets);
    }

    public void setRaceViewController(RaceViewController controller) {
        this.raceViewController = controller;
    }

    private void parsePackets() {
        while (socketThread.getPacketQueue().peek() != null) {
            StreamPacket packet = socketThread.getPacketQueue().poll();
            switch (packet.getType()) {
                case RACE_STATUS:
                    processRaceStatusUpdate(StreamParser.extractRaceStatus(packet));

                    if (raceState.getTimeTillStart() <= 5000) {
                        startRaceIfAllDataReceived();
                    }

                    break;

                case REGATTA_XML:
                    regattaData = XMLParser.parseRegatta(
                        StreamParser.extractXmlMessage(packet)
                    );

                    raceState.setTimeZone(
                        TimeZone.getTimeZone(
                            ZoneId.ofOffset("UTC", ZoneOffset.ofHours(regattaData.getUtcOffset()))
                        )
                    );
                    break;

                case RACE_XML:
                    RaceXMLData raceXMLData = XMLParser.parseRace(
                        StreamParser.extractXmlMessage(packet)
                    );
                    if (courseData == null) { //workaround for object comparisons. Avoid recreating
                        courseData = raceXMLData;
                    }
                    if (raceView != null) {  //Token update
                        raceView.updateTokens(raceXMLData);
                    }
                    break;

                case BOAT_XML:
                    allBoatsMap = XMLParser.parseBoats(
                        StreamParser.extractXmlMessage(packet)
                    );
                    clientLobbyList.clear();
                    allBoatsMap.forEach((id, boat) ->
                        clientLobbyList.add(boat.getBoatName())
                    );
                    raceState.setBoats(allBoatsMap.values());
                    if (lobbyController != null) {
                        lobbyController.setBoats(allBoatsMap);
                    }
                    break;

                case RACE_START_STATUS:
                    raceState.updateState(StreamParser.extractRaceStartStatus(packet));
                    if (lobbyController != null) Platform.runLater(() -> lobbyController.updateRaceState(raceState));
                    break;

                case BOAT_LOCATION:
                    updatePosition(StreamParser.extractBoatLocation(packet));
                    break;

                case MARK_ROUNDING:
                    updateMarkRounding(StreamParser.extractMarkRounding(packet));
                    break;

                case YACHT_EVENT_CODE:
                    processYachtEvent(StreamParser.extractYachtEventCode(packet));
                    break;

                case CHATTER_TEXT:
                    Pair<Integer, String> playerIdMessagePair = StreamParser
                        .extractChatterText(packet);
                    if (playerIdMessagePair != null) {
                        raceView.updateChatHistory(
                            allBoatsMap.get(playerIdMessagePair.getKey()).getColour(),
                            playerIdMessagePair.getValue()
                        );
                    }
            }
        }
    }

    private void startRaceIfAllDataReceived() {
        if (allXMLReceived() && raceView == null) {
            raceView = ViewManager.getInstance().loadRaceView();

            ClientYacht player = allBoatsMap.get(socketThread.getClientId());
            raceView.loadRace(allBoatsMap, courseData, raceState, player);
            raceView.showView();
            raceView.getSendPressedProperty().addListener((obs, old, isPressed) -> {
                if (isPressed) {
                    formatAndSendChatMessage(raceView.readChatInput());
                }
            });
            gameKeyBind.toggleTurningMode();
            sendToggleTurningModePacket(); // notify the server about player's steering mode
        }
    }

    private boolean allXMLReceived() {
        return courseData != null && allBoatsMap != null && regattaData != null;
    }

    /**
     * Updates the position of a boat. Boat and position are given in the provided data.
     */
    private void updatePosition(PositionUpdateData positionData) {
        if (positionData.getType() == DeviceType.YACHT_TYPE) {
            if (allXMLReceived() && allBoatsMap.containsKey(positionData.getDeviceId())) {
                ClientYacht yacht = allBoatsMap.get(positionData.getDeviceId());
                yacht.updateLocation(positionData.getLat(),
                    positionData.getLon(), positionData.getHeading(),
                    positionData.getGroundSpeed());
            }
        }
    }

    /**
     * Updates the boat as having passed the mark. Boat and mark are given by the ids in the
     * provided data.
     *
     * @param roundingData Contains data for the rounding of a mark.
     */
    private void updateMarkRounding(MarkRoundingData roundingData) {
        if (allXMLReceived()) {
            ClientYacht clientYacht = allBoatsMap.get(roundingData.getBoatId());
            clientYacht.roundMark(
                roundingData.getTimeStamp(),
                raceState.getRaceTime() - roundingData.getTimeStamp()
            );
        }
        updatePlayerPositions();
    }

    private void processRaceStatusUpdate(RaceStatusData data) {
        if (allXMLReceived()) {
            raceState.updateState(data);
            boolean raceFinished = true;
            for (ClientYacht yacht : allBoatsMap.values()) {
                if (yacht.getBoatStatus() != BoatStatus.FINISHED.getCode()) {
                    raceFinished = false;
                } else if (!finishedBoats.contains(yacht)) {
                    finishedBoats.add(yacht);
                    Sounds.playFinishSound();
                }
            }

            for (long[] boatData : data.getBoatData()) {
                ClientYacht clientYacht = allBoatsMap.get((int) boatData[0]);
                clientYacht.setEstimateTimeTillNextMark(raceState.getRaceTime() - boatData[1]);
                clientYacht.setEstimateTimeAtFinish(boatData[2]);
                clientYacht.setBoatStatus((int) boatData[4]);
            }

            if (raceFinished && !raceState.getRaceFinished()) {
                raceState.setRaceFinished();
                Sounds.playFinishSound();
                raceViewController.showFinishDialog(finishedBoats);
//                close();
//                ViewManager.getInstance().getGameClient().stopGame();
                //loadFinishScreenView();
            }
        }
    }

    private void updatePlayerPositions() {
        raceState.sortPlayers();
        for (ClientYacht yacht : raceState.getPlayerPositions()) {
            yacht.setPosition(raceState.getPlayerPositions().indexOf(yacht) + 1);
        }
    }

    /**
     * Handle the key-pressed event from the text field.
     * @param e The key event triggering this call
     */
    public void keyPressed(KeyEvent e) {
        if (raceView.isChatInputFocused()) {
            if (e.getCode() == KeyCode.ENTER) {
                formatAndSendChatMessage(raceView.readChatInput());
            }
            return;
        }

        if (gameKeyBind.getKeyCode(KeyAction.VMG) == e.getCode()) { // align with vmg
            socketThread.sendBoatAction(BoatAction.VMG);
        } else if (gameKeyBind.getKeyCode(KeyAction.UPWIND) == e.getCode()) { // upwind
            socketThread.sendBoatAction(BoatAction.UPWIND);
        } else if (gameKeyBind.getKeyCode(KeyAction.DOWNWIND) == e.getCode()) { // downwind
            socketThread.sendBoatAction(BoatAction.DOWNWIND);
        } else if (gameKeyBind.getKeyCode(KeyAction.TACK_GYBE) == e.getCode()) { // tack/gybe
            // if chat box is active take whatever is in there and send it to server
            socketThread.sendBoatAction(BoatAction.TACK_GYBE);
        }
    }


    public void keyReleased(KeyEvent e) {
        if (raceView.isChatInputFocused()) {
            return;
        }

        if (gameKeyBind.getKeyCode(KeyAction.SAILS_STATE) == e.getCode()) {  // sails in/sails out
            if (allBoatsMap.get(socketThread.getClientId()).getSailIn()) {
                socketThread.sendBoatAction(BoatAction.SAILS_OUT);
            } else {
                socketThread.sendBoatAction(BoatAction.SAILS_IN);
            }
            allBoatsMap.get(socketThread.getClientId()).toggleSail();
        } else if (gameKeyBind.getKeyCode(KeyAction.UPWIND) == e.getCode()
            || gameKeyBind.getKeyCode(KeyAction.DOWNWIND) == e.getCode()) {
            socketThread.sendBoatAction(BoatAction.MAINTAIN_HEADING);
        }
    }

    public RaceXMLData getCourseData() {
        return courseData;
    }


    /**
     * Appropriately displays the event client side given the YachtEventCode (collision / token..)
     *
     * @param yachtEventData The YachtEvent data packet
     */
    private void processYachtEvent(YachtEventData yachtEventData) {
        ClientYacht thisYacht = allBoatsMap.get(yachtEventData.getSubjectId().intValue());

        if (yachtEventData.getEventId() == YachtEventType.COLLISION.getCode()) {
            showCollisionAlert(thisYacht);
        } else if (yachtEventData.getEventId() == YachtEventType.POWER_DOWN.getCode()) {
            thisYacht.powerDown();
            Sounds.playTokenPickupSound();  // TODO: 23/09/17 This should be power down sound
        } else if (yachtEventData.getEventId() == YachtEventType.BUMPER_CRASH.getCode()) {
            showDisableAlert(thisYacht);
        } else {        //Else all token pickup types
            TokenType tokenType = null;
            if (yachtEventData.getEventId() == YachtEventType.TOKEN_VELOCITY.getCode()) {
                tokenType = TokenType.BOOST;
            } else if (yachtEventData.getEventId() == YachtEventType.TOKEN_BUMPER.getCode()) {
                tokenType = TokenType.BUMPER;
            } else if (yachtEventData.getEventId() == YachtEventType.TOKEN_HANDLING.getCode()) {
                tokenType = TokenType.HANDLING;
            } else if (yachtEventData.getEventId() == YachtEventType.TOKEN_RANDOM.getCode()) {
                tokenType = TokenType.RANDOM;
            } else if (yachtEventData.getEventId() == YachtEventType.TOKEN_WIND_WALKER.getCode()) {
                tokenType = TokenType.WIND_WALKER;
            }

            Sounds.playTokenPickupSound();
            thisYacht.setPowerUp(tokenType);
        }
    }


    /**
     * Turns a disabled boat black until the bumper affect wears off
     *
     * @param yacht The yacht to show as disabled
     */
    private void showDisableAlert(ClientYacht yacht) {
        Color originalColor = yacht.getColour();
        yacht.setColour(Color.BLACK);

        Timer disableTimer = new Timer("Disable Timer");
        disableTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                yacht.setColour(originalColor);
            }
        }, GameState.BUMPER_DISABLE_TIME);
    }

    /**
     * Tells race view to show a collision animation.
     */
    private void showCollisionAlert(ClientYacht yacht) {
        Sounds.playCrashSound();
        raceState.storeCollision(yacht);
    }

    private void formatAndSendChatMessage(String rawChat) {
        if (rawChat.length() > 0) {
            socketThread.sendChatterMessage(
                new SimpleDateFormat("[HH:mm:ss] ").format(new Date()) +
                    allBoatsMap.get(socketThread.getClientId()).getShortName() + ": " + rawChat
            );
        }
    }

    public void startGame(){
        server.startGame();
    }

    public ClientToServerThread getServerThread() {
        return socketThread;
    }

    public void stopGame() {
        GameState.setCurrentStage(GameStages.CANCELLED);
        if (server != null) server.terminate();
        if (socketThread != null) socketThread.setSocketToClose();
        server = null;
//        socketThread = null;
    }

    public Map<Integer, ClientYacht> getAllBoatsMap() {
        return allBoatsMap;
    }

    public void sendToggleTurningModePacket() {
        if (socketThread != null) {
            if (gameKeyBind.isContinuouslyTurning()) {
                socketThread.sendBoatAction(BoatAction.CONTINUOUSLY_TURNING);
            } else {
                socketThread.sendBoatAction(BoatAction.DEFAULT_TURNING);
            }
        }
    }
}
