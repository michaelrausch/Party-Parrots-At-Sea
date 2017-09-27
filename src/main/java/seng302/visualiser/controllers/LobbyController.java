package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import seng302.discoveryServer.DiscoveryServerClient;
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
import seng302.visualiser.MapPreview;
import seng302.visualiser.controllers.cells.PlayerCell;
import seng302.visualiser.controllers.dialogs.BoatCustomizeController;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
import seng302.visualiser.fxObjects.assets_3D.ModelType;

public class LobbyController implements Initializable {

    private final double INITIAL_MAP_HEIGHT = 770d;
    private final double INITIAL_MAP_WIDTH = 574d;

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
    private AnchorPane serverMap;
    @FXML
    private Label roomLabel;
    @FXML
    private Pane speedTokenPane, handlingTokenPane, windWalkerTokenPane, bumperTokenPane, randomTokenPane;
    //---------FXML END---------//

    private RaceState raceState;
    private JFXDialog customizationDialog;
    public Color playersColor;
    private Map<Integer, ClientYacht> playerBoats;
    private Double mapWidth = INITIAL_MAP_WIDTH, mapHeight = INITIAL_MAP_HEIGHT;
    private MapPreview mapPreview;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roomLabel.setText("");
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

            int tries = 0;

            while (DiscoveryServerClient.getRoomCode() == null && tries <= 10){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tries ++;
            }

            if (DiscoveryServerClient.getRoomCode() != null){
                setRoomCode(DiscoveryServerClient.getRoomCode());
            }

            ViewManager.getInstance().getPlayerList().addListener((ListChangeListener<String>) c -> Platform.runLater(this::refreshPlayerList));

            ViewManager.getInstance().getPlayerList().setAll(ViewManager.getInstance().getPlayerList().sorted());
        });

        customizeButton.setOnMouseReleased(event -> {
            customizationDialog = createCustomizeDialog();
            Sounds.playButtonClick();
            customizationDialog.show();
        });

        Platform.runLater(() -> {
            Integer playerId = ViewManager.getInstance().getGameClient().getServerThread().getClientId();

            playersColor = Colors.getColor(playerId - 1);
        });

        leaveLobbyButton.setOnMouseEntered(e -> Sounds.playHoverSound());
        customizeButton.setOnMouseEntered(e -> Sounds.playHoverSound());
        beginRaceButton.setOnMouseEntered(e -> Sounds.playHoverSound());

        initMapPreview();
        initTokenPreviews();
    }

    private void initTokenPreviews() {
        Group speedToken = ModelFactory.importModel(ModelType.VELOCITY_PICKUP).getAssets();
        Group handlingToken = ModelFactory.importModel(ModelType.HANDLING_PICKUP).getAssets();
        Group windWalkerToken = ModelFactory.importModel(ModelType.WIND_WALKER_PICKUP).getAssets();
        Group bumperToken = ModelFactory.importModel(ModelType.BUMPER_PICKUP).getAssets();
        Group randomToken = ModelFactory.importModel(ModelType.RANDOM_PICKUP).getAssets();
        List<Group> tokensPreviews = new ArrayList<>(
            Arrays.asList(speedToken, handlingToken, windWalkerToken, bumperToken, randomToken));

        tokensPreviews.forEach((tokenPreview) -> {
            tokenPreview.getTransforms().addAll(
                new Translate(40, 50, 0),
                new Scale(13, 13, 13));
        });

        //Hacky rotations for wind and random to level it in the plane
        windWalkerToken.getTransforms().add(new Rotate(-70, new Point3D(1, 0, 0)));
        randomToken.getTransforms().add(new Rotate(-90, new Point3D(1, 0, 0)));

        speedTokenPane.getChildren().add(speedToken);
        handlingTokenPane.getChildren().add(handlingToken);
        windWalkerTokenPane.getChildren().add(windWalkerToken);
        bumperTokenPane.getChildren().add(bumperToken);
        randomTokenPane.getChildren().add(randomToken);

    }

    private JFXDialog createCustomizeDialog() {
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
        controller.setPlayerName(this.playerBoats
            .get(ViewManager.getInstance().getGameClient().getServerThread().getClientId())
            .getBoatName());
        controller.setCurrentBoat(this.playerBoats.get(ViewManager.getInstance().getGameClient().getServerThread().getClientId())
            .getBoatType().toString());

        return customizationDialog;
    }


    /**
     * Initializes a top down preview of the race course map.
     */
    private void initMapPreview() {
        RaceXMLData raceData = ViewManager.getInstance().getGameClient().getCourseData();
        List<Limit> border = raceData.getCourseLimit();
        List<CompoundMark> marks = new ArrayList<>(raceData.getCompoundMarks().values());
        List<Corner> corners = raceData.getMarkSequence();

        mapPreview = new MapPreview(marks, corners, border);
        serverMap.getChildren().clear();
        serverMap.getChildren().add(mapPreview.getAssets());

        mapPreview.setSize(mapWidth, mapHeight);

        serverMap.widthProperty().addListener((observable, oldValue, newValue) -> {
            mapWidth = newValue.doubleValue();
            mapPreview.setSize(mapWidth, mapHeight);
        });
//
        serverMap.heightProperty().addListener((observable, oldValue, newValue) -> {
            mapHeight = newValue.doubleValue();
            mapPreview.setSize(mapWidth, mapHeight);
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

            loader.setController(new PlayerCell(playerId, yacht));

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

    public void setRoomCode(String roomCode) {
        roomLabel.setText("Room: " + roomCode);
    }
}
