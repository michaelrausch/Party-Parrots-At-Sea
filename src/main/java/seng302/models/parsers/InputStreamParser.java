package seng302.models.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class InputStreamParser {

    private static String currentLine;
    private static BufferedReader buffer = null;

    private static void readLine() {
        try {
            currentLine = buffer.readLine();
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
        while(reading) {
            System.out.println(currentLine);
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

    public static void main(String[] args) {

        runTest();

    }

}
