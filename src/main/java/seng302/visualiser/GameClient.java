package seng302.visualiser;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TimeZone;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.messages.BoatAction;
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
            socketThread = new ClientToServerThread(ipAddress, portNumber);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Unable to connect to host...");
        }

        socketThread.addStreamObserver(this::parsePackets);
        LobbyController lobbyController = loadLobby();
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
    }

    /**
     * Connect to a game as the host at the given address and starts the visualiser.
     * @param ipAddress IP to connect to.
     * @param portNumber Port to connect to.
     */
    public void runAsHost(String ipAddress, Integer portNumber) {
        server = new MainServerThread();
        try {
            socketThread = new ClientToServerThread(ipAddress, portNumber);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Unable to make local connection to host...");
        }
        socketThread.addStreamObserver(this::parsePackets);
        LobbyController lobbyController = loadLobby();
        lobbyController.setPlayerListSource(clientLobbyList);

        if (regattaData != null){
            lobbyController.setTitle("Hosting: " + regattaData.getRegattaName());
            lobbyController.setCourseName(regattaData.getCourseName());
        }
        else{
            lobbyController.setTitle("Hosting: " + ipAddress);
            lobbyController.setCourseName("");
        }

        lobbyController.addCloseListener(exitCause -> {
            if (exitCause == CloseStatus.READY) {
                GameState.resetStartTime();
                lobbyController.disableReadyButton();
                server.startGame();
            } else if (exitCause == CloseStatus.LEAVE) {
                loadStartScreen();
            }
        });

        this.lobbyController = lobbyController;
        server.setGameClient(this);
    }

    private void loadStartScreen() {
        socketThread.setSocketToClose();
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
            e.printStackTrace();
        }
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
        LobbyController lobbyController = fxmlLoader.getController();
        lobbyController.setSocketThread(socketThread);
        lobbyController.setPlayerListSource(clientLobbyList);
        lobbyController.setPlayerID(socketThread.getClientId());

        return lobbyController;
    }

    private void loadRaceView() {
        FXMLLoader fxmlLoader = new FXMLLoader(
            RaceViewController.class.getResource("/views/RaceView.fxml"));
        try {
            final Node node = fxmlLoader.load();
            Platform.runLater(() -> {
                holderPane.getChildren().clear();
                holderPane.getChildren().add(node);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        holderPane.getScene().setOnKeyPressed(this::keyPressed);
        holderPane.getScene().setOnKeyReleased(this::keyReleased);
        raceView = fxmlLoader.getController();
        ClientYacht player = allBoatsMap.get(socketThread.getClientId());
        raceView.loadRace(allBoatsMap, courseData, raceState, player);
    }

    private void loadFinishScreenView() {
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource("/views/FinishScreenView.fxml"));
        try {
            final Node finishScreenFX = fxmlLoader.load();
            Platform.runLater(() -> {
                holderPane.getChildren().clear();
                holderPane.getChildren().add(finishScreenFX);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                ClientYacht clientYacht = allBoatsMap.get(positionData.getDeviceId());
                clientYacht.updateLocation(positionData.getLat(),
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
            clientYacht.setMarkRoundingTime(roundingData.getTimeStamp());
            clientYacht.updateTimeSinceLastMarkProperty(
                raceState.getRaceTime() - roundingData.getTimeStamp());
            clientYacht.setLastMarkRounded(
                courseData.getCompoundMarks().get(
                    roundingData.getMarkId()
                )
            );
        }
    }

    private void processRaceStatusUpdate(RaceStatusData data) {
        if (allXMLReceived()) {
            raceState.updateState(data);
            if (raceView != null) {
                raceView.getGameView().setWindDir(raceState.getWindDirection());
            }
            boolean raceFinished = true;
            for (ClientYacht yacht : allBoatsMap.values()) {
                if (yacht.getBoatStatus() != 3) {
                    raceFinished = false;
                }
            }
            if (raceFinished == true) {
                close();
                loadFinishScreenView();
            }

            for (long[] boatData : data.getBoatData()) {
                ClientYacht clientYacht = allBoatsMap.get((int) boatData[0]);
                clientYacht.setEstimateTimeTillNextMark(raceState.getRaceTime() - boatData[1]);
                clientYacht.setEstimateTimeAtFinish(boatData[2]);
                int legNumber = (int) boatData[3];
                clientYacht.setLegNumber(legNumber);
                clientYacht.setBoatStatus((int) boatData[4]);
                if (legNumber != clientYacht.getLegNumber()) {
                    int placing = 1;
                    for (ClientYacht otherClientYacht : allBoatsMap.values()) {
                        if (otherClientYacht.getSourceId() != boatData[0] &&
                            clientYacht.getLegNumber() <= otherClientYacht.getLegNumber())
                            placing++;
                    }
                    clientYacht.setPositionInteger(placing);
                }
            }
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
        switch (e.getCode()) {
            case SPACE: // align with vmg
                socketThread.sendBoatAction(BoatAction.VMG); break;
            case PAGE_UP: // upwind
                socketThread.sendBoatAction(BoatAction.UPWIND); break;
            case PAGE_DOWN: // downwind
                socketThread.sendBoatAction(BoatAction.DOWNWIND); break;
            case ENTER: // tack/gybe
                socketThread.sendBoatAction(BoatAction.TACK_GYBE); break;
            //TODO Allow a zoom in and zoom out methods
            case Z:  // zoom in
                System.out.println("Zoom in");
                break;
            case X:  // zoom out
                System.out.println("Zoom out");
                break;
        }
    }

    private void keyReleased(KeyEvent e) {
        switch (e.getCode()) {
            //TODO 12/07/17 Determine the sail state and send the appropriate packet (eg. if sails are in, send a sail out packet)
            case SHIFT:  // sails in/sails out
                socketThread.sendBoatAction(BoatAction.SAILS_IN);
                raceView.getGameView().getPlayerYacht().toggleSail();
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
            raceView.showCollision(yachtEventData.getSubjectId());
        }
    }
}
