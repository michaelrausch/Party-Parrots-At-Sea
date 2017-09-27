package seng302.gameServer.messages;

public class RoomCodeRequest extends Message{
    private int size = 0;

    @Override
    public int getSize() {
        return size;
    }

    public RoomCodeRequest(String roomCode){
        size = roomCode.length() + 6;

        setHeader(new Header(MessageType.ROOM_CODE_REQUEST, 0x01, (short)getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        putInt(roomCode.length(), 6);
        putBytes(roomCode.getBytes());

        writeCRC();
        rewind();
    }
}
