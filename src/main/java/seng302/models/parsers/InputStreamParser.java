package seng302.models.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


public class InputStreamParser {

    private static InputStream stream = null;
    private static boolean reading = true;

    private static void skipBytes(int n){
        for (int i=0; i < n; i++){
            readByte();
        }
    }

    private static int readByte() {
        int currentByte = -1;
        try {
            currentByte = stream.read();
            if (currentByte == -1){
                reading = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentByte;
    }

    private static void runTest() {

        Socket host = null;
        String hostAddress = "livedata.americascup.com";
        int hostPort = 4941;

        try {
            host = new Socket(hostAddress, hostPort);
            if (host != null) {
                stream = host.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int sync1;
        int sync2;
        //currently "reading" will not break the program nicely (because there are multiple readBytes within the while loop)
        while(reading) {
            sync1 = readByte();
            sync2 = readByte();
            //checking if it is the start of the packet
            if(sync1 == 0x47 && sync2 == 0x83) {
                System.out.println("message type: " + readByte());
                skipBytes(10);
                byte[] b = new byte[2];
                try {
                    stream.read(b);
                } catch (IOException e){
                    e.printStackTrace();
                }
                int payloadLength = bytesToInt(b);
                System.out.println("payload length: " + payloadLength);
                skipBytes(payloadLength);
                skipBytes(4);
            }
        }

        try {
            if (host != null) {
                host.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * takes an array of up to 4 bytes and returns and int
     * @return an int if there is less than 4 bytes -1 otherwise
     */
    private static int bytesToInt(byte[] bytes){
        int partialInt = 0;
        int index = 0;
        for (byte b: bytes){
            if (index > 3){
                return -1;
            }
            partialInt = partialInt | (b & 0xFF) << (index * 8);
            index++;
        }
        return partialInt;
    }

    public static void main(String[] args) {

        runTest();

    }

}
