package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.model.ClientYacht;
import seng302.model.Colors;
import seng302.model.Limit;
import seng302.model.RaceState;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Corner;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.utilities.Sounds;
import seng302.visualiser.GameView;
import seng302.visualiser.controllers.cells.PlayerCell;
import seng302.visualiser.controllers.dialogs.BoatCustomizeController;

public class LobbyController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private VBox playerListVBox;
    @FXML
    private ScrollPane playerListScrollPane;
    @FXML
    private JFXButton customizeButton, leaveLobbyButton, beginRaceButton;
    @FXML
    private StackPane serverListMainStackPane;
    @FXML
    private Label serverName;
    @FXML
    private Label mapName;
    @FXML
    private Pane serverMap;
    //---------FXML END---------//

    private List<LobbyController_old.LobbyCloseListener> lobbyListeners = new ArrayList<>();
    private RaceState raceState;
    private JFXDialog customizationDialog;
    public Color playersColor;
    private Map<Integer, ClientYacht> playerBoats;
    private Double mapWidth, mapHeight;
    private GameView gameView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.playerBoats = ViewManager.getInstance().getGameClient().getAllBoatsMap();

        if (this.playersColor == null) {
            this.playersColor = Colors.getColor(ViewManager.getInstance().getGameClient().getServerThread().getClientId() - 1);
        }

        leaveLobbyButton.setOnMouseReleased(event -> leaveLobby());
        beginRaceButton.setOnMouseReleased(event -> beginRace());
        leaveLobbyButton.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            leaveLobby();
        });

        beginRaceButton.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            beginRace();
        });

        Platform.runLater(() -> {
            serverName.setText(ViewManager.getInstance().getProperty("serverName"));
            mapName.setText(ViewManager.getInstance().getProperty("mapName"));

            ViewManager.getInstance().getPlayerList().addListener((ListChangeListener<String>) c -> Platform.runLater(this::refreshPlayerList));

            ViewManager.getInstance().getPlayerList().setAll(ViewManager.getInstance().getPlayerList().sorted());
        });

        Platform.runLater(() -> {
            Integer playerId = ViewManager.getInstance().getGameClient().getServerThread().getClientId();
            String name = ViewManager.getInstance().getGameClient().getPlayerNames().get(playerId - 1);

            playersColor = Colors.getColor(playerId - 1);
            customizationDialog = createCustomizeDialog();

            customizeButton.setOnMouseReleased(event -> {
                Sounds.playButtonClick();
                customizationDialog.show();
            });
        });

        leaveLobbyButton.setOnMouseEntered(e -> Sounds.playHoverSound());
        customizeButton.setOnMouseEntered(e -> Sounds.playHoverSound());
        beginRaceButton.setOnMouseEntered(e -> Sounds.playHoverSound());

        initMapPreview();
    }

    private JFXDialog createCustomizeDialog() {
        // TODO: 12/09/17 ajm412: Why is this here? is there no better way we can do this? Ideally inside the LobbyController.
        FXMLLoader dialog = new FXMLLoader(
            getClass().getResource("/views/dialogs/BoatCustomizeDialog.fxml"));

        JFXDialog customizationDialog = null;

        try {
            customizationDialog = new JFXDialog(serverListMainStackPane, dialog.load(),
                JFXDialog.DialogTransition.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }

        BoatCustomizeController controller = dialog.getController();

        controller.setParentController(this);
        controller.setPlayerColor(this.playersColor);

        return customizationDialog;
    }

    /**
     *
     */
    private void refreshMapView(){
        RaceXMLData raceData = ViewManager.getInstance().getGameClient().getCourseData();
        List<Limit> border = raceData.getCourseLimit();
        List<CompoundMark> marks = new ArrayList<CompoundMark>(raceData.getCompoundMarks().values());
        List<Corner> corners = raceData.getMarkSequence();

        gameView.setSize(mapWidth, mapHeight);

        // Update game view
        gameView.updateBorder(border);
        gameView.updateCourse(marks, corners);
    }

    /**
     * Initializes a top down preview of the race course map.
     */
    private void initMapPreview() {
        gameView = new GameView();
        gameView.setHorizontalBuffer(330d);

        mapWidth = 770d;
        mapHeight = 574d;

        // Add game view
        serverMap.getChildren().clear();
        serverMap.getChildren().add(gameView);

        serverMap.widthProperty().addListener((observable, oldValue, newValue) -> {
            mapWidth = newValue.doubleValue();
            refreshMapView();
        });

        serverMap.heightProperty().addListener((observable, oldValue, newValue) -> {
            mapHeight = newValue.doubleValue();
            refreshMapView();
        });
    }

    /**
     *
     */
    private void beginRace() {
        beginRaceButton.setDisable(true);
        customizeButton.setDisable(true);
        GameState.setCurrentStage(GameStages.PRE_RACE);
        GameState.resetStartTime();
        Platform.runLater(()-> ViewManager.getInstance().getGameClient().startGame());
    }

    /**
     * Refreshes the list of players and their boats, as a series of VBox PlayerCell objects.
     */
    private void refreshPlayerList() {
        playerListVBox.getChildren().clear();
        if (this.playerBoats == null || this.playerBoats.size() == 0) {
            this.playerBoats = ViewManager.getInstance().getGameClient().getAllBoatsMap();
        }
        // TODO: 12/09/2017 ajm412: Make it so that it only removes players who's details have changed.
        for (Integer playerId : playerBoats.keySet()) {
            VBox pane = null;

            ClientYacht yacht = playerBoats.get(playerId);

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/cells/PlayerCell.fxml"));

            loader.setController(new PlayerCell(playerId, yacht.getBoatName(), yacht.getColour()));

            try {
                pane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            playerListVBox.getChildren().add(pane);

        }
    }

    private void leaveLobby() {
        ViewManager.getInstance().getGameClient().stopGame();
        ViewManager.getInstance().goToStartView();
    }

    public void disableReadyButton() {
        this.beginRaceButton.setDisable(true);
        this.beginRaceButton.setText("Waiting for host...");
    }

    /**
     *
     * @param raceState
     */
    public void updateRaceState(RaceState raceState){
        this.raceState = raceState;
        this.beginRaceButton.setText("Starting in: " + raceState.getRaceTimeStr());
    }

    public void setBoats(Map<Integer, ClientYacht> boats) {
        this.playerBoats = boats;
    }

    public void closeCustomizationDialog() {
        customizationDialog.close();
    }
}
