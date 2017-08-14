package seng302.gameServer.server.messages;

/**
 * Created by ajm412 on 14/08/17.
 */
public class CustomizeResponseMessage extends Message {

    private static int MESSAGE_LENGTH = 2;

    public CustomizeResponseMessage(CustomizeResponseType responseType) {
        setHeader(new Header(MessageType.CUSTOMIZATION_RESPONSE, 1, (short) getSize()));

        allocateBuffer();
        writeHeaderToBuffer();

        putInt(responseType.getType(), 2);

        writeCRC();
        rewind();
    }

    @Override
    public int getSize() {
        return MESSAGE_LENGTH; // placeholder
    }


}
