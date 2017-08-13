package seng302.gameServer;

import java.util.Arrays;
import seng302.model.stream.packets.StreamPacket;
import seng302.gameServer.server.messages.BoatAction;


public class ServerPacketParser {


    public static BoatAction extractBoatAction(StreamPacket packet) {
        byte[] payload = packet.getPayload();
        int messageVersionNo = payload[0];
        long actionTypeValue = bytesToLong(Arrays.copyOfRange(payload, 0, 1));
        return BoatAction.getType((int) actionTypeValue);
    }

    /**
     * takes an array of up to 7 bytes and returns a positive
     * long constructed from the input bytes
     *
     * @return a positive long if there is less than 7 bytes -1 otherwise
     */
    private static long bytesToLong(byte[] bytes) {
        long partialLong = 0;
        int index = 0;
        for (byte b : bytes) {
            if (index > 6) {
                return -1;
            }
            partialLong = partialLong | (b & 0xFFL) << (index * 8);
            index++;
        }
        return partialLong;
    }
}

