package seng302.gameServerWithThreading;

import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * A class describing the overall server, which creates and collects server threads for each client
 * Created by wmu16 on 13/07/17.
 */
public class ServerThreadHandler extends Thread {

    private static final int PORT = 4950;
    private static final Integer MAX_NUM_PLAYERS = 10;

    private ServerSocket serverSocket = null;
    private Socket socket;
    private ArrayList<ServerThread> serverThreads = new ArrayList<>();

    public ServerThreadHandler() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("IO error in server thread handler upon trying to make new server socket");
        }
    }


    public void run() {
        //You should handle interrupts in some way, so that the thread won't keep on forever if you exit the app.
        while (!isInterrupted()) {
            try {
                Thread.sleep(1000 / 60);    //60 times per second we should calculate the game state
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (GameState.getCurrentStage() == GameStages.LOBBYING && GameState.getPlayers().size() < MAX_NUM_PLAYERS) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    System.out.println("IO error in server thread handler upon trying to accept connection");
                }
                ServerThread thread = new ServerThread(socket);
                serverThreads.add(thread);
                thread.start();
            }

            updateClients();
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("IO error in server thread handler upon closing socket");
        }
    }

    public void updateClients() {
        for (ServerThread serverThread : serverThreads) {
            serverThread.updateClient();
        }
    }
}
