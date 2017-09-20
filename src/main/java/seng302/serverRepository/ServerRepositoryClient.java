package seng302.serverRepository;

import com.sun.xml.internal.ws.api.message.Packet;
import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.ServerRegistrationMessage;
import seng302.model.stream.packets.PacketType;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ServerRepositoryClient {

    private String roomCode = "0";

    public ServerRepositoryClient() throws Exception {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000);
    }

    private void sendUpdate() throws Exception {
        Socket socket = new Socket("localhost", 9999);
        ServerRepoStreamParser parser = new ServerRepoStreamParser(socket.getInputStream());
        Message req = new ServerRegistrationMessage("asdf", "Asdf", "asdf", 6969, 1, 20, "4949");

        socket.getOutputStream().write(req.getBuffer());

        PacketType packetType = parser.parse();

        if (packetType != PacketType.ROOM_CODE_REQUEST){
            return;
        }

        String roomCode = parser.getRoomCode();

        if (roomCode.equals("0")){
            return;
        }

        this.roomCode = roomCode;

        socket.close();
    }
}

