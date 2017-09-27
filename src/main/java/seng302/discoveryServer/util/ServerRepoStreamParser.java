package seng302.discoveryServer.util;


import seng302.gameServer.messages.Message;
import seng302.model.stream.packets.PacketType;

import java.io.InputStream;
import java.util.Arrays;

public class ServerRepoStreamParser {
    private ReadableByteInputStream inputStream;

    private String roomCode;
    private String mapName;
    private ServerListing serverListing;

    public ServerRepoStreamParser(InputStream is){
        inputStream = new ReadableByteInputStream(is);
    }

    public PacketType parse() throws Exception {
        int sync1 = inputStream.readByte();
        int sync2 = inputStream.readByte();

        PacketType packetType = null;

        if (sync1 == 0x47 && sync2 == 0x83) {
            int type = inputStream.readByte();
            inputStream.skipBytes(10);
            long payloadLength = Message.bytesToLong(inputStream.getBytes(2));
            byte[] payload = inputStream.getBytes((int) payloadLength);
            inputStream.skipBytes(4);

            packetType = PacketType.assignPacketType(type, payload);

            switch (packetType) {
                case ROOM_CODE_REQUEST:
                    roomCode = parseRoomCodeRequest(payload);
                    break;

                case LOBBY_REQUEST:
                    mapName = parseLobbyRequest(payload);

                case SERVER_REGISTRATION:
                    serverListing = parseServerRegistration(payload);
                    break;
            }

        }

        return packetType;
    }
    private String parseLobbyRequest(byte[] payload) {
        int mapNameLength = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 0 ,4));

        return new String(Arrays.copyOfRange(payload, 4, 4+mapNameLength));
    }

    private String parseRoomCodeRequest(byte[] payload) {
        int roomCodeLength = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 0 ,6));

        return new String(Arrays.copyOfRange(payload, 6, 6+roomCodeLength));
    }

    public static ServerListing parseServerRegistration(byte[] payload) {
        int nameLength = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 0, 6));
        int mapNameLength = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 6, 12));
        int addressLength = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 12, 18));
        int roomCodeLength = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 18, 24));

        int portNumber = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 24, 28));
        int players = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 28, 32));
        int capacity = (int) Message.bytesToLong(Arrays.copyOfRange(payload, 32, 36));

        int currentPos = 36;
        int nextPos = currentPos + nameLength;
        String serverName = new String(Arrays.copyOfRange(payload, currentPos, nextPos));

        currentPos = nextPos;
        nextPos = currentPos + mapNameLength;
        String mapName = new String(Arrays.copyOfRange(payload, currentPos, nextPos));

        currentPos = nextPos;
        nextPos = currentPos + addressLength;
        String address = new String(Arrays.copyOfRange(payload, currentPos, nextPos));

        currentPos = nextPos;
        nextPos = currentPos + roomCodeLength;
        String roomCode = new String(Arrays.copyOfRange(payload, currentPos, nextPos));

        ServerListing serverListing = new ServerListing(serverName, mapName, address, portNumber, capacity);
        serverListing.setNumberOfPlayers(players);
        serverListing.setRoomCode(roomCode);

        return serverListing;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getMapName() {
        return mapName;
    }

    public ServerListing getServerListing() {
        return serverListing;
    }
}
