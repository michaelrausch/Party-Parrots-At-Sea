package seng302.gameServer.messages;

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
    private ByteBuffer buff;
    private int buffPos;

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
        buff = ByteBuffer.allocate(MESSAGE_LEN);
        buffPos = 0;
    }

    private void putInBuffer(byte[] bytes, long val){
        byte[] tmp = bytes.clone();
        Message.reverse(tmp);

        buff.put(tmp);
        buffPos += tmp.length;
        buff.position(buffPos);
    }

    /**
     * Reset the buffer
     */
    public void reset(){
        buffPos = 0;
        buff.clear();
        buff.position(buffPos);
    }

    /**
     * @return a ByteBuffer containing the message header
     */
    public ByteBuffer getByteBuffer(){
        reset();

        putInBuffer(ByteBuffer.allocate(1).put((byte)syncByte1).array(), syncByte1);

        putInBuffer(ByteBuffer.allocate(1).put((byte)syncByte2).array(), syncByte2);

        putInBuffer(ByteBuffer.allocate(1).put((byte)messageType.getCode()).array(), messageType.getCode());

        putInBuffer(Message.intToByteArray(timeStamp, 6), timeStamp);

        putInBuffer(Message.intToByteArray(sourceId, 4), sourceId);

        putInBuffer(Message.intToByteArray(messageLength, 2), messageLength);

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
