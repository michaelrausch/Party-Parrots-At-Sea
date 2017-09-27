package seng302.serverDiscovery;

import org.junit.Test;
import seng302.gameServer.messages.Message;
import seng302.gameServer.messages.RoomCodeRequest;
import seng302.model.stream.packets.PacketType;
import seng302.discoveryServer.util.ServerRepoStreamParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;

public class testStreamParser {
    private static ServerRepoStreamParser parser;
    private static InputStream inputStream;

    private static void setupWithByteArray(byte[] bytes){
        inputStream = new ByteArrayInputStream(bytes);
        parser = new ServerRepoStreamParser(inputStream);
    }

    @Test
    public void testParseRoomCodeRequest() throws Exception {
        Message roomCodeMsg = new RoomCodeRequest("1234");
        setupWithByteArray(roomCodeMsg.getBuffer());

        assertTrue(parser.parse() == PacketType.ROOM_CODE_REQUEST);
        assertTrue(parser.getRoomCode().equals("1234"));
    }
}
