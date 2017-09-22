package seng302.gameServer;

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
import seng302.model.token.Token;
import seng302.model.token.TokenType;
import seng302.utilities.XMLGenerator;

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
        List<ServerYacht> yachts = new ArrayList<>(GameState.getYachts().values());
        List<Token> tokens = GameState.getTokensInPlay();
        RaceXMLTemplate raceXMLTemplate = new RaceXMLTemplate(yachts, tokens);
        xmlGenerator.setRaceTemplate(raceXMLTemplate);

        XMLMessage raceXMLMessage = new XMLMessage(
            xmlGenerator.getRaceAsXml(),
            XMLMessageSubType.RACE,
            xmlGenerator.getRaceAsXml().length());

        return raceXMLMessage;
    }

    public static XMLMessage getRegattaXML() {
        //@TODO calculate lat/lng values

        return new XMLMessage(
            xmlGenerator.getRegattaAsXml(),
            XMLMessageSubType.REGATTA,
            xmlGenerator.getRegattaAsXml().length());
    }

    public static XMLMessage getBoatXML() {
        List<ServerYacht> yachts = new ArrayList<>(GameState.getYachts().values());
        List<Token> tokens = GameState.getTokensInPlay();
        RaceXMLTemplate raceXMLTemplate = new RaceXMLTemplate(yachts, tokens);
        xmlGenerator.setRaceTemplate(raceXMLTemplate);

        return new XMLMessage(
            xmlGenerator.getBoatsAsXml(),
            XMLMessageSubType.BOAT,
            xmlGenerator.getBoatsAsXml().length());
    }

    public static YachtEventCodeMessage makeCollisionMessage(ServerYacht serverYacht) {
        return new YachtEventCodeMessage(serverYacht.getSourceId(), YachtEventType.COLLISION);
    }

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

    public static ChatterMessage makeChatterMessage(Integer messageType, String message) {
        return new ChatterMessage(messageType, "SERVER: " + message);
    }
}
