package seng302.gameServer.messages;

import seng302.discoveryServer.util.ServerListing;

public class ServerRegistrationMessage extends Message {
    private int size;

    public ServerRegistrationMessage(ServerListing serverListing) {
        String serverName = serverListing.getServerName();
        String mapName = serverListing.getMapName();
        String address = serverListing.getAddress();
        int port = serverListing.getPortNumber();
        int players = serverListing.getPortNumber();
        int capacity = serverListing.getCapacity();
        String roomCode = serverListing.getRoomCode();

        createMessage(serverName, mapName, address, port, players, capacity, roomCode);
    }

    @Override
    public int getSize() {
        return size;
    }

    public ServerRegistrationMessage(String serverName, String mapName, String address, int port, int players, int capacity, String roomCode){
        createMessage(serverName, mapName, address, port, players, capacity, roomCode);
    }

    private void createMessage(String serverName, String mapName, String address, int port, int players, int capacity, String roomCode){
        size = serverName.getBytes().length + mapName.length() + address.length() + roomCode.length() + 36;

        setHeader(new Header(MessageType.REPO_REGISTRATION_REQUEST, 0x01, (short) getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        int nameLength = serverName.length();
        int mapNameLength = mapName.length();
        int addressLength = address.length();
        int roomCodeLength = roomCode.length();

        // Put fields here
        putInt(nameLength, 6);
        putInt(mapNameLength, 6);
        putInt(addressLength, 6);
        putInt(roomCodeLength, 6);

        putInt(port, 4);
        putInt(players, 4);
        putInt(capacity, 4);

        putBytes(serverName.getBytes());
        putBytes(mapName.getBytes());
        putBytes(address.getBytes());
        putBytes(roomCode.getBytes());

        writeCRC();
        rewind();
    }

}
