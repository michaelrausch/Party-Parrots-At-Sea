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

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;

public class DiscoveryServer {
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static String DISCOVERY_SERVER = "party.sydney.srv.michaelrausch.nz";

    private ServerTable serverTable;
    public static final Integer PORT_NUMBER = 9969;

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
                "   .cdxo;':lxxxxxxc'';cccccoxxxxxxxxxxxxo,.;lc.  " + ANSI_YELLOW + "Party-Parrots-At-Sea Discovery Server v0.1 " + selectedColor +"\n" +
                "  .loc'.'lxxxxxxxxocc;''''';ccoxxxxxxxxx:..oc     \n" +
                "olc,..',:cccccccccccc:;;;;;;;;:ccccccccc,.'c,     \n" +
                "Ol;......................................;l'  ");
        System.out.println(ANSI_RESET);
    }

    public DiscoveryServer() throws Exception {
        displayHeader();
        serverTable = new ServerTable();

        ServerSocket serverSocket;

        try{
            serverSocket = new ServerSocket(PORT_NUMBER);
        }
        catch(java.net.BindException e){
            logger.error("FATAL - Could not bind socket, are you sure there isn't already an instance running?");
            System.exit(1);
            return;
        }

        logger.info("Started successfully - Now accepting connections");

        while (true){
            Socket clientSocket = serverSocket.accept();

            parseRequest(clientSocket);

            clientSocket.close();
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

                    ServerListing serverListing = serverTable.getServerByRoomCode(desiredRoomCode);
                    Message response;

                    if (serverListing != null){
                        response = new ServerRegistrationMessage(serverListing.getServerName(), serverListing.getMapName(), serverListing.getAddress(), serverListing.getPortNumber(), 0, 0, desiredRoomCode);
                    }
                    else{
                        response = new ServerRegistrationMessage("", "", "", 0, 0, 0, "");
                    }

                    clientSocket.getOutputStream().write(response.getBuffer());
                    break;
            }
        }
    }
}
