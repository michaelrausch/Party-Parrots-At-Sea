package seng302.discoveryServer.util;

import java.io.InputStream;

public class ReadableByteInputStream {
    private InputStream is;

    public ReadableByteInputStream(InputStream is){
        this.is = is;
    }

    /**
     * Get n bytes from the input stream
     * @param n number of bytes
     * @return the bytes read
     * @throws Exception .
     */
    public byte[] getBytes(int n) throws Exception {
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++) {
            bytes[i] = (byte) readByte();
        }
        return bytes;
    }

    /**
     * Skip n bytes
     * @param n number of bytes to skip
     * @throws Exception
     */
    public void skipBytes(long n) throws Exception {
        for (int i = 0; i < n; i++) {
            readByte();
        }
    }

    /**
     * Read the next byte from the stream
     * @return The byte that was read
     * @throws Exception .
     */
    public int readByte() throws Exception {
        int currentByte = is.read();

        if (currentByte == -1) {
            throw new Exception();
        }
        return currentByte;
    }
}
