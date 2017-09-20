package seng302.serverRepository;

import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.RoomCodeRequest;
import seng302.gameServer.messages.ServerRegistrationMessage;
import seng302.model.stream.packets.PacketType;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ServerRepository {
    private ServerTable serverTable;

    public ServerRepository() throws Exception {
        System.out.println(" -- Starting Server Repository -- ");
        serverTable = new ServerTable();

        ServerSocket serverSocket = new ServerSocket(9999);


        // TODO Remove later, this is for testing
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    new ServerRepositoryClient();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000);

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
