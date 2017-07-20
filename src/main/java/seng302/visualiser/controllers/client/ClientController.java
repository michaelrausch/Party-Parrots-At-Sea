package seng302.visualiser.controllers.client;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import seng302.model.stream.XMLParser;
import seng302.model.stream.packets.StreamPacket;
import seng302.server.messages.BoatActionMessage;
import seng302.server.messages.BoatActionType;
import seng302.visualiser.ClientToServerThread;

/**
 * Created by cir27 on 20/07/17.
 */
public class ClientController {

    Pane holderPane;
    ClientToServerThread socketThread;

    public ClientController (String ipAddress, Pane holder) {
        this.holderPane = holder;
        socketThread = new ClientToServerThread(ipAddress, 4950);
        socketThread.start();
        socketThread.waitForXML(event -> storeXMLData());
    }

    private void parsePacket(StreamPacket packet) {
        try {
            switch (packet.getType()) {
                case HEARTBEAT:
                    extractHeartBeat(packet);
                    break;
                case RACE_STATUS:
                    extractRaceStatus(packet);
                    break;
                case DISPLAY_TEXT_MESSAGE:
                    extractDisplayMessage(packet);
                    break;
                case XML_MESSAGE:
                    newRaceXmlReceived = true;
                    extractXmlMessage(packet);
                    break;
                case RACE_START_STATUS:
                    extractRaceStartStatus(packet);
                    break;
                case YACHT_EVENT_CODE:
                    extractYachtEventCode(packet);
                    break;
                case YACHT_ACTION_CODE:
                    extractYachtActionCode(packet);
                    break;
                case CHATTER_TEXT:
                    extractChatterText(packet);
                    break;
                case BOAT_LOCATION:
                    extractBoatLocation(packet);
                    break;
                case MARK_ROUNDING:
                    extractMarkRounding(packet);
                    break;
                case COURSE_WIND:
                    extractCourseWind(packet);
                    break;
                case AVG_WIND:
                    extractAvgWind(packet);
                    break;
                case BOAT_ACTION:
                    extractBoatAction(packet);
                    break;
            }
        } catch (NullPointerException e) {
            System.out.println("Error parsing packet");
            e.printStackTrace();
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
