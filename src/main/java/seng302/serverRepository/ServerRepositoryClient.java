package seng302.serverRepository;

import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.RoomCodeRequest;
import seng302.gameServer.messages.ServerRegistrationMessage;
import seng302.model.stream.packets.PacketType;

import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ServerRepositoryClient {

    private static String roomCode = null;
    private Timer serverListingUpdateTimer;

    public ServerRepositoryClient() {

    }

    public void register(ServerListing serverListing){
        serverListingUpdateTimer = new Timer();

        serverListingUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendRegistrationUpdate(serverListing);
                } catch (Exception e) {
                    e.printStackTrace();//todo proper error handling
                }
            }
        }, 0, 5000);
    }

    public void unregister(){
        serverListingUpdateTimer.cancel();
    }

    public ServerListing getServerForRoomCode(String roomCode) throws Exception {
        // TODO replace localhost with server
        Socket socket = new Socket("localhost", 9999);
        ServerRepoStreamParser parser = new ServerRepoStreamParser(socket.getInputStream());

        Message request = new RoomCodeRequest(roomCode); //roomCode);
        socket.getOutputStream().write(request.getBuffer());

        PacketType packetType = parser.parse();

        if (packetType != PacketType.SERVER_REGISTRATION){
            System.out.println("Wrong packet type");
            return null;
        }

        socket.close();

        return parser.getServerListing();
    }

    private void sendRegistrationUpdate(ServerListing serverListing) throws Exception {
        Socket socket = new Socket("localhost", 9999);
        ServerRepoStreamParser parser = new ServerRepoStreamParser(socket.getInputStream());

        Message req = new ServerRegistrationMessage(serverListing);

        socket.getOutputStream().write(req.getBuffer());

        PacketType packetType = parser.parse();

        if (packetType != PacketType.ROOM_CODE_REQUEST){
            return;
        }

        String roomCode = parser.getRoomCode();

        if (roomCode.length() != 0){
            ServerRepositoryClient.roomCode = roomCode;
        }

        socket.close();
    }

    public static String getRoomCode(){
        return roomCode;
    }
}

