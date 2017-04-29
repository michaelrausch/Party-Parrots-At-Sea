package seng302.server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.CRC32;

public class Heartbeat extends Message {
    private final int MESSAGE_SIZE = 4;
    private int seqNo;

    /**
     * Heartbeat from the AC35 Streaming data spec
     * @param seqNo Increment every time a message is sent
     */
    public Heartbeat(int seqNo){
        this.seqNo = seqNo;
    }

    @Override
    public int getSize() {
        return MESSAGE_SIZE;
    }

    @Override
    public void send(SocketChannel outputStream) throws IOException {
        setHeader(new Header(MessageType.HEARTBEAT, 0x01, (short) getSize()));

        allocateBuffer();
        writeHeaderToBuffer();

        putUnsignedInt(seqNo, 4);

        writeCRC();

        outputStream.write(getBuffer());
    }
}