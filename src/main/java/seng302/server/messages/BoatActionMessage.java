package seng302.server.messages;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by kre39 on 12/07/17.
 */
public class BoatActionMessage extends Message{
    private final MessageType MESSAGE_TYPE = MessageType.BOAT_ACTION;
    private final int MESSAGE_VERSION = 1; //Always set to 1
    private final int MESSAGE_SIZE = 1;
    private BoatActionType actionType;

    public BoatActionMessage(BoatActionType actionType) {
        this.actionType = actionType;
    }

    @Override
    public int getSize() {
        return 0;
    }

    /**
     * Send this message as a stream of bytes
     * @param outputStream The output stream to send the message
     */
    public void send(SocketChannel outputStream) throws IOException {
        System.out.println("Sending boat action type: " + actionType.toString());
        allocateBuffer();
        writeHeaderToBuffer();
        // Write message fields
        putUnsignedByte((byte) MESSAGE_VERSION);
        putInt((int) BoatActionType.getBoatPacketType(actionType), 1);
        writeCRC();
        rewind();

        outputStream.write(getBuffer());
    }
}
