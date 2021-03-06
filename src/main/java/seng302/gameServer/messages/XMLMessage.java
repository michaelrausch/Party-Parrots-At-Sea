package seng302.gameServer.messages;

public class XMLMessage extends Message{
    private final MessageType MESSAGE_TYPE = MessageType.XML_MESSAGE;
    private final int MESSAGE_VERSION = 1; //Always set to 1
    private final int MESSAGE_SIZE = 14;

    // Message fields
    private long timeStamp;
    private long ack = 0x00; //Unused
    private XMLMessageSubType xmlMessageSubType;
    private long length;
    private long sequence;
    private String content;

    /**
     * XML Message from the AC35 Streaming data spec
     * @param content The XML content
     * @param type The XML Message Sub Type
     * @param sequenceNum sequenceNum
     */
    public XMLMessage(String content, XMLMessageSubType type, long sequenceNum){
        this.content = content;
        this.xmlMessageSubType = type;
        timeStamp = System.currentTimeMillis() / 1000L;
        ack = 0;
        length = this.content.length();
        sequence = sequenceNum;

        setHeader(new Header(MESSAGE_TYPE, 0x01, (short) getSize()));
        allocateBuffer();
        writeHeaderToBuffer();

        // Write message fields
        putUnsignedByte((byte) MESSAGE_VERSION);
        putInt((int) ack, 2);
        putInt((int) timeStamp, 6);
        putByte((byte)xmlMessageSubType.getType());
        putInt((int) sequence, 2);
        putInt((int) length, 2);
        putBytes(content.getBytes());

        writeCRC();
        rewind();
    }

    /**
     * @return The length of this message
     */
    public int getSize(){
        return MESSAGE_SIZE + content.length();
    }
}
