package seng302.gameServer.messages;


public class RegistrationRequestMessage extends Message {
    private static int MESSAGE_LENGTH = 2;

    public RegistrationRequestMessage(ClientType type, int clientID){
        setHeader(new Header(MessageType.REGISTRATION_REQUEST, clientID, (short) getSize()));

        allocateBuffer();
        writeHeaderToBuffer();

        putInt(type.getCode(), 2);

        writeCRC();
    }

    @Override
    public int getSize() {
        return MESSAGE_LENGTH;
    }
}
