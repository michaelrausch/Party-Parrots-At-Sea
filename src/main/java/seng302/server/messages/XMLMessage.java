package seng302.server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class XMLMessage extends Message{
    private final MessageType MESSAGE_TYPE = MessageType.XML_MESSAGE;
    private final int MESSAGE_VERSION = 1; //Always set to 1
    private final int MESSAGE_SIZE = 14;

    // Message fields
    private int timeStamp;
    private short ack = 0x00; //Unused
    private XMLMessageSubType xmlMessageSubType;
    private Short length;
    private Short sequence;
    private String content;
    private CRC32 crc;

    /**
     * XML Message from the AC35 Streaming data spec
     * @param content The XML content
     * @param type The XML Message Sub Type
     */
    public XMLMessage(String content, XMLMessageSubType type, short sequenceNum){
        this.content = content;
        this.xmlMessageSubType = type;
        crc = new CRC32();
        timeStamp = (int) (System.currentTimeMillis() / 1000L);
        ack = 0;
        length = (short) this.content.length();
        sequence = sequenceNum;

        setHeader(new Header(MESSAGE_TYPE, 0x01, (short) getSize()));
    }

    /**
     * @return The length of this message
     */
    public int getSize(){
        return MESSAGE_SIZE + content.length();
    }

    /**
     * Send this message as a stream of bytes
     * @param outputStream The output stream to send the message
     */
    public void send(DataOutputStream outputStream) {
        ByteBuffer buff = ByteBuffer.allocate(Header.getSize() + getSize() + 4);
        buff.put(getHeader().getByteBuffer());
        buff.position(Header.getSize());

        // Version Number, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)MESSAGE_VERSION).array());
        buff.position(Header.getSize() + 1);

        // Ack, 2 bytes
        buff.put(ByteBuffer.allocate(2).putShort(ack).array());
        buff.position(Header.getSize() + 3);

        // Timestamp, 6 bytes
        buff.put(ByteBuffer.allocate(6).putInt(timeStamp).array());
        buff.position(Header.getSize() + 9);

        // XML message sub type, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)xmlMessageSubType.getType()).array());
        buff.position(Header.getSize() + 10);

        // Seq num, 2 bytes
        buff.put(ByteBuffer.allocate(2).putShort(sequence).array());
        buff.position(Header.getSize() + 12);

        // Message length, 2 bytes
        buff.put(ByteBuffer.allocate(2).putShort(length).array());
        buff.position(Header.getSize() + 14);

        // XML Content
        buff.put(this.content.getBytes());
        buff.position(Header.getSize() + 14 + this.content.getBytes().length);

        // calculate CRC
        crc.update(buff.array());

        // Add CRC to message
        buff.put(ByteBuffer.allocate(4).putInt((short)crc.getValue()).array());

        // Send
        try {
            outputStream.write(buff.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
