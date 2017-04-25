package seng302.server.messages;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public abstract class Message {
    private final int CRC_SIZE = 4;
    private Header header;
    private ByteBuffer buffer;
    private int bufferPosition;
    private CRC32 crc;

    /**
     * @param header Set the header for this message
     */
    void setHeader(Header header){
        this.header = header;
    }

    /**
     * @return the header specified for this message
     */
    Header getHeader(){
        return header;
    }

    /**
     * @return the size of the message
     */
    public abstract int getSize();

    /**
     * Send the message as through the outputStream
     */
    public abstract void send(DataOutputStream outputStream);

    /**
     * Allocate byte buffer to correct size
     */
    void allocateBuffer(){
        buffer = ByteBuffer.allocate(Header.getSize() + getSize() + CRC_SIZE);
        bufferPosition = 0;
    }

    /**
     * Write the set header to the byte buffer
     */
    void writeHeaderToBuffer(){
        buffer.put(getHeader().getByteBuffer());
        bufferPosition += Header.getSize();
        buffer.position(bufferPosition);
    }

    /**
     * Move the buffer position by n bytes
     * @param size Number of bytes to move the buffer by
     */
    private void moveBufferPositionBy(int size){
        bufferPosition += size;
        buffer.position(bufferPosition);
    }

    /**
     * Put an unsigned byte in the buffer
     */
    void putUnsignedByte(byte b){
        buffer.put(ByteBuffer.allocate(1).put((byte) (b & 0xff)).array());
        moveBufferPositionBy(1);
    }

    /**
     * Put an signed byte in the buffer
     */
    void putByte(byte b){
        buffer.put(ByteBuffer.allocate(1).put(b).array());
        moveBufferPositionBy(1);
    }

    /**
     * Place an unsigned integer of the specified length in the buffer
     * @param val The integer value to add (Note: This must be long due to java not supporting unsigned integers)
     * @param size The size of the int to be added to the buffer
     */
    void putUnsignedInt(long val, int size){
        if (size <= 1){
            putUnsignedByte((byte) val);

        }
        else if (size < 4){
            // Use short
            buffer.put(ByteBuffer.allocate(size).putShort((short) (val & 0xffff)).array());
            moveBufferPositionBy(size);
        }
        else{
            // Use int
            buffer.put(ByteBuffer.allocate(size).putInt((int) (val & 0xffffffffL)).array());
            moveBufferPositionBy(size);
        }
    }

    /**
     * Put a signed int of a specified length in the buffer
     * @param val The integer value to add
     * @param size The size of the integer to be added to the buffer
     */
    void putInt(int val, int size){
        if (size < 4){
            buffer.put(ByteBuffer.allocate(size).putShort((short) val).array());
        }
        else{
            buffer.put(ByteBuffer.allocate(size).putInt((short) val).array());
        }
        moveBufferPositionBy(size);
    }

    /**
     * Write an array of bytes to the buffer
     * @param bytes to write
     */
    void putBytes(byte[] bytes){
        buffer.put(bytes);
        moveBufferPositionBy(bytes.length);
    }

    /**
     * Write a ByteBuffer of bytes to the buffer
     * @param bytes to write
     * @param size number of bytes
     */
    void putBytes(ByteBuffer bytes, int size){
        buffer.put(bytes);
        moveBufferPositionBy(size);
    }


    /**
     * Calculate the CRC of the buffer and append it to the end of the buffer
     */
    void writeCRC(){
        crc = new CRC32();

        buffer.position(0);
        crc.update(buffer.array());
        buffer.position(bufferPosition);

        putInt((int) crc.getValue(), CRC_SIZE);
    }

    /**
     * @return The current buffer
     */
    public ByteBuffer getBuffer(){
        return buffer;
    }

}
