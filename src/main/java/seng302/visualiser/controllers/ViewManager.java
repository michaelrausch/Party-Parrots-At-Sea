package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyph;
import java.io.IOException;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.ServerAdvertiser;
import seng302.utilities.BonjourInstallChecker;
import seng302.utilities.Sounds;
import seng302.visualiser.GameClient;

public class ViewManager {

    private static ViewManager instance;
    private GameClient gameClient;
    private JFXDecorator decorator;
    private HashMap<String, String> properties; //TODO is this the best way to do this??
    private ObservableList<String> playerList;
    private Logger logger = LoggerFactory.getLogger(ViewManager.class);

    public Stage getStage() {
        return stage;
    }

    private Stage stage;

    private ViewManager() {
        properties = new HashMap<>();
    }

    private FXMLLoader loadFxml(String fxmlLocation) {
        return new FXMLLoader(
            getClass().getResource(fxmlLocation)
        );
    }

    public static ViewManager getInstance() {
        if (instance == null) {
            instance = new ViewManager();
        }

        return instance;
    }

    /**
     * Initialize the start view in the given stage.
     */
    public void initialStartView(Stage stage) throws Exception {
        this.stage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/views/StartScreenView.fxml"));
        stage.setTitle("Party Parrots At Sea");

        JFXDecorator decorator = new JFXDecorator(stage, root, false, true, true);
        decorator.setCustomMaximize(true);
        decorator.applyCss();
        decorator.getStylesheets()
            .add(getClass().getResource("/css/Master.css").toExternalForm());
        gameClient = new GameClient(decorator);
        setDecorator(decorator);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/PP.png")));
        Scene scene = new Scene(decorator, 1200, 800, false, SceneAntialiasing.BALANCED);
        stage.setMinHeight(800);
        stage.setMinWidth(1200);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> closeAll());

        decorator.setOnCloseButtonAction(this::closeAll);

        // TODO Platform.runLater(this::checkCompatibility);

        Sounds.stopMusic();
        Sounds.playMenuMusic();

