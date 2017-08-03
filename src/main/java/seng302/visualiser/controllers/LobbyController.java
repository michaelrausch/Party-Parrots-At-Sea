package seng302.visualiser.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;

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
    private GridPane lobbyScreen;
    @FXML
    private Text lobbyIpText;
    @FXML
    private Button readyButton;
    @FXML
    private ListView<String> firstListView;
    @FXML
    private ListView secondListView;
    @FXML
    private ListView thirdListView;
    @FXML
    private ListView fourthListView;
    @FXML
    private ListView fifthListView;
    @FXML
    private ListView sixthListView;
    @FXML
    private ListView seventhListView;
    @FXML
    private ListView eighthListView;
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

    private List<ObservableList<String>> competitors = new ArrayList<>();
    private ObservableList<String> firstCompetitor = FXCollections.observableArrayList();
    private ObservableList<String> secondCompetitor = FXCollections.observableArrayList();
    private ObservableList<String> thirdCompetitor = FXCollections.observableArrayList();
    private ObservableList<String> fourthCompetitor = FXCollections.observableArrayList();
    private ObservableList<String> fifthCompetitor = FXCollections.observableArrayList();
    private ObservableList<String> sixthCompetitor = FXCollections.observableArrayList();
    private ObservableList<String> seventhCompetitor = FXCollections.observableArrayList();
    private ObservableList<String> eighthCompetitor = FXCollections.observableArrayList();

    private List<ImageView> imageViews = new ArrayList<>();
    private List<ListView> listViews;

    private int MAX_NUM_PLAYERS = 8;

    private List<LobbyCloseListener> lobbyListeners = new ArrayList<>();
    private ObservableList<String> players = FXCollections.observableArrayList();

    public void initialize() {
        imageViews = new ArrayList<>();
        Collections
            .addAll(imageViews, firstImageView, secondImageView, thirdImageView, fourthImageView,
                fifthImageView, sixthImageView, seventhImageView, eighthImageView);
        listViews = new ArrayList<>();
        Collections.addAll(listViews, firstListView, secondListView, thirdListView, fourthListView, fifthListView,
            sixthListView, seventhListView, eighthListView);
        competitors = new ArrayList<>();
        Collections.addAll(competitors, firstCompetitor, secondCompetitor, thirdCompetitor,
            fourthCompetitor, fifthCompetitor, sixthCompetitor, seventhCompetitor, eighthCompetitor);

        initialiseImageView();
    }

    private void initialiseListView() {
        listViews.forEach(listView -> listView.getItems().clear());
        imageViews.forEach(gif -> gif.setVisible(false));
        competitors.forEach(ol -> ol.removeAll());
        for (int i = 0; i < players.size(); i++) {
            competitors.get(i).add(players.get(i));
            listViews.get(i).setItems(competitors.get(i));
            imageViews.get(i).setVisible(true);
        }
    }

    private void initialiseImageView() {
        imageViews.add(firstImageView);
        imageViews.add(secondImageView);
        imageViews.add(thirdImageView);
        imageViews.add(fourthImageView);
        imageViews.add(fifthImageView);
        imageViews.add(sixthImageView);
        imageViews.add(seventhImageView);
        imageViews.add(eighthImageView);
        for (int i = 0; i < MAX_NUM_PLAYERS; i++) {
            imageViews.get(i).setImage(
                new Image(
                    RaceViewController.class.getResourceAsStream(
                        "/pics/sail.png")
                )
            );
        }
    }

    @FXML
    public void leaveLobbyButtonPressed() {
        // TODO: 10/07/17 wmu16 - Finish function!
//        setContentPane("/views/StartScreenView.fxml");
        GameState.setCurrentStage(GameStages.CANCELLED);
        // TODO: 20/07/17 wmu16 - Implement some way of terminating the game
//        ClientState.setConnectedToHost(false);
        for (LobbyCloseListener readyListener : lobbyListeners)
            readyListener.notify(CloseStatus.LEAVE);

    }

    @FXML
    public void readyButtonPressed() {
        GameState.setCurrentStage(GameStages.RACING);
        for (LobbyCloseListener readyListener : lobbyListeners)
            readyListener.notify(CloseStatus.READY);
    }


//    private static MediaPlayer mediaPlayer;
//
//    private void playTheme() {
//        Random random = new Random(System.currentTimeMillis());
//        Integer rand = random.nextInt();
//        if(rand == 10) {
//            URL file = getClass().getResource("/music/Disturbed - down with the sickness.mp3");
//            Media hit = new Media(file.toString());
//            mediaPlayer = new MediaPlayer(hit);
//            mediaPlayer.play();
//        } else if(rand == 9) {
//            URL file = getClass().getResource("/music/Owl City - Fireflies.mp3");
//            Media hit = new Media(file.toString());
//            mediaPlayer = new MediaPlayer(hit);
//            mediaPlayer.play();
//        }
//    }

//    private void switchToRaceView() {
//        if (!switchedPane) {
//            switchedPane = true;
//            setContentPane("/views/RaceView.fxml");
//        }
//    }
// TODO: 26/07/17 cir27 - Could probably be done in a cleaner way.
    public void setTitle (String title) {
        lobbyIpText.setText(title);
    }

    public void addCloseListener(LobbyCloseListener listener) {
        lobbyListeners.add(listener);
    }

    public void setPlayerListSource (ObservableList<String> players) {
        this.players = players;
        players.addListener((ListChangeListener<? super String>) (lcl) ->
            Platform.runLater(this::initialiseListView)
        );
        Platform.runLater(this::initialiseListView);
    }

    public void disableReadyButton () {
        readyButton.setDisable(true);
    }
}
