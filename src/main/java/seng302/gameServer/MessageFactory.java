package seng302.gameServer;

import seng302.gameServer.messages.*;
import java.util.ArrayList;
import java.util.List;
import seng302.gameServer.messages.BoatLocationMessage;
import seng302.gameServer.messages.BoatSubMessage;
import seng302.gameServer.messages.ChatterMessage;
import seng302.gameServer.messages.RaceStartNotificationType;
import seng302.gameServer.messages.RaceStartStatusMessage;
import seng302.gameServer.messages.RaceStatus;
import seng302.gameServer.messages.RaceStatusMessage;
import seng302.gameServer.messages.RaceType;
import seng302.gameServer.messages.XMLMessage;
import seng302.gameServer.messages.XMLMessageSubType;
import seng302.gameServer.messages.YachtEventCodeMessage;
import seng302.gameServer.messages.YachtEventType;
import seng302.model.Player;
import seng302.model.ServerYacht;
import seng302.model.stream.xml.generator.RaceXMLTemplate;
import seng302.model.stream.xml.generator.RegattaXMLTemplate;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.model.token.Token;
import seng302.utilities.XMLGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * A Class for interfacing between the data we have in the GameState to the messages we need to send
 * through the MainServerThread.
 *
 * WARNING DO NOT USE THIS CLASS IF GAMESTATE HAS NOT BEEN INSTANTIATED. (Main Server has not started)
 * // TODO: 29/08/17 wmu16 - Make GameState non static to fix this ¯\_(ツ)_/¯
 * Created by wmu16 on 29/08/17.
 */

/*
Ideally this class would be created with an instance of the GameState (I tried implementing this for
 a bit) but it was too difficult to properly make GameState non static without doing some proper
 re working. To do later.
 */
public class MessageFactory {

    private static XMLGenerator xmlGenerator = new XMLGenerator();
    private static XMLMessage race;
    private static XMLMessage regatta;
    private static XMLMessage boats;

    public static void updateXMLGenerator(RaceXMLData race, RegattaXMLData regatta) {
        xmlGenerator.setRegattaTemplate(
            new RegattaXMLTemplate(
                regatta.getRegattaName(),
                regatta.getCourseName(),
                regatta.getCentralLat(),
                regatta.getCentralLng()
            )
        );
        xmlGenerator.setRaceTemplate(
            new RaceXMLTemplate(
                new ArrayList<>(),
                new ArrayList<>(),
                race.getMarkSequence(),
                race.getCourseLimit(),
                new ArrayList<>(race.getCompoundMarks().values())
            )
        );
        String xmlStr = xmlGenerator.getRaceAsXml();
        MessageFactory.race = new XMLMessage(xmlStr, XMLMessageSubType.RACE, xmlStr.length());
        xmlStr = xmlGenerator.getRegattaAsXml();
        MessageFactory.regatta = new XMLMessage(xmlStr, XMLMessageSubType.REGATTA, xmlStr.length());
        xmlStr = xmlGenerator.getBoatsAsXml();
        MessageFactory.boats = new XMLMessage(xmlStr, XMLMessageSubType.BOAT, xmlStr.length());
    }

    public static void updateBoats(List<ServerYacht> yachts) {
        xmlGenerator.getRace().setBoats(yachts);
        String xmlStr = xmlGenerator.getBoatsAsXml();
        MessageFactory.boats = new XMLMessage(xmlStr, XMLMessageSubType.BOAT, xmlStr.length());
    }

    public static void updateTokens(List<Token> tokens) {
        xmlGenerator.getRace().setTokens(tokens);
        String xmlStr = xmlGenerator.getRaceAsXml();
        MessageFactory.race = new XMLMessage(xmlStr, XMLMessageSubType.RACE, xmlStr.length());
    }


    public static RaceStartStatusMessage getRaceStartStatusMessage() {
        return new RaceStartStatusMessage(
            1,
            GameState.getStartTime(),
            1,
            RaceStartNotificationType.SET_RACE_START_TIME);
    }

