package seng302.server.messages;

import java.nio.ByteBuffer;

public class Header {
    // From API spec
    private final int syncByte1 = 0x47;
    private final int syncByte2 = 0x83;

    private MessageType messageType;
    private int timeStamp;
    private int sourceId;
    private short messageLength;
    private static final int MESSAGE_LEN = 15;

    /**
     * Message Header from section 3.2 of the AC35 Streaming
     * Data spec
     * @param messageType The type of the message following this header
     * @param sourceId The message source (as defined in the spec)
     * @param messageLength The length of the message following this header
     */
    public Header(MessageType messageType, int sourceId, Short messageLength){
        this.messageType = messageType;
        this.sourceId = sourceId;
        this.messageLength = messageLength;
        timeStamp = (int) (System.currentTimeMillis() / 1000L);
    }

    /**
     * @return a ByteBuffer containing the message header
     */
    public ByteBuffer getByteBuffer(){
        ByteBuffer buff = ByteBuffer.allocate(15);

        // Sync Byte 1, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)syncByte1).array());
        buff.position(1);

        // Sync Byte 2, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)syncByte2).array());
        buff.position(2);

        // Message Type, 1 byte
        buff.put(ByteBuffer.allocate(1).put((byte)messageType.getCode()).array());
        buff.position(3);

        // Timestamp, 6 bytes
        int x = ((int) Integer.toUnsignedLong(6));
        buff.put(ByteBuffer.allocate(6).putInt(timeStamp).array());
        buff.position(9);

        // Source ID, 4 bytes
        buff.put(ByteBuffer.allocate(4).putInt(sourceId).array());
        buff.position(13);

        // Message Length, 2 bytes
        buff.put(ByteBuffer.allocate(2).putShort(messageLength).array());
        buff.position(15);

        return buff;
    }

    /**
     * Returns the size of this message
     * @return the size of the message
     */
    public static Integer getSize(){
        return MESSAGE_LEN;
    }
}
