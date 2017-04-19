package seng302.server.messages;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    public void send(DataOutputStream outputStream) {
        setHeader(new Header(MessageType.HEARTBEAT, 0x01, (short) getSize()));

        ByteBuffer buff = ByteBuffer.allocate(Header.getSize() + getSize() + getSize());

        // Write header
        buff.put(getHeader().getByteBuffer());
        buff.position(Header.getSize());

        // Write seq num
        buff.put(ByteBuffer.allocate(4).putInt(seqNo).array());
        buff.position(Header.getSize()+4);

        // Write CRC
        CRC32 crc = new CRC32();
        crc.update(buff.array());

        buff.put(ByteBuffer.allocate(4).putInt((short)crc.getValue()).array());

        try {
            outputStream.write(buff.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}