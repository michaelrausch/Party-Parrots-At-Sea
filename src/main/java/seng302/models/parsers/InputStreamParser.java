package seng302.models.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class InputStreamParser {

    //changed the currentline variable from sring to long in order to check it's value
//    private static String currentLine;
    private static long currentLine;
    private static BufferedReader buffer = null;

    private static void readLine() {
        try {
            //Rather than read strings it reads a long which is used for checking the head
//            currentLine = buffer.readline();
            currentLine = buffer.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runTest() {

        Socket host = null;
        String hostAddress = "csse-s302staff.canterbury.ac.nz";
        int hostPort = 4941;

        try {
            host = new Socket(hostAddress, hostPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //buffer = new DataInputStream(host.getInputStream());
            if (host != null) {
                buffer = new BufferedReader(new InputStreamReader(host.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        readLine();
        boolean reading = true;
        long prev = 0;
        long len = 0;
        while(reading) {
//            System.out.println(currentLine);
            readLine();
            //checking if it is the start of the packet
            if(prev == 71 && currentLine == 65533) {
                System.out.println("PACKET LENGTH: " + (len));
                len = 0;
            }
            len += 1;
            prev = currentLine;
            if (currentLine == -1) {
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

    public static void main(String[] args) {

        runTest();

    }

}
