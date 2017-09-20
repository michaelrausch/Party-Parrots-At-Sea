package seng302.serverRepository;

import java.io.InputStream;

public class ReadableByteInputStream {
    private InputStream is;

    public ReadableByteInputStream(InputStream is){
        this.is = is;
    }

    public byte[] getBytes(int n) throws Exception {
        byte[] bytes = new byte[n];
        for (int i = 0; i < n; i++) {
            bytes[i] = (byte) readByte();
        }
        return bytes;
    }

    public void skipBytes(long n) throws Exception {
        for (int i = 0; i < n; i++) {
            readByte();
        }
    }

    public int readByte() throws Exception {
        int currentByte = is.read();

        if (currentByte == -1) {
            throw new Exception();
        }
        return currentByte;
    }
}
