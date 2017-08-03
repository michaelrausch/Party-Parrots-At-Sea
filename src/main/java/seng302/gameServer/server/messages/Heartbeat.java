package seng302.gameServer.server.messages;

public class Heartbeat extends Message {
    private final int MESSAGE_SIZE = 4;

    /**
     * Heartbeat from the AC35 Streaming data spec
     * @param seqNo Increment every time a message is sent
     */
    public Heartbeat(int seqNo){
        setHeader(new Header(MessageType.HEARTBEAT, 0x01, (short) getSize()));

        allocateBuffer();
        writeHeaderToBuffer();

        putUnsignedInt(seqNo, 4);

        writeCRC();
        rewind();
    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE;
    }

}