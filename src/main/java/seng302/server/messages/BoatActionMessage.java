package seng302.server.messages;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by kre39 on 12/07/17.
 */
public class BoatActionMessage extends Message{
    private final MessageType MESSAGE_TYPE = MessageType.BOAT_ACTION;
    private final int MESSAGE_SIZE = 1;
    private BoatActionType actionType;

    public BoatActionMessage(BoatActionType actionType) {
        this.actionType = actionType;
        setHeader(new Header(MessageType.BOAT_ACTION, 0, (short) 1)); // the second variable is the source id

    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE;
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
        putInt((int) BoatActionType.getBoatPacketType(actionType), 1);
        writeCRC();
        rewind();

        outputStream.write(getBuffer());
    }
}
