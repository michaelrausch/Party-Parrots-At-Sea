package seng302.gameServer.server.messages;


public class CustomizeRequestMessage extends Message {


    private static int MESSAGE_LENGTH = 2;

    //Message fields
    private Double sourceID;
    private CustomizeRequestType customizeType;

    public CustomizeRequestMessage(CustomizeRequestType customizeType, Double sourceID) {
        setHeader(
            new Header(MessageType.CUSTOMIZATION_REQUEST, sourceID.intValue(), (short) getSize()));

        allocateBuffer();
        writeHeaderToBuffer();


    }

    @Override
    public int getSize() {
        return 1; // placeholder
    }


}
