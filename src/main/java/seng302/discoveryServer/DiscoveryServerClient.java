package seng302.discoveryServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.discoveryServer.util.ServerListing;
import seng302.discoveryServer.util.ServerRepoStreamParser;
import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.RoomCodeRequest;
import seng302.gameServer.messages.ServerRegistrationMessage;
import seng302.model.stream.packets.PacketType;
import seng302.visualiser.controllers.ViewManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class DiscoveryServerClient {
    private final Integer UPDATE_INTERVAL_MS = 1000;

    private static String roomCode = null;
    private Timer serverListingUpdateTimer;
    private Logger logger = LoggerFactory.getLogger(DiscoveryServerClient.class);
    private String ip = "";
    private Boolean isInInvalidState = false;

    public DiscoveryServerClient() {
        try {
            ip = getInetIpAddr();
        } catch (Exception e) {
            failError();
        }
    }

    public String getInetIp(){
        return ip;
    }

    private void failError() {
        isInInvalidState = true;
        ViewManager.getInstance().showErrorSnackBar("You do not appear to be able to connect to the internet. Matchmaking will be unavailable.");
    }

    public boolean didFail(){
        return isInInvalidState;
    }

    /**
     * Register the server with the discovery server
     * @param serverListing The listing to register
     */
    public void register(ServerListing serverListing){
        if (isInInvalidState) return;

        if (serverListingUpdateTimer != null){
            serverListingUpdateTimer.cancel();
            serverListingUpdateTimer = null;
        }

        serverListingUpdateTimer = new Timer();

        serverListingUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendRegistrationUpdate(serverListing);
                } catch (Exception e) {
                    logger.debug("Could not update server listing");
                }
            }
        }, 0, UPDATE_INTERVAL_MS);
    }

    /**
     * Stop updating the server registration updates
     */
    public void unregister(){
        if (serverListingUpdateTimer != null)
            serverListingUpdateTimer.cancel();
    }

    /**
     * Gets the connection information for a server given a room code
     *
     * @param roomCode The room code to search for
     * @return The ServerListing, or null if there was an error
     * @throws Exception .
     */
    public ServerListing getServerForRoomCode(String roomCode) throws Exception {
        Socket socket = new Socket(DiscoveryServer.DISCOVERY_SERVER, DiscoveryServer.PORT_NUMBER);
        ServerRepoStreamParser parser = new ServerRepoStreamParser(socket.getInputStream());

        Message request = new RoomCodeRequest(roomCode); //roomCode);
        socket.getOutputStream().write(request.getBuffer());

        PacketType packetType = parser.parse();

        if (packetType != PacketType.SERVER_REGISTRATION){
            logger.debug("Wrong packet received in response to a room code request");
            return null;
        }

        socket.close();

        return parser.getServerListing();
    }

    public ServerListing getRandomServer() throws Exception {
        Socket socket = new Socket(DiscoveryServer.DISCOVERY_SERVER, DiscoveryServer.PORT_NUMBER);
        ServerRepoStreamParser parser = new ServerRepoStreamParser(socket.getInputStream());

        Message request = new RoomCodeRequest("0000");
        socket.getOutputStream().write(request.getBuffer());

        PacketType packetType = parser.parse();

        if (packetType != PacketType.SERVER_REGISTRATION){
            logger.error("Incorrect packet type received");
            return null;
        }

        socket.close();

        ServerListing serverListing = parser.getServerListing();

        if (serverListing == null || serverListing.equals(ServerRegistrationMessage.getEmptyRegistration())){
            return null;
        }

        return serverListing;
    }

    /**
     * Sends a registration update to the discovery server.
     *
     * @param serverListing The server listing to send
     * @throws Exception IF there was an error sending the update
     */
    private void sendRegistrationUpdate(ServerListing serverListing) throws Exception {
        Socket socket = new Socket(DiscoveryServer.DISCOVERY_SERVER, DiscoveryServer.PORT_NUMBER);
        ServerRepoStreamParser parser = new ServerRepoStreamParser(socket.getInputStream());

        Message req = new ServerRegistrationMessage(serverListing);

        socket.getOutputStream().write(req.getBuffer());

        PacketType packetType = parser.parse();

        if (packetType != PacketType.ROOM_CODE_REQUEST){
            socket.close();
            return;
        }

        String roomCode = parser.getRoomCode();

        if (roomCode.length() != 0){
            DiscoveryServerClient.roomCode = roomCode;
        }

        socket.close();
    }

    /**
     * @return The last room code received by the client
     */
    public static String getRoomCode(){
        return roomCode;
    }

    public static String getInetIpAddr() throws Exception {
        URL myIp = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    myIp.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}

