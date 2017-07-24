package seng302.client;

import java.util.List;
import java.util.Observable;

/**
 * Created by zyt10 on 21/07/17.
 */
public class ClientStateQueryingRunnable extends Observable implements Runnable {

    private Boolean terminate = false;

    public ClientStateQueryingRunnable() {}

    @Override
    public void run() {
        while(!terminate) {
            if (ClientState.isRaceStarted() && ClientState.isConnectedToHost()) {
                setChanged();
                notifyObservers();
            }
        }
    }

    public void terminate() {
        terminate = true;
    }
}
