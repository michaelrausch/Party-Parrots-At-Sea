package seng302.models.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;


public class InputStreamParser {

    private static InputStream stream = null;
    private static boolean reading = true;
    private static BufferedReader buffer = null;
    private static String currentLine;
    private static boolean isWithinTag = false;

    private static void skipBytes(int n){
        for (int i=0; i < n; i++){
            readByte();
        }
    }

    private static void readLine() {
        try {
            //Rather than read strings it reads a long which is used for checking the head
            currentLine = buffer.readLine();
        } catch (IOException e) {
            e.printStackTrace();
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

    private static void runPacketLengthTest() {

        Socket host = null;
        String hostAddress = "csse-s302staff.canterbury.ac.nz";
//        String hostAddress = "livedata.americascup.com";
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


    private static void runParserTest() {
        Socket host = null;
        String hostAddress = "csse-s302staff.canterbury.ac.nz";
        int hostPort = 4941;

        try {
            host = new Socket(hostAddress, hostPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (host != null) {
                buffer = new BufferedReader(new InputStreamReader(host.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        readLine();
        boolean reading = true;

        while(reading) {
            parseLine(currentLine);
            readLine();

            if (currentLine == null) {
                reading = false;
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

    private static void parseLine(String line){
        if (line.startsWith("<Boat") || line.startsWith("<Race")){
            isWithinTag = true;
        }
        if (isWithinTag) {
//            System.out.println(line);
        }
        if (line.startsWith("</Boat") || line.startsWith("</Race")) {
            isWithinTag = false;
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

        runPacketLengthTest();
        runParserTest();
    }


}
