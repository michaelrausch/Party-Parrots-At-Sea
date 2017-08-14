package seng302.gameServer.server.messages;


public class CustomizeRequestMessage extends Message {

    CustomizeRequestType customizeType;
    private static int MESSAGE_LENGTH = 2;

    private Double sourceID;

    public CustomizeRequestMessage(CustomizeRequestType customizeType, Double sourceID,)

    @Override
    public int getSize() {
        return 1; // placeholder
    }


}
