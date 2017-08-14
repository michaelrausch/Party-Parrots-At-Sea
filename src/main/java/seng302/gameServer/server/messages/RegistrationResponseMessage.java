package seng302.gameServer.server.messages;

public class RegistrationResponseMessage extends Message{

    public RegistrationResponseMessage(int clientSourceID, RegistrationResponseStatus status){
        setHeader(new Header(MessageType.REGISTRATION_RESPONSE, 1, (short) getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        putInt(clientSourceID, 4);
        putInt(status.getCode(), 1);

        writeCRC();
    }

    @Override
    public int getSize() {
        return 5;
    }
}
