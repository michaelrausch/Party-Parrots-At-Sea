package seng302.visualiser.controllers;

import com.sun.media.jfxmedia.logging.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.model.RaceState;
import seng302.visualiser.ClientToServerThread;

/**
 * A class describing the actions of the lobby screen
 * Created by wmu16 on 10/07/17.
 */
public class LobbyController {

    public enum CloseStatus {
        LEAVE,
        READY
    }

    @FunctionalInterface
    public interface LobbyCloseListener {
        void notify(CloseStatus exitCause);
    }

    @FXML
    private Text lobbyIpText;
    @FXML
    private Button readyButton;
    @FXML
    private TextArea playerOneTxt;
    @FXML
    private TextArea playerTwoTxt;
    @FXML
    private TextArea playerThreeTxt;
    @FXML
    private TextArea playerFourTxt;
    @FXML
    private TextArea playerFiveTxt;
    @FXML
    private TextArea playerSixTxt;
    @FXML
    private TextArea playerSevenTxt;
    @FXML
    private TextArea playerEightTxt;
    @FXML
    private ImageView firstImageView;
    @FXML
    private ImageView secondImageView;
    @FXML
    private ImageView thirdImageView;
    @FXML
    private ImageView fourthImageView;
    @FXML
    private ImageView fifthImageView;
    @FXML
    private ImageView sixthImageView;
    @FXML
    private ImageView seventhImageView;
    @FXML
    private ImageView eighthImageView;
    @FXML
    private Text timeUntilStart;
    @FXML
    private Text courseNameText;

    private List<ImageView> imageViews = new ArrayList<>();
    private List<TextArea> listViews = new ArrayList<>();
    private RaceState raceState;

    private ClientToServerThread socketThread;

    private int MAX_NUM_PLAYERS = 8;
    private Integer playerID;

    private List<LobbyCloseListener> lobbyListeners = new ArrayList<>();
    private ObservableList<String> players;

    /**
     * Add all FXObjects to lists and initialize images.
     */
    public void initialize() {
        Collections.addAll(listViews,
            playerOneTxt, playerTwoTxt, playerThreeTxt, playerFourTxt, playerFiveTxt, playerSixTxt,
            playerSevenTxt, playerEightTxt
        );
        Collections.addAll(imageViews,
            firstImageView, secondImageView, thirdImageView, fourthImageView,
            fifthImageView, sixthImageView, seventhImageView, eighthImageView
        );
        initialiseImageView();

        timeUntilStart.setText("Waiting For Host...");
    }

    /**
     * Updates player names.
     */
    private void updatePlayers() {
        //Update players if one added.
        for (int i = 0; i < players.size(); i++) {
            listViews.get(i).setText(players.get(i));
            if (playerID == (i + 1)) {
                listViews.get(i).setText(listViews.get(i).getText() + " (YOU)");
            }
            imageViews.get(i).setVisible(true);
        }
        //Update empty text fields if player left.
        for (int i = MAX_NUM_PLAYERS-1; i >= players.size(); i--) {
            listViews.get(i).setText("");
            imageViews.get(i).setVisible(false);
        }
    }

    /**
     * Sets all images and hides them till players join.
     */
    private void initialiseImageView() {
        for (ImageView viewer : imageViews) {
            viewer.setImage(
                new Image(
                    RaceViewController.class.getResourceAsStream(
                        "/pics/sail.png")
                )
            );
            viewer.setVisible(false);
        }
    }

    @FXML
    public void customize() {
        Parent root;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LobbyController.class.getResource("/views/customizeView.fxml"));
            root = fxmlLoader.load();
            Stage customizeStage = new Stage();
            CustomizationController cc = fxmlLoader.getController();
            cc.setServerThread(this.socketThread);
            customizeStage.setTitle("Customize Boat");
            customizeStage.setScene(new Scene(root, 700, 450));
            cc.setStage(customizeStage); // pass the stage through so it can be closed later.
            customizeStage.show();
        } catch (IOException e) {
            Logger.logMsg(4, "Failed to load Customization View from resources.");
        }
    }

    public void setSocketThread(ClientToServerThread thread) {
        this.socketThread = thread;
    }

    @FXML
    public void leaveLobbyButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function!
        GameState.setCurrentStage(GameStages.CANCELLED);
        // TODO: 20/07/17 wmu16 - Implement some way of terminating the game
        for (LobbyCloseListener readyListener : lobbyListeners)
            readyListener.notify(CloseStatus.LEAVE);
    }

    @FXML
    public void readyButtonPressed() {
        GameState.setCurrentStage(GameStages.PRE_RACE);
        // Do countdown logic here

        for (LobbyCloseListener readyListener : lobbyListeners)
            readyListener.notify(CloseStatus.READY);
    }

    public void setTitle (String title) {
        lobbyIpText.setText(title);
    }

    public void setCourseName(String courseName){
        courseNameText.setText(courseName);
    }

    public void addCloseListener(LobbyCloseListener listener) {
        lobbyListeners.add(listener);
    }

    public void setPlayerListSource (ObservableList<String> players) {
        this.players = players;
        players.addListener((ListChangeListener<? super String>) (lcl) ->
            Platform.runLater(this::updatePlayers)
        );
        Platform.runLater(this::updatePlayers);
    }

    public void setPlayerID(Integer id) {
        playerID = id;
    }

    public void updateRaceState(RaceState raceState){
        this.raceState = raceState;
        timeUntilStart.setText("Starting in: " + raceState.getRaceTimeStr());
    }

    public void disableReadyButton () {
        readyButton.setDisable(true);
        readyButton.setVisible(false);
    }
}
