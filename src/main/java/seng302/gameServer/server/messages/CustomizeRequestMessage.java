package seng302.gameServer.server.messages;

// TODO: 14/08/17 ajm412: this may eventually need adjusting due to conforming to the agreed spec.
public class CustomizeRequestMessage extends Message {


    private static int MESSAGE_LENGTH = 6;

    //Message fields
    private CustomizeRequestType customizeType;
    private Integer payloadLength;

    public CustomizeRequestMessage(CustomizeRequestType customizeType, double sourceID,
        byte[] payload) {
        setHeader(new Header(MessageType.CUSTOMIZATION_REQUEST, 1, (short) getSize()));

        allocateBuffer();
        writeHeaderToBuffer();

        payloadLength = payload.length;
        putInt((int) sourceID, 4);
        putInt((int) customizeType.getType(), 2);
        putBytes(payload);

        writeCRC();
        rewind();
    }

    @Override
    public int getSize() {
        return MESSAGE_LENGTH + payloadLength; // placeholder
    }


}
