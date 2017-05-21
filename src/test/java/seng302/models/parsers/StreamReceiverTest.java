package seng302.models.parsers;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import seng302.models.parsers.packets.StreamPacket;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ptg19 on 26/04/17.
 */
public class StreamReceiverTest {

    private PriorityBlockingQueue pq;
    private byte[] brokenPacket = {0x47, (byte) 0x83, 37, // sync1 sync2 and message type
            0b00000000, 0b01000000, 0b00100010, 0b00100100, 0b00011000, 0b00000000, //timestamp
            0b00000000, 0b00010000, 0b01000000, 0b00000000, //source id
            0b00100010, 0b00101000, // message length
            0b00010010, 0b00010010, 0b00010010}; //random start of payload

    private byte[] workingPacket = {0x47, (byte) 0x83, 37, // sync1 sync2 and message type
            0b00000000, 0b01000000, 0b00100010, 0b00100100, 0b00011000, 0b00000000, //timestamp
            0b00000000, 0b00010000, 0b01000000, 0b00000000, //source id
            0b00000010, 0b00000000, // message length
            0b00010010, 0b00010010, // payload
            0b00100110, (byte)0b10000111, 0b00110101, 0b01111000}; //crc

    private byte[] crcMismatchPacket = {0x47, (byte) 0x83, 37, // sync1 sync2 and message type
            0b00000000, 0b01000000, 0b00100010, 0b00100100, 0b00011000, 0b00000000, //timestamp
            0b00000000, 0b00000000, 0b01000000, 0b00000000, //source id
            0b00000010, 0b00000000, // message length
            0b00010010, 0b00010010, // payload
            0b00100110, (byte)0b10000111, 0b00110101, 0b01111000}; //crc


    @Before
    public void setup(){
        pq = new PriorityBlockingQueue<>(256, new Comparator<StreamPacket>() {
            @Override
            public int compare(StreamPacket s1, StreamPacket s2) {
                return (int) (s1.getTimeStamp() - s2.getTimeStamp());
            }
        });
    }

    @Test
    public void connectExitsOnUnexpectedStreamEnd() throws Exception {
        Socket host=mock(Socket.class);
        InputStream stream = new ByteArrayInputStream(brokenPacket);
        when(host.getInputStream()).thenReturn(stream);
        StreamReceiver streamReceiver = new StreamReceiver(host, pq);

        streamReceiver.connect();
        assert pq.size() == 0;
    }

    @Test
    public void connectReadsAPacket() throws Exception {
        Socket host=mock(Socket.class);
        InputStream stream = new ByteArrayInputStream(workingPacket);
        when(host.getInputStream()).thenReturn(stream);
        StreamReceiver streamReceiver = new StreamReceiver(host, pq);

        streamReceiver.connect();
        assert pq.size() == 1;
    }

    @Test
    public void connectDropsAMismatchedCrc() throws Exception {
        Socket host=mock(Socket.class);
        InputStream stream = new ByteArrayInputStream(crcMismatchPacket);
        when(host.getInputStream()).thenReturn(stream);
        StreamReceiver streamReceiver = new StreamReceiver(host, pq);

        streamReceiver.connect();
        assert pq.size() == 0;
    }

    @Test
    public void bytestoLongTest() {
        Socket host=mock(Socket.class);
        StreamReceiver streamReceiver = new StreamReceiver(host, pq);
        try {
            Class[] args = new Class[1];
            args[0] = byte[].class;
            Method bytesToLong = streamReceiver.getClass().getDeclaredMethod("bytesToLong", args);
            bytesToLong.setAccessible(true);
            byte[] sevenBtyeNumber = {0b01100100, 0b00110100, 0b00010100, 0b00000000, 0b00000000, 0b00000000, (byte)0b10000000};
            assert bytesToLong.invoke(streamReceiver, sevenBtyeNumber).equals(36028797020288100L);
            byte[] eightByteNumber = {0b01100100, 0b00110100, 0b00010100, 0b00000000, 0b00000000, 0b00000000, (byte)0b10000000, 0b00100101};
            assert bytesToLong.invoke(streamReceiver, eightByteNumber).equals(-1L);
            byte[] emptyArray = {};
            assert bytesToLong.invoke(streamReceiver, emptyArray).equals(0L);
        } catch (Exception e){
            System.out.println("");
        }
    }

}