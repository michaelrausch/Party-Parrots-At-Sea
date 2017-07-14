package seng302.gameServerWithThreading;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A class describing a single connection to a Client for the purposes of sending and receiving on its own thread.
 * All server threads created and owned by the server thread handler which can trigger client updates on its threads
 * Created by wmu16 on 13/07/17.
 */
public class ServerThread extends Thread {

    private static final Integer MAX_ID_ATTEMPTS = 10;

    private InputStream is;
    private OutputStream os;
    private Socket socket;

    private Boolean userIdentified = false;
    private Boolean connected = true;
    private Boolean updateClient = true;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("IO error in server thread upon grabbing streams");
        }

        threeWayHandshake();

        // TODO: 13/07/17 wmu16 - Some way of knowing if the client is still connected. perhaps when we read disconnect message switch this bool?
        while (connected) {

            //Perform a read and update game state
            try {
                Integer userInput = is.read();
            } catch (IOException e) {
                System.out.println("IO error in server thread upon reading input stream");
            }


            //Perform a write if it is time to as delegated by the ServerThreadHandler
            if (updateClient) {
                // TODO: 13/07/17 wmu16 - Write out game state - some function that would write all appropriate messages to this output stream
//                try {
//                    GameState.outputState(os);
//                } catch (IOException e) {
//                    System.out.println("IO error in server thread upon writing to output stream");
//                }
                updateClient = false;
            }
        }

        closeSocket();

    }

    public void updateClient() {
        updateClient = true;
    }


    /**
     * Tries to confirm the connection just accepted.
     * Sends ID, expects that ID echoed for confirmation,
     * if so, sends a confirmation packet back to that connection
     * Creates a player instance with that ID and this thread and adds it to the GameState
     * If not, close the socket and end the threads execution
     */
    private void threeWayHandshake() {
        // TODO: 13/07/17 Finish using AC35
//        Integer playerID = GameState.getUniquePlayerID();
//        Integer confirmationID = null;
//        Integer identificationAttempt = 0
//        while (!userIdentified) {
//            os.write(playerID);                                       //Send out new ID looking for echo
//            confirmationID = is.read();
//            if (playerID == idConfirmation) {                         //ID is echoed back. Connection is a client
//                os.write(  some determined confirmation message  );   //Confirm to client
//                GameState.addPlayer(new Player(playerID, this));      //Create a player in game state for client
//                userIdentified = true;
//            } else if (identificationAttempt > MAX_ID_ATTEMPTS) {     //No response. not a client. tidy up and go home.
//                closeSocket();
//                return;
//            }
//        identificationAttempt++;
//        }
    }

    public void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("IO error in server thread upon trying to close socket");
        }
    }
}
