package seng302.visualiser.controllers.client;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import seng302.model.Boat;
import seng302.model.RaceState;
import seng302.model.mark.Mark;
import seng302.model.stream.parsers.PositionUpdateData.DeviceType;
import seng302.model.stream.parsers.MarkRoundingData;
import seng302.model.stream.parsers.RaceStatusData;
import seng302.model.stream.parsers.xml.RaceXMLData;
import seng302.model.stream.parsers.StreamParser;
import seng302.model.stream.parsers.xml.RegattaXMLData;
import seng302.model.stream.parsers.xml.XMLParser;
import seng302.model.stream.parsers.PositionUpdateData;
import seng302.model.stream.packets.StreamPacket;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.controllers.RaceViewController;

/**
 * Created by cir27 on 20/07/17.
 */
public class ClientController {

    private Pane holderPane;
    private ClientToServerThread socketThread;

    private RaceViewController raceView;

    private Map<Integer, Boat> allBoatsMap;
    private Map<Integer, Boat> racingBoats = new HashMap<>();
    private RegattaXMLData regattaData;
    private RaceXMLData courseData;
    private RaceState raceState = new RaceState();

    public ClientController (String ipAddress, Pane holder) {
        this.holderPane = holder;
        socketThread = new ClientToServerThread(ipAddress, 4950);
        socketThread.start();
        socketThread.addStreamObserver(this::parsePacket);
    }

    private void loadRaceView () {
        allBoatsMap.forEach((id, boat) -> {
            if (courseData.getParticipants().contains(id))
                racingBoats.put(id, boat);
        });
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RaceView.fxml"));
        raceView = fxmlLoader.getController();
        try {
            holderPane.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        raceView.loadRace(racingBoats, courseData, raceState);
    }

    private void parsePacket(StreamPacket packet) {
        switch (packet.getType()) {
            case RACE_STATUS:
                processRaceStatusUpdate(StreamParser.extractRaceStatus(packet));
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
                startRaceIfAllDataReceived();
                break;

            case RACE_XML:
                courseData = XMLParser.parseRace(
                    StreamParser.extractXmlMessage(packet)
                );
                if (raceView != null) {
                    raceView.updateRaceData(courseData);
                }
                startRaceIfAllDataReceived();
                break;

            case BOAT_XML:
                allBoatsMap = XMLParser.parseBoats(
                    StreamParser.extractXmlMessage(packet)
                );
                startRaceIfAllDataReceived();
                break;

            case RACE_START_STATUS:
                raceState.updateState(StreamParser.extractRaceStartStatus(packet));
                break;

            case BOAT_LOCATION:
                updatePosition(StreamParser.extractBoatLocation(packet));
                break;

            case MARK_ROUNDING:
                updateMarkRounding(StreamParser.extractMarkRounding(packet));
                break;
        }
    }

    private void startRaceIfAllDataReceived() {
        if (courseData != null && allBoatsMap != null && regattaData != null)
            loadRaceView();
    }

    /**
     * Updates the position of a boat. Boat and position are given in the provided data.
     * @param positionData
     */
    private void updatePosition(PositionUpdateData positionData) {
        if (positionData.getType() == DeviceType.YACHT_TYPE) {
            Boat boat = racingBoats.get(positionData.getDeviceId());
            boat.setVelocity(positionData.getGroundSpeed());
            boat.setLat(positionData.getLat());
            boat.setLon(positionData.getLon());
            boat.setHeading(positionData.getHeading());
        } else if (positionData.getType() == DeviceType.MARK_TYPE) {
            Mark mark = courseData.getCompoundMarks().get(positionData.getDeviceId());
        }
    }

    /**
     * Updates the boat as having passed the mark. Boat and mark are given by the ids in the
     * provided data.
     * @param roundingData Contains data for the rounding of a mark.
     */
    private void updateMarkRounding(MarkRoundingData roundingData) {
        Boat boat = racingBoats.get(roundingData.getBoatId());
        boat.setMarkRoundingTime(roundingData.getTimeStamp());
        boat.setTimeSinceLastMark(raceState.getRaceTime() - roundingData.getTimeStamp());
        boat.setLastMarkRounded(
            courseData.getCompoundMarks().get(
                roundingData.getMarkId()
            )
        );
    }

    private void processRaceStatusUpdate (RaceStatusData data) {
        raceState.updateState(data);
        for (long[] boatData : data.getBoatData()) {
            Boat boat = allBoatsMap.get((int) boatData[0]);
            boat.setEstimateTimeTillNextMark(raceState.getRaceTime() - boatData[1]);
            boat.setEstimateTimeAtFinish(boatData[2]);
            int legNumber = (int) boatData[3];
            boat.setLegNumber(legNumber);
            if (legNumber != boat.getLegNumber()) {
                int placing = 1;
                for (Boat otherBoat : allBoatsMap.values()) {
                    if (otherBoat.getSourceID() != boatData[0] &&
                        boat.getLegNumber() <= otherBoat.getLegNumber())
                        placing++;
                }
                boat.setPosition(placing);
            }
        }
    }

    private void close () {
        socketThread.closeSocket();
    }

//    /** Handle the key-pressed event from the text field. */
//    public void keyPressed(KeyEvent e) {
//        BoatActionMessage boatActionMessage;
//        switch (e.getCode()){
//            case SPACE: // align with vmg
//                boatActionMessage = new BoatActionMessage(BoatActionType.VMG);
//                clientToServerThread.sendBoatActionMessage(boatActionMessage);
//                break;
//            case PAGE_UP: // upwind
//                boatActionMessage = new BoatActionMessage(BoatActionType.UPWIND);
//                clientToServerThread.sendBoatActionMessage(boatActionMessage);
//                break;
//            case PAGE_DOWN: // downwind
//                boatActionMessage = new BoatActionMessage(BoatActionType.DOWNWIND);
//                clientToServerThread.sendBoatActionMessage(boatActionMessage);
//                break;
//            case ENTER: // tack/gybe
//                boatActionMessage = new BoatActionMessage(BoatActionType.TACK_GYBE);
//                clientToServerThread.sendBoatActionMessage(boatActionMessage);
//                break;
//            //TODO Allow a zoom in and zoom out methods
//            case Z:  // zoom in
//                System.out.println("Zoom in");
//                break;
//            case X:  // zoom out
//                System.out.println("Zoom out");
//                break;
//        }
//    }

//    public void keyReleased(KeyEvent e) {
//        switch (e.getCode()) {
//            //TODO 12/07/17 Determine the sail state and send the appropriate packet (eg. if sails are in, send a sail out packet)
//            case SHIFT:  // sails in/sails out
//                BoatActionMessage boatActionMessage = new BoatActionMessage(BoatActionType.SAILS_IN);
//                clientToServerThread.sendBoatActionMessage(boatActionMessage);
//                break;
//        }
//    }
//
//    onKeyPressed="#keyPressed" onKeyReleased="#keyReleased"
}
