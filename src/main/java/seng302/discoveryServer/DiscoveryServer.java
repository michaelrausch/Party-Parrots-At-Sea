package seng302.discoveryServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.RoomCodeRequest;
import seng302.gameServer.messages.ServerRegistrationMessage;
import seng302.model.stream.packets.PacketType;
import seng302.discoveryServer.util.ServerListing;
import seng302.discoveryServer.util.ServerRepoStreamParser;
import seng302.discoveryServer.util.ServerTable;
import seng302.visualiser.ServerListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;

public class DiscoveryServer {
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    private static final int MAX_SERVER_TRIES = 10;
    public static String DISCOVERY_SERVER = "party.sydney.srv.michaelrausch.nz";

    private ServerTable serverTable;
    public static final Integer PORT_NUMBER = 9969;
    private ServerSocket serverSocket;

    private final Logger logger = LoggerFactory.getLogger(DiscoveryServer.class);

    private void displayHeader(){
        String selectedColor = Arrays.asList(ANSI_BLUE, ANSI_GREEN, ANSI_YELLOW).get(new Random().nextInt(2));
        System.out.println(selectedColor);
        System.out.println("                              .ccccc.             \n" +
                "                         .cc;'coooxkl;.           \n" +
                "                     .:c:::c:,,,,,;c;;,.'.        \n" +
                "                   .clc,',:,..:xxocc;'..c;        \n" +
                "                  .c:,';:ox:..:c,,,,,,...cd,      \n" +
                "                .c:'.,oxxxxl::l:.,loll;..;ol.     \n" +
                "                ;Oc..:xxxxxxxxx:.,llll,....oc     \n" +
                "             .,;,',:loxxxxxxxxx:.,llll;.,,.'ld,   \n" +
                "            .lo;..:xxxxxxxxxxxx:.'cllc,.:l:'cO;   \n" +
                "           .:;...'cxxxxxxxxxxxxoc;,::,..cdl;;l'   \n" +
                "         .cl;':,'';oxxxxxxdxxxxxx:....,cooc,cO;   \n" +
                "     .,,,::;,lxoc:,,:lxxxxxxxxxxxo:,,;lxxl;'oNc   \n" +
                "   .cdxo;':lxxxxxxc'';cccccoxxxxxxxxxxxxo,.;lc.  " + ANSI_YELLOW + "Party-Parrots-At-Sea Discovery Server v1.0.0 (Release) " + selectedColor +"\n" +
                "  .loc'.'lxxxxxxxxocc;''''';ccoxxxxxxxxx:..oc     \n" +
                "olc,..',:cccccccccccc:;;;;;;;;:ccccccccc,.'c,     \n" +
                "Ol;......................................;l'  ");
        System.out.println(ANSI_RESET);
    }

    public DiscoveryServer() throws Exception {
        displayHeader();
        serverTable = new ServerTable();

        try{
            serverSocket = new ServerSocket(PORT_NUMBER);
        }
        catch(java.net.BindException e){
            logger.error("FATAL - Could not bind socket, are you sure there isn't already an instance running?");
            System.exit(1);
            return;
        }

        logger.info("Started successfully - Now accepting connections");

        try{
            while (true){
                Socket clientSocket = serverSocket.accept();

                parseRequest(clientSocket);

                clientSocket.close();
            }
        }
        catch (Exception e){
            close();
        }
    }


    private void parseRequest(Socket clientSocket) throws Exception {
        ServerRepoStreamParser parser = new ServerRepoStreamParser(clientSocket.getInputStream());

        if (clientSocket.isConnected() && !clientSocket.isClosed()){
            PacketType parsePacketResult = parser.parse();

            switch (parsePacketResult){
                case SERVER_REGISTRATION:
                    ServerListing listing = parser.getServerListing();

                    if (!serverTable.getAllServers().contains(listing)){
                        listing.setRoomCode(serverTable.getNextRoomCode().toString());
                    }

                    serverTable.addServer(listing);

                    Message serverRegMessage = new RoomCodeRequest(listing.getRoomCode());
                    clientSocket.getOutputStream().write(serverRegMessage.getBuffer());
                    break;

                case ROOM_CODE_REQUEST:
                    String desiredRoomCode = parser.getRoomCode();
                    ServerListing serverListing;

                    if (desiredRoomCode.equals("0000")){
                        serverListing = getRandomFreeServer();
                    }
                    else {
                        serverListing = serverTable.getServerByRoomCode(desiredRoomCode);
                    }

                    Message response;

                    if (serverListing != null){
                        response = new ServerRegistrationMessage(serverListing.getServerName(), serverListing.getMapName(), serverListing.getAddress(), serverListing.getPortNumber(), 0, 0, desiredRoomCode);
                    }
                    else{
                        response = ServerRegistrationMessage.getEmptyRegistration();
                    }

                    clientSocket.getOutputStream().write(response.getBuffer());
                    break;
            }
        }
    }

    public ServerListing getRandomFreeServer() {
        ServerListing serverToJoin;

        List<ServerListing> servers = serverTable.getAllServers();

        if (servers.size() <= 0){
            return null;
        }

        if (servers.size() == 1){
            return servers.get(0);
        }

        serverToJoin = servers.get(new Random().nextInt(servers.size()));

        int tries = 0;

        while (serverToJoin != null && serverToJoin.isMaxPlayersReached() && tries < MAX_SERVER_TRIES){
            serverToJoin = servers.get(new Random().nextInt(servers.size()));
            tries++;
        }

        if (serverToJoin != null && serverToJoin.isMaxPlayersReached()){
            return null;
        }

        return serverToJoin;
    }

    public void close(){
        try {
            serverSocket.close();
        } catch (IOException ignored) {
            ;
        }
    }
}
