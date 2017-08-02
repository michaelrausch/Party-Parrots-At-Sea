package seng302.client;

import java.util.Observable;

/**
 * Used by LobbyController to run a separate thread-loop
 * updates the controller when change is detected.
 */
public class ClientStateQueryingRunnable extends Observable implements Runnable {

    private Boolean terminate = false;

    public ClientStateQueryingRunnable() {}

    /**
     * Notifies observers(the lobby controller) that "game started" if ClientState
     * raceStarted flag is true and terminates itself. Also, it notifies observers
     * to add/remove players if ClientState boatsUpdated flag is true, then resets
     * the flag to false;
     */
    @Override
    public void run() {
        while(!terminate) {
            // Sleeping the thread so it will respond to the if statement below
            // if you know a better fix, pls tell me :) -ryan
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                continue;
            }

            if (ClientState.isRaceStarted() && ClientState.isConnectedToHost()) {
                setChanged();
                notifyObservers("game started");
                terminate();
            }

            if (ClientState.isBoatsUpdated()) {
                setChanged();
                notifyObservers("update players");
                ClientState.setBoatsUpdated(false);
            }
        }
    }

    /**
     * Used to terminate the thread.
     *
     * Currently called by the main while loop when game started is detected.
     */
    public void terminate() {
        terminate = true;
    }
}
