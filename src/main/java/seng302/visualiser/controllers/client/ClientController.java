package seng302.visualiser.controllers.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import seng302.model.Boat;
import seng302.model.mark.Mark;
import seng302.model.stream.parsers.PositionUpdateData.DeviceType;
import seng302.model.stream.parsers.MarkRoundingData;
import seng302.model.stream.parsers.RaceStartData;
import seng302.model.stream.parsers.RaceStatusData;
import seng302.model.stream.parsers.xml.RaceXMLData;
import seng302.model.stream.parsers.StreamParser;
import seng302.model.stream.parsers.xml.RegattaXMLData;
import seng302.model.stream.parsers.xml.XMLParser;
import seng302.model.stream.parsers.PositionUpdateData;
import seng302.model.stream.packets.StreamPacket;
import seng302.visualiser.ClientSocketListener;
import seng302.visualiser.ClientToServerThread;
import seng302.visualiser.controllers.RaceViewController;

/**
 * Created by cir27 on 20/07/17.
 */
public class ClientController {

    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private Pane holderPane;
    private ClientToServerThread socketThread;
    private ClientSocketListener socketListener;

    private RaceViewController raceView;

    private Map<Integer, Boat> allBoatsMap;
    private Map<Integer, Boat> racingBoats = new HashMap<>();
    private RegattaXMLData regattaData;
    private RaceXMLData raceData;

    public ClientController (String ipAddress, Pane holder) {
        this.holderPane = holder;
        socketThread = new ClientToServerThread(ipAddress, 4950);
        socketThread.start();
        socketListener = this::parsePacket;
        socketThread.addStreamObserver(socketListener);
    }

    private void loadRaceView () {
        allBoatsMap.forEach((id, boat) -> {
            if (raceData.getParticipants().contains(id))
                racingBoats.put(id, boat);
        });
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RaceView.fxml"));
        raceView = fxmlLoader.getController();
        raceView.loadRace(racingBoats, raceData);
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
                DATE_TIME_FORMAT.setTimeZone(
                    TimeZone.getTimeZone(
                        ZoneId.ofOffset("UTC", ZoneOffset.ofHours(regattaData.getUtcOffset()))
                    )
                );
                startRaceIfAllDataRecieved();
                break;

            case RACE_XML:
                raceData = XMLParser.parseRace(
                    StreamParser.extractXmlMessage(packet)
                );
                if (raceView != null) {
                    raceView.updateRaceData(raceData);
                }
                startRaceIfAllDataRecieved();
                break;

            case BOAT_XML:
                allBoatsMap = XMLParser.parseBoats(
                    StreamParser.extractXmlMessage(packet)
                );
                startRaceIfAllDataRecieved();
                break;

            case RACE_START_STATUS:
                RaceStartData raceStartData = StreamParser.extractRaceStartStatus(packet);
                break;

            case BOAT_LOCATION:
                PositionUpdateData positionData = StreamParser.extractBoatLocation(packet);
                updatePosition(positionData);
                break;

            case MARK_ROUNDING:
                MarkRoundingData roundingData = StreamParser.extractMarkRounding(packet);
                updateMarkRounding(roundingData);
                break;
        }
    }

    private void startRaceIfAllDataRecieved () {
        if (raceData != null && allBoatsMap != null && regattaData != null)
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
        } else {
            Mark mark = raceData.getCompoundMarks().get(positionData.getDeviceId());
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
        boat.setLastMarkRounded(
            raceData.getCompoundMarks().get(
                roundingData.getMarkId()
            )
        );
    }

    private void processRaceStatusUpdate (RaceStatusData data) {
        String raceTimeStr = DATE_TIME_FORMAT.format(data.getCurrentTime());
        Date date = new Date();
        date.getTime();
        long timeTillStart = (data.getExpectedStartTime() - data.getCurrentTime()) / 1000;
        for (long[] boatData : data.getBoatData()) {
            Boat boat = allBoatsMap.get((int) boatData[0]);
            boat.setEstimateTimeAtNextMark(boatData[1]);
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
//
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
