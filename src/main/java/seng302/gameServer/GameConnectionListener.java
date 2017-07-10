package seng302.gameServer;

import seng302.models.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * A class defining the lobby host that will wait for connections and update the GameState appropriately
 * Created by wmu16 on 10/07/17.
 */
public class GameConnectionListener extends Thread{

    public static final Integer GAME_HOST_PORT = 4950;

    private Thread t;
    private ServerSocketChannel serverSocketChannel;


    public GameConnectionListener() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        // TODO: 10/07/17 wmu16 - If you pres host, leave lobby, host, - an error is thrown as this port is already bound.
        serverSocketChannel.socket().bind(new InetSocketAddress("localhost", GAME_HOST_PORT));
    }


    /**
     * Starts the listening thread
     */
    public void start() {
        if (t == null) {
            t = new Thread(this, "GameConnectionListener");
            t.start();
        }
    }


    /**
     * This listens for players connecting and adds them to the GameState object
     * WHILE - max plaers is not exceeded AND the race has not started
     */
    public void run() {
        while(GameState.getPlayers().size() < GameState.MAX_NUM_PLAYERS && !GameState.getIsRaceStarted()) {
            try {
                GameState.addPlayer(new Player(serverSocketChannel.accept()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
