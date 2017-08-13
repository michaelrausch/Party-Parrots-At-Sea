package seng302.gameServer;

import java.util.Arrays;

import seng302.gameServer.server.messages.ClientType;
import seng302.gameServer.server.messages.Message;
import seng302.model.stream.packets.StreamPacket;
import seng302.gameServer.server.messages.BoatAction;


public class ServerPacketParser {

    public static BoatAction extractBoatAction(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long actionTypeValue = Message.bytesToLong(Arrays.copyOfRange(payload, 0, 1));
        return BoatAction.getType((int) actionTypeValue);
    }

    public static ClientType extractClientType(StreamPacket packet){
        byte[] payload = packet.getPayload();
        long value = Message.bytesToLong(Arrays.copyOfRange(payload, 0, 1));
        return ClientType.getClientType((int) value);
    }
}

