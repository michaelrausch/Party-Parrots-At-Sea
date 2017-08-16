package seng302.visualiser.controllers;

import java.util.*;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.model.RaceState;
import seng302.visualiser.GameClient;

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

    private int MAX_NUM_PLAYERS = 8;

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

        timeUntilStart.setText("");
    }

    /**
     * Updates player names.
     */
    private void updatePlayers() {
        //Update players if one added.
        for (int i = 0; i < players.size(); i++) {
            listViews.get(i).setText(players.get(i));
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

    public void updateRaceState(RaceState raceState){
        this.raceState = raceState;
        timeUntilStart.setText(raceState.getRaceTimeStr());
    }

    public void disableReadyButton () {
        readyButton.setDisable(true);
        readyButton.setVisible(false);
    }
}