    public static RaceStatusMessage getRaceStatusMessage() {
        // variables taken from GameServerThread

        List<BoatSubMessage> boatSubMessages = new ArrayList<>();
        RaceStatus raceStatus;

        for (Player player : GameState.getPlayers()) {
            ServerYacht y = player.getYacht();
            BoatSubMessage m = new BoatSubMessage(y.getSourceId(), y.getBoatStatus(),
                y.getLegNumber(),
                0, 0, 1234L,
                1234L);
            boatSubMessages.add(m);
        }

        long timeTillStart = System.currentTimeMillis() - GameState.getStartTime();

        if (GameState.getCurrentStage() == GameStages.LOBBYING) {
            raceStatus = RaceStatus.PRESTART;
        } else if (GameState.getCurrentStage() == GameStages.PRE_RACE) {
            raceStatus = RaceStatus.PRESTART;

            if (timeTillStart > GameState.WARNING_TIME) {
                raceStatus = RaceStatus.WARNING;
            }

            if (timeTillStart > GameState.PREPATORY_TIME) {
                raceStatus = RaceStatus.PREPARATORY;
            }
        } else {
            raceStatus = RaceStatus.STARTED;
        }

        return new RaceStatusMessage(1, raceStatus, GameState.getStartTime(),
            GameState.getWindDirection(),
            GameState.getWindSpeedMMS().longValue(), GameState.getPlayers().size(),
            RaceType.MATCH_RACE, 1, boatSubMessages);
    }

    public static BoatLocationMessage getBoatLocationMessage(ServerYacht yacht) {
        return new BoatLocationMessage(
            yacht.getSourceId(),
            0,  // TODO: 29/08/17 wmu16 - Work out what to do with seqNo. Currently not used
            yacht.getLocation().getLat(),
            yacht.getLocation().getLng(),
            yacht.getHeading(),
            yacht.getCurrentVelocity().longValue());
    }

    public static XMLMessage getRaceXML() {
        return race;
    }

    public static XMLMessage getRegattaXML() {
       return regatta;
    }

    public static XMLMessage getBoatXML() {
        return boats;
    }

    public static YachtEventCodeMessage makeCollisionMessage(ServerYacht serverYacht) {
        return new YachtEventCodeMessage(serverYacht.getSourceId(), YachtEventType.COLLISION);
    }


    /**
     * Constructs a message to be sent out whenever a yacht picks up a boost
     *
     * @param serverYacht The yacht that has picked up a power up
     * @param token The token which they picked up
     * @return The corresponding YachtEventCodeMessage
     */
    public static YachtEventCodeMessage makePickupMessage(ServerYacht serverYacht, Token token) {
        YachtEventType yachtEventType = null;
        switch (token.getTokenType()) {
            case BOOST:
                yachtEventType = YachtEventType.TOKEN_VELOCITY;
                break;
            case HANDLING:
                yachtEventType = YachtEventType.TOKEN_HANDLING;
                break;
            case WIND_WALKER:
                yachtEventType = YachtEventType.TOKEN_WIND_WALKER;
                break;
            case BUMPER:
                yachtEventType = YachtEventType.TOKEN_BUMPER;
                break;
            case RANDOM:
                yachtEventType = YachtEventType.TOKEN_RANDOM;
                break;
        }
        return new YachtEventCodeMessage(serverYacht.getSourceId(), yachtEventType);
    }

    /**
     * Constructs a message representing a certain buff / debuff for a given yacht. For now this is
     * just for the bumper debuff so the affected boat is aware that it has been crashed. This could
     * however be extended to render affects for all boats given a certain debuff.
     *
     * @param yacht The yacht affected by some status
     * @param token The token indicating what status they have
     * @return A YachtEventCodeMessage
     */
    public static YachtEventCodeMessage makeStatusEffectMessage(ServerYacht yacht,
        TokenType token) {
        YachtEventType yachtEventType = null;
        switch (token) {
            case BUMPER:
                yachtEventType = YachtEventType.BUMPER_CRASH;
                break;
        }
        return new YachtEventCodeMessage(yacht.getSourceId(), yachtEventType);
    }


    /**
     * Constructs a message to be sent out when a given yacht powers down (From a boost of any type)
     *
     * @param yacht The yacht that is powering down
     * @return A YachtEventCodeMessage representing this action
     */
    public static YachtEventCodeMessage makePowerDownMessage(ServerYacht yacht) {
        return new YachtEventCodeMessage(yacht.getSourceId(), YachtEventType.POWER_DOWN);
    }

    public static ChatterMessage makeChatterMessage(Integer messageType, String message) {
        return new ChatterMessage(messageType, "SERVER: " + message);
    }
}
