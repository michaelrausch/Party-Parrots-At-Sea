package seng302.gameServer.messages;


public class RegistrationRequestMessage extends Message {
    private static int MESSAGE_LENGTH = 2;

    public RegistrationRequestMessage(ClientType type){
        setHeader(new Header(MessageType.REGISTRATION_REQUEST, 1, (short) getSize()));

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
