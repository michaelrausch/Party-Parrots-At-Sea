package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
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
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seng302.gameServer.ServerAdvertiser;
import seng302.utilities.BonjourInstallChecker;
import seng302.utilities.Sounds;
import seng302.visualiser.GameClient;
import seng302.visualiser.controllers.dialogs.BoatCustomizeController;

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

        setDecorator(decorator);

        gameClient = new GameClient(decorator);

        stage.getIcons().add(new Image(getClass().getResourceAsStream("/PP.png")));
        Scene scene = new Scene(decorator, 1200, 800);
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
            "M39.389,13.769 22.235,28.606 6,28.606 6,47.699 21.989,47.699 39.389,62.75 39.389,13.769 M 48.128,49.03 C 50.057,45.934 51.19,42.291 51.19,38.377 C 51.19,34.399 50.026,30.703 48.043,27.577 M 55.082,20.537 C 58.777,25.523 60.966,31.694 60.966,38.377 C 60.966,44.998 58.815,51.115 55.178,56.076 M 61.71,62.611 C 66.977,55.945 70.128,47.531 70.128,38.378 C 70.128,29.161 66.936,20.696 61.609,14.01",
            Color.WHITE);
        SVGGlyph volumeOff = new SVGGlyph(0, "VOLUME_ON",
            "M39.389,13.769 22.235,28.606 6,28.606 6,47.699 21.989,47.699 39.389,62.75 39.389,13.769",
            Color.WHITE);
        volumeOn.setSize(16, 16);
        volumeOff.setSize(12, 16);
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

        if (disableReadyButton) {
            LobbyController lobbyController = loader.getController();
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

    // TODO: 12/09/17 ajm412: Why is this here? is there no better way we can do this? Ideally inside the LobbyController.
    public JFXDialog loadCustomizationDialog(StackPane parent, LobbyController lobbyController,
        Color playerColor, String name) {
        FXMLLoader dialog = loadFxml("/views/dialogs/BoatCustomizeDialog.fxml");

        JFXDialog customizationDialog = null;

        try {
            customizationDialog = new JFXDialog(parent, dialog.load(),
                JFXDialog.DialogTransition.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }

        BoatCustomizeController controller = dialog.getController();

        controller.setParentController(lobbyController);
        controller.setPlayerColor(playerColor);
        controller.setPlayerName(name);
        controller.setServerThread(gameClient.getServerThread());
        controller.setPlayerColor(lobbyController.playersColor);

        return customizationDialog;
    }
}
