package seng302.server.messages;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
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
    public abstract void send(SocketChannel outputStream) throws IOException;

    /**
     * Allocate byte buffer to correct size
     */
    void allocateBuffer(){
        buffer = ByteBuffer.allocate(Header.getSize() + getSize() + CRC_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        bufferPosition = 0;
    }

    /**
     * Write the set header to the byte buffer
     */
    void writeHeaderToBuffer(){
        buffer.put(getHeader().getByteBuffer().array());
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
            byte[] tmp =  Message.intToByteArray(val, size); //ByteBuffer.allocate(size).putShort((short) (val & 0xffff)).array();
            reverse(tmp);
            buffer.put(tmp);
            moveBufferPositionBy(size);
        }
        else{
            // Use int
            byte[] tmp = Message.intToByteArray(val, size);
            reverse(tmp);
            moveBufferPositionBy(size);
        }
    }

    /**
     * Put a signed int of a specified length in the buffer
     * @param val The integer value to add
     * @param size The size of the integer to be added to the buffer
     */
    void putInt(long val, int size){
        if (size < 4){
            byte[] tmp = Message.intToByteArray(val, size);
            reverse(tmp);
            buffer.put(tmp);
        }
        else{
            byte[] tmp = Message.intToByteArray(val, size);
            reverse(tmp);
            buffer.put(tmp);
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
        buffer.put(bytes.array());
        moveBufferPositionBy(size);
    }


    /**
     * Calculate the CRC of the buffer and append it to the end of the buffer
     */
    void writeCRC(){
        crc = new CRC32();

        buffer.position(0);

        byte[] data = Arrays.copyOfRange(buffer.array(), 0, buffer.array().length-CRC_SIZE);
        crc.update(data);
        buffer.position(bufferPosition);

        putInt((int) crc.getValue(), CRC_SIZE);
    }

    /**
     * @return The current buffer
     */
    public ByteBuffer getBuffer(){
        return buffer;
    }

    /**
     * Rewind the buffer to the beginning
     */
    void rewind(){
        buffer.flip();
    }

    /**
     * Convert an integer to an array of bytes
     * @param val The value to add
     * @param len The width of the integer in the buffer
     * @return
     */
    public static byte[] intToByteArray(long val, int len){
        int index = 0;
        byte[] data = new byte[len];

        for (int i = 0; i < len; i++){
            data[len - index - 1] = (byte) (val & 0xFF);
            val >>>= 8;
            index++;
        }

        return data;
    }

    /**
     * Reverse an array of bytes
     * @param data The byte[] to reverse
     */
    public static void reverse(byte[] data) {
        for (int left = 0, right = data.length - 1; left < right; left++, right--) {
            byte temp = (byte) (data[left] & 0xff);
            data[left]  = (byte) (data[right] & 0xff);
            data[right] = (byte) (temp & 0xff);
        }
    }
}
