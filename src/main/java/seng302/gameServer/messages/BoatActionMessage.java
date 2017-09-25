package seng302.gameServer.messages;

/**
 * Created by kre39 on 12/07/17.
 */
public class BoatActionMessage extends Message{
    private final MessageType MESSAGE_TYPE = MessageType.BOAT_ACTION;
    private final int MESSAGE_SIZE = 5;
    private BoatAction actionType;

    public BoatActionMessage(BoatAction actionType, int sourceId) {
        this.actionType = actionType;
        setHeader(new Header(MessageType.BOAT_ACTION, sourceId, (short) MESSAGE_SIZE)); // the second variable is the source id
        allocateBuffer();
        writeHeaderToBuffer();
        // Write message fields
        putInt(actionType.getValue(), 1);
        putInt(sourceId, 4);
        writeCRC();
        rewind();
    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE;
    }


}