        decorator.setOnCloseButtonAction(() -> {
            try {
                ServerAdvertiser.getInstance().unregister();
            } catch (IOException e) {
                logger.warn("Couldn't unregister server");
            }

            gameClient.stopGame();
            System.exit(0);
        });
    }

    /**
     * Sets the decorator when a new one is created (and ideally the old one destroyed)
     * Also allows injection of buttons into the decorator for custom functions.
     *
     * @param newDecorator The new JFXDecorator to handle the game window.
     */
    private void setDecorator(JFXDecorator newDecorator) {
        decorator = newDecorator;

        decorator.setOnCloseButtonAction(() -> {
            gameClient.stopGame();
            System.exit(0);
        });

        //Injecting a volume toggle into the decorator.
        //Get the button box
        HBox btns = (HBox) decorator.getChildren().get(0);

        //Create new button
        JFXButton btnMute = new JFXButton();
        btnMute.setText(" Toggle Sound");
        btnMute.setStyle("-fx-text-fill:#fff");
        btnMute.getStyleClass().add("jfx-decorator-button");
        btnMute.setCursor(Cursor.HAND);

        //Create Graphics
        SVGGlyph spacer = new SVGGlyph(0, "SPACER", "", Color.WHITE);
        SVGGlyph volumeOn = new SVGGlyph(0, "VOLUME_ON",
            "M0,6 L0,12 L4,12 L9,17 L9,1 L4,6 L0,6 L0,6 Z M13.5,9 C13.5,7.2 12.5,5.7 11,5 L11,13 C12.5,12.3 13.5,10.8 13.5,9 L13.5,9 Z M11,0.2 L11,2.3 C13.9,3.2 16,5.8 16,9 C16,12.2 13.9,14.8 11,15.7 L11,17.8 C15,16.9 18,13.3 18,9 C18,4.7 15,1.1 11,0.2 L11,0.2 Z",
            Color.WHITE);
        SVGGlyph volumeOff = new SVGGlyph(0, "VOLUME_ON",
            "M13.5,9 C13.5,7.2 12.5,5.7 11,5 L11,7.2 L13.5,9.7 L13.5,9 L13.5,9 Z M16,9 C16,9.9 15.8,10.8 15.5,11.6 L17,13.1 C17.7,11.9 18,10.4 18,8.9 C18,4.6 15,1 11,0.1 L11,2.2 C13.9,3.2 16,5.8 16,9 L16,9 Z M1.3,0 L0,1.3 L4.7,6 L0,6 L0,12 L4,12 L9,17 L9,10.3 L13.3,14.6 C12.6,15.1 11.9,15.5 11,15.8 L11,17.9 C12.4,17.6 13.6,17 14.7,16.1 L16.7,18.1 L18,16.8 L9,7.8 L1.3,0 L1.3,0 Z M9,1 L6.9,3.1 L9,5.2 L9,1 L9,1 Z",
            Color.WHITE);
        volumeOn.setSize(16, 16);
        volumeOff.setSize(16, 16);
        spacer.setSize(40, 16);

        // Determine which graphic should go on the button
        if (Sounds.isMusicMuted() && Sounds.isSoundEffectsMuted()) {
            btnMute.setGraphic(volumeOff);
        } else {
            btnMute.setGraphic(volumeOn);
        }

        // Add Buttons
        btns.getChildren().add(0, spacer);
        btns.getChildren().add(0, btnMute);
        btnMute.setOnAction((action) -> {
            Sounds.toggleAllSounds();
            if (btnMute.getGraphic().equals(volumeOff)) {
                btnMute.setGraphic(volumeOn);
            } else {
                btnMute.setGraphic(volumeOff);
            }
        });

    }

    /**
     * Determines if a PC has compatibility with the bonjour protocol for server detection.
     */
    private void checkCompatibility() {
        if (BonjourInstallChecker.isBonjourSupported()) {
            BonjourInstallChecker.openInstallUrl();
        }
    }

    private void closeAll() {
        try {
            ServerAdvertiser.getInstance().unregister();
        } catch (IOException e1) {
            logger.warn("Could not un-register game");
        }

        System.exit(0);
    }

    public JFXDecorator getDecorator() {
        return decorator;
    }

    public void setScene(Node scene) {
        Platform.runLater(() -> decorator.setContent(scene));
    }

    /**
     * Create a new stage and re-initialize the start view in the new stage.
     */
    public void goToStartView() {
        try {
            this.stage.close();
            Stage stage = new Stage();
            initialStartView(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GameClient getGameClient() {
        return gameClient;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String val) {
        properties.put(key, val);
    }

    public void setPlayerList(ObservableList<String> playerList) {
        this.playerList = playerList;
    }

    public ObservableList<String> getPlayerList() {
        return playerList;
    }

    /**
     * Change the view to the Lobby Screen
     * @param disableReadyButton Boolean value so that clients can't try start a game.
     * @return A LobbyController object for the Lobby Screen.
     */
    public LobbyController goToLobby(Boolean disableReadyButton) {
        FXMLLoader loader = loadFxml("/views/LobbyView.fxml");

        try {
            setScene(loader.load());
        } catch (IOException e) {
            logger.error("Could not load lobby view");
        }

        LobbyController lobbyController = loader.getController();

        if (disableReadyButton) {
            lobbyController.disableReadyButton();
        }

        return loader.getController();
    }

    /**
     * Sets up the view for the race. Creating a new decorator and destroying the old one.
     * @return A RaceViewController for the race view screen.
     */
    public RaceViewController loadRaceView() {
        FXMLLoader loader = loadFxml("/views/RaceView.fxml");
        // have to create a new stage and set the race view maximized as JFoenix decorator has
        // bug causes stage cannot be fully maximised.
        Platform.runLater(() -> {
            try {
                stage.close();
                stage = new Stage();
                JFXDecorator decorator = new JFXDecorator(stage, loader.load(), false, true, true);
                decorator.setCustomMaximize(true);
                decorator.applyCss();
                decorator.getStylesheets()
                    .add(getClass().getResource("/css/Master.css").toExternalForm());
                setDecorator(decorator);
                Scene scene = new Scene(decorator);
                RaceViewController raceViewController = loader.getController();
                gameClient.setRaceViewController(raceViewController);
                // set key press event to catch key stoke
                scene.setOnKeyPressed(gameClient::keyPressed);
                scene.setOnKeyReleased(gameClient::keyReleased);

                // uncomment to make it full screen
//                Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
//                stage.setX(visualBounds.getMinX());
//                stage.setY(visualBounds.getMinY());
//                stage.setWidth(visualBounds.getWidth());
//                stage.setHeight(visualBounds.getHeight());
//                stage.setMaximized(true);
//                stage.setFullScreen(true);

                stage.setMinHeight(500);
                stage.setMinWidth(800);
                stage.setOnCloseRequest(e -> closeAll());
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        while (loader.getController() == null){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return loader.getController();
    }
}
