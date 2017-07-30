package seng302.gameServer;

import java.io.IOException;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import seng302.model.Player;
import seng302.server.messages.Heartbeat;
import seng302.server.messages.Message;

/**
 * Send Heartbeat messages to connected player at a specified interval
 * Will call .clientDisconnected on the delegate when a heartbeat message
 * cannot be sent to a player
 */
public class HeartbeatThread extends Thread{
    private final int HEARTBEAT_PERIOD = 200;
    private ClientConnectionDelegate delegate;
    private Integer seqNum;
    private Stack<Player> disconnectedPlayers;

    public HeartbeatThread(ClientConnectionDelegate delegate){
        this.delegate =  delegate;
        seqNum = 0;
        disconnectedPlayers = new Stack<>();
    }

    /**
     * A player has lost connection to the server
     * The player is added to a stack so that the delegate
     * can be notified
     *
     * @param player The player that has disconnected
     */
    private void playerLostConnection(Player player){
        disconnectedPlayers.push(player);
    }

    /**
     * Sends a heartbeat message to each connected player
     * The delegate is notified if a player has disconnected
     */
    private void sendHeartbeatToAllPlayers(){
        Message heartbeat = new Heartbeat(seqNum);

        for (Player player : GameState.getPlayers()){
            if (!player.getSocket().isConnected()) {
                playerLostConnection(player);
            }

            try {
                player.getSocket().getOutputStream().write(heartbeat.getBuffer());
            } catch (IOException e) {
                playerLostConnection(player);
            }
        }

        updateDelegate();
        seqNum++;
    }

    /**
     * Notifies the delegate about
     * each disconnected player
     */
    private void updateDelegate() {
        while (!disconnectedPlayers.empty()){
            delegate.clientDisconnected(disconnectedPlayers.pop());
        }
    }

    public void run(){
        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeatToAllPlayers();
            }
        }, 0, HEARTBEAT_PERIOD);
    }
}
