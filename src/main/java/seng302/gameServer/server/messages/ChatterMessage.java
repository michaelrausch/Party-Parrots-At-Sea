package seng302.gameServer.server.messages;

/**
 * Created by kre39 on 20/07/17.
 */
public class ChatterMessage extends Message {

    private final long MESSAGE_VERSION_NUMBER = 1;
    private final int MESSAGE_SIZE = 3;
    private int message_type;
    private int message_size = 21;
    private String message;

    public ChatterMessage(int message_type, int message_size, String message) {
        this.message_type = message_type;
        this.message_size = message_size;
        this.message = message;

        setHeader(new Header(MessageType.CHATTER_TEXT, 1, (short) getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        putByte((byte) MESSAGE_VERSION_NUMBER);
        putInt(message_type, 1);
        putInt(message_size, 1);
        putBytes(message.getBytes());

        writeCRC();
        rewind();
    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE + message_size;
    }


}
