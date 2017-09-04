package seng302.visualiser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.messages.BoatAction;
import seng302.gameServer.messages.BoatStatus;
import seng302.model.ClientYacht;
import seng302.model.RaceState;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.parser.MarkRoundingData;
import seng302.model.stream.parser.PositionUpdateData;
import seng302.model.stream.parser.PositionUpdateData.DeviceType;
import seng302.model.stream.parser.RaceStatusData;
import seng302.model.stream.parser.YachtEventData;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.utilities.StreamParser;
import seng302.utilities.XMLParser;
import seng302.visualiser.controllers.FinishScreenViewController;
import seng302.visualiser.controllers.LobbyController;
import seng302.visualiser.controllers.LobbyController.CloseStatus;
import seng302.visualiser.controllers.RaceViewController;

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

    private ObservableList<String> clientLobbyList = FXCollections.observableArrayList();

    /**
     * Create an instance of the game client. Does not do anything until run with runAsClient()
     * runAsHost().
     * @param holder The JavaFX Pane that the visual elements for the race will be inserted into.
     */
    public GameClient(Pane holder) {
        this.holderPane = holder;
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
                Platform.runLater(this::loadStartScreen);
            });
            socketThread.addStreamObserver(this::parsePackets);
            LobbyController lobbyController = loadLobby();
            lobbyController.setSocketThread(socketThread);
            lobbyController.setPlayerID(socketThread.getClientId());
            lobbyController.setPlayerListSource(clientLobbyList);
            lobbyController.disableReadyButton();
            if (regattaData != null){
                lobbyController.setTitle(regattaData.getRegattaName());
                lobbyController.setCourseName(regattaData.getCourseName());
            }
            else{
                lobbyController.setTitle(ipAddress);
                lobbyController.setCourseName("");
            }

            lobbyController.addCloseListener((exitCause) -> this.loadStartScreen());
            this.lobbyController = lobbyController;
        } catch (IOException ioe) {
            showConnectionError("Unable to find server");
            Platform.runLater(this::loadStartScreen);
        }
    }

    /**
     * Connect to a game as the host at the given address and starts the visualiser.
     * @param ipAddress IP to connect to.
     * @param portNumber Port to connect to.
     */
    public void runAsHost(String ipAddress, Integer portNumber) {
        server = new MainServerThread();
        try {
            startClientToServerThread(ipAddress, portNumber);
            socketThread.addDisconnectionListener((cause) -> {
                Platform.runLater(this::loadStartScreen);
            });
            LobbyController lobbyController = loadLobby();
            lobbyController.setSocketThread(socketThread);
            lobbyController.setPlayerID(socketThread.getClientId());
            lobbyController.setPlayerListSource(clientLobbyList);
            if (regattaData != null) {
                lobbyController.setTitle("Hosting: " + regattaData.getRegattaName());
                lobbyController.setCourseName(regattaData.getCourseName());
            } else {
                lobbyController.setTitle("Hosting: " + ipAddress);
                lobbyController.setCourseName("");
            }

            lobbyController.addCloseListener(exitCause -> {
                if (exitCause == CloseStatus.READY) {
                    GameState.resetStartTime();
                    lobbyController.disableReadyButton();
                    server.startGame();
                } else if (exitCause == CloseStatus.LEAVE) {
                    server.terminate();
                    server = null;
                    loadStartScreen();
                }
            });
            this.lobbyController = lobbyController;
        } catch (IOException ioe) {
            showConnectionError("Cannot connect to server as host");
            Platform.runLater(this::loadStartScreen);
        }
    }

    private void loadStartScreen() {
        if (socketThread != null) {
            socketThread.setSocketToClose();
        }
        if (server != null) {
            server.terminate();
            server = null;
        }
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
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Connection Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void startClientToServerThread (String ipAddress, int portNumber) throws IOException {
        socketThread = new ClientToServerThread(ipAddress, portNumber);
        socketThread.addStreamObserver(this::parsePackets);
    }

    /**
     * Loads a view of the lobby into the clients pane
     *
     * @return the lobby controller.
     */
    private LobbyController loadLobby() {
        FXMLLoader fxmlLoader = new FXMLLoader(
            GameClient.class.getResource("/views/LobbyView.fxml"));
        try {
            holderPane.getChildren().clear();
            holderPane.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader.getController();
    }

    private void loadRaceView() {
        FXMLLoader fxmlLoader = loadFXMLToHolder("/views/RaceView.fxml");
        holderPane.getScene().setOnKeyPressed(this::keyPressed);
        holderPane.getScene().setOnKeyReleased(this::keyReleased);
        raceView = fxmlLoader.getController();
        ClientYacht player = allBoatsMap.get(socketThread.getClientId());
        raceView.loadRace(allBoatsMap, courseData, raceState, player);
        raceView.getSendPressedProperty().addListener((obs, old, isPressed) -> {
            if (isPressed) {
                formatAndSendChatMessage(raceView.readChatInput());
            }
        });
    }



    private void loadFinishScreenView() {
        FXMLLoader fxmlLoader = loadFXMLToHolder("/views/FinishScreenView.fxml");
        FinishScreenViewController controller = fxmlLoader.getController();
        controller.setFinishers(raceState.getPlayerPositions());
    }

    private FXMLLoader loadFXMLToHolder(String fxmlLocation) {
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource(fxmlLocation)
        );
        try {
            final Node fxmlLoaderFX = fxmlLoader.load();
            Platform.runLater(() -> {
                holderPane.getChildren().clear();
                holderPane.getChildren().add(fxmlLoaderFX);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader;
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
                    courseData = XMLParser.parseRace(
                        StreamParser.extractXmlMessage(packet)
                    );
                    if (raceView != null) {
                        raceView.updateRaceData(courseData);
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
                    break;

                case RACE_START_STATUS:
                    raceState.updateState(StreamParser.extractRaceStartStatus(packet));
                    if (lobbyController != null) lobbyController.updateRaceState(raceState);
                    break;

                case BOAT_LOCATION:
                    updatePosition(StreamParser.extractBoatLocation(packet));
                    break;

                case MARK_ROUNDING:
                    updateMarkRounding(StreamParser.extractMarkRounding(packet));
                    break;

                case YACHT_EVENT_CODE:
                    showCollisionAlert(StreamParser.extractYachtEventCode(packet));
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
            loadRaceView();
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
                courseData.getCompoundMarks().get(roundingData.getMarkId()),
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
                close();
                loadFinishScreenView();
            }
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
    private void keyPressed(KeyEvent e) {
        if (raceView.isChatInputFocused()) {
            if (e.getCode() == KeyCode.ENTER) {
                formatAndSendChatMessage(raceView.readChatInput());
            }
            return;
        }
        switch (e.getCode()) {
            case SPACE: // align with vmg
                socketThread.sendBoatAction(BoatAction.VMG); break;
            case PAGE_UP: // upwind
                socketThread.sendBoatAction(BoatAction.UPWIND); break;
            case PAGE_DOWN: // downwind
                socketThread.sendBoatAction(BoatAction.DOWNWIND); break;
            case ENTER: // tack/gybe
                // if chat box is active take whatever is in there and send it to server
                socketThread.sendBoatAction(BoatAction.TACK_GYBE); break;
        }
    }


    private void keyReleased(KeyEvent e) {
        if (raceView.isChatInputFocused()) {
            return;
        }
        switch (e.getCode()) {
            //TODO 12/07/17 Determine the sail state and send the appropriate packet (eg. if sails are in, send a sail out packet)
            case SHIFT:  // sails in/sails out
                socketThread.sendBoatAction(BoatAction.SAILS_IN);
                allBoatsMap.get(socketThread.getClientId()).toggleSail();
                break;
            case PAGE_UP:
            case PAGE_DOWN:
                socketThread.sendBoatAction(BoatAction.MAINTAIN_HEADING); break;
        }
    }

    public RaceXMLData getCourseData() {
        return courseData;
    }

    /**
     * Tells race view to show a collision animation.
     */
    private void showCollisionAlert(YachtEventData yachtEventData) {
        // 33 is the agreed code to show collision
        if (yachtEventData.getEventId() == 33) {
            raceState.storeCollision(
                allBoatsMap.get(
                    yachtEventData.getSubjectId().intValue()
                )
            );
        }
    }

    private void formatAndSendChatMessage(String rawChat) {
        if (rawChat.length() > 0) {
            socketThread.sendChatterMessage(
                new SimpleDateFormat("[HH:mm:ss] ").format(new Date()) +
                    allBoatsMap.get(socketThread.getClientId()).getShortName() + ": " + rawChat
            );
        }
    }


    public ClientToServerThread getSocketThread() {
        return socketThread;
    }

}
