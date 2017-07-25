//package seng302.client;
//
//import java.util.Observable;
//
///**
// * Used by LobbyController to run a separate thread-loop
// * updates the controller when change is detected.
// */
//public class ClientStateQueryingRunnable extends Observable implements Runnable {
//
//    private Boolean terminate = false;
//
//    public ClientStateQueryingRunnable() {}
//
//    @Override
//    public void run() {
//        while(!terminate) {
//            // Sleeping the thread so it will respond to the if statement below
//            // if you know a better fix, pls tell me :) -ryan
//            try {
//                Thread.sleep(0);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            if (ClientState.isRaceStarted() && ClientState.isConnectedToHost()) {
//                setChanged();
//                notifyObservers("game started");
//                terminate();
//            }
//
//            if (ClientState.isDirtyState()) {
//                setChanged();
//                notifyObservers("update players");
//                ClientState.setDirtyState(false);
//            }
//        }
//    }
//
//    public void terminate() {
//        terminate = true;
//    }
//}
