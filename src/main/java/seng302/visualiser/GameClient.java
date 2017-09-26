package seng302.visualiser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
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

    private Pane holderPane;
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
     * @param holder The JavaFX Pane that the visual elements for the race will be inserted into.
     */
    public GameClient(Pane holder) {
        this.holderPane = holder;
        this.gameKeyBind = GameKeyBind.getInstance();
    }

    /**
     * Connect to a game at the given address and starts the visualiser.
     * @param ipAddress IP to connect to.
     * @param portNumber Port to connect to.
     */
    public void runAsClient(String ipAddress, Integer portNumber) {
        try {
            startClientToServerThread(ipAddress, portNumber);
            socketThread.addDisconnectionListener((cause) -> {
                showConnectionError(cause);
                tearDownConnection();
                Platform.runLater(this::loadStartScreen);
            });
            socketThread.addStreamObserver(this::parsePackets);

            ViewManager.getInstance().setPlayerList(clientLobbyList);

            while (regattaData == null){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            ViewManager.getInstance().setProperty("serverName", regattaData.getRegattaName());
            ViewManager.getInstance().setProperty("mapName", regattaData.getCourseName());

            this.lobbyController = ViewManager.getInstance().goToLobby(true);

        } catch (IOException ioe) {
            showConnectionError("Unable to find server");
        }
    }

    /**
     * Connect to a game as the host at the given address and starts the visualiser.
     * @param ipAddress IP to connect to.
     * @param portNumber Port to connect to.
     */
    public ServerDescription runAsHost(String ipAddress, Integer portNumber, String serverName, Integer maxPlayers) {
        XMLGenerator.setDefaultRaceName(serverName);
        GameState.setMaxPlayers(maxPlayers);

        server = new MainServerThread();

        try {
            startClientToServerThread(ipAddress, 4942);
        } catch (IOException e) {
            showConnectionError("Cannot connect to server as host");
        }

        while (regattaData == null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.lobbyController = ViewManager.getInstance().goToLobby(false);

        ViewManager.getInstance().setPlayerList(clientLobbyList);
        return new ServerDescription(serverName, regattaData.getCourseName(), GameState.getNumberOfPlayers(), GameState.getCapacity(), ipAddress, 4942);
    }

    private void tearDownConnection() {
        socketThread.setSocketToClose();
        if (server != null) {
            server.terminate();
            server = null;
        }
    }

    private void loadStartScreen() {
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource("/views/StartScreenView.fxml"));
        try {
            holderPane.getChildren().clear();
            holderPane.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            showConnectionError("JavaFX crashed. Please restart the app");
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
                    YachtEventData yachtEventData = StreamParser.extractYachtEventCode(packet);
                    if (yachtEventData.getEventId() == YachtEventType.COLLISION.getCode()) {
                        showCollisionAlert(StreamParser.extractYachtEventCode(packet));
                    } else if (yachtEventData.getEventId() == YachtEventType.TOKEN.getCode()) {
                        showPickUp();
                    }
                    break;

                case CHATTER_TEXT:
                    Pair<Integer, String> playerIdMessagePair = StreamParser
                        .extractChatterText(packet);
                    raceView.updateChatHistory(
                        allBoatsMap.get(playerIdMessagePair.getKey()).getColour(),
                        playerIdMessagePair.getValue()
                    );
            }
        }
    }

    private void startRaceIfAllDataReceived() {
        if (allXMLReceived() && raceView == null) {
            raceView = ViewManager.getInstance().loadRaceView();

            ClientYacht player = allBoatsMap.get(socketThread.getClientId());
            raceView.loadRace(allBoatsMap, courseData, raceState, player);

            raceView.getSendPressedProperty().addListener((obs, old, isPressed) -> {
                if (isPressed) {
                    formatAndSendChatMessage(raceView.readChatInput());
                }
            });
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
        } else if (positionData.getType() == DeviceType.MARK_TYPE) {
            //CompoundMark mark = courseData.getCompoundMarks().get(positionData.getDeviceId());
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
//                int legNumber = (int) boatData[3];
                clientYacht.setBoatStatus((int) boatData[4]);
//                if (legNumber != clientYacht.getLegNumber()) {
//                    clientYacht.setLegNumber(legNumber);
//                }
            }

            if (raceFinished) {
                raceViewController.showFinishDialog(finishedBoats);
                Sounds.playFinishSound();
                close();
                ViewManager.getInstance().getGameClient().stopGame();
                //loadFinishScreenView();
            }
            raceState.setRaceFinished();
        }
    }

    private void updatePlayerPositions() {
        raceState.sortPlayers();
        for (ClientYacht yacht : raceState.getPlayerPositions()) {
            yacht.setPosition(raceState.getPlayerPositions().indexOf(yacht) + 1);
        }
    }

    private void close() {
        socketThread.setSocketToClose();
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
     * Tells race view to show a collision animation.
     */
    private void showCollisionAlert(YachtEventData yachtEventData) {
        Sounds.playCrashSound();
        raceState.storeCollision(
            allBoatsMap.get(
                yachtEventData.getSubjectId().intValue()
            )
        );
    }

    // TODO: 11/09/17 wmu16 - Add in functionality to viually indicate a pickup to a user
    private void showPickUp() {
        Sounds.playTokenPickupSound();
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

    public List<String> getPlayerNames(){
        return Collections.unmodifiableList(clientLobbyList.sorted());
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
