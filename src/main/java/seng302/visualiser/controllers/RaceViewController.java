package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.animation.Timeline;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import seng302.model.ClientYacht;
import seng302.model.RaceState;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.token.TokenType;
import seng302.utilities.Sounds;
import seng302.visualiser.GameView3D;
import seng302.visualiser.controllers.annotations.ImportantAnnotationController;
import seng302.visualiser.controllers.annotations.ImportantAnnotationDelegate;
import seng302.visualiser.controllers.annotations.ImportantAnnotationsState;
import seng302.visualiser.controllers.cells.WindCell;
import seng302.visualiser.MiniMap;
import seng302.visualiser.controllers.dialogs.FinishDialogController;
import seng302.visualiser.fxObjects.ChatHistory;

/**
 * Controller class that manages the display of a race
 */
public class RaceViewController extends Thread {

    private final int CHAT_LIMIT = 128;
    private static final Double ICON_BLINK_TIMEOUT_RATIO = 0.6;
    private static final Integer ICON_BLINK_PERIOD = 500;

    @FXML
    private AnchorPane loadingScreenPane;
    @FXML
    private ImageView loadingScreen;
    @FXML
    private JFXButton chatSend;
    @FXML
    private Pane chatHistoryHolder;
    @FXML
    private TextField chatInput;
    @FXML
    private Label timerLabel;
    @FXML
    private StackPane contentStackPane;
    @FXML
    private Pane miniMapPane;
    @FXML
    private ImageView windImageView;
    @FXML
    private AnchorPane rvAnchorPane;
    @FXML
    private AnchorPane windArrowHolder;
    @FXML
    private Slider annotationSlider;
    @FXML
    private Button selectAnnotationBtn;
    @FXML
    private ComboBox<ClientYacht> yachtSelectionComboBox;
    @FXML
    private Text fpsDisplay;
//    @FXML
//    private ImageView windImageView;
    @FXML
    private Label windDirectionLabel;
    @FXML
    private Label windSpeedLabel;
    @FXML
    private Label positionLabel, boatSpeedLabel, boatHeadingLabel;
    @FXML
    private ImageView velocityIcon, handlingIcon, windWalkerIcon, bumperIcon, badRandomIcon;
    @FXML
    private VBox windArrowVBox;
    @FXML
    private JFXButton miniMapButton;


    private WindCell windCell;
    //Race Data
    private Map<Integer, ClientYacht> participants;
    private Map<Integer, CompoundMark> markers;
    private RaceXMLData courseData;
    private GameView3D gameView;
    private RaceState raceState;
    private ChatHistory chatHistory;
    private Timer timer = new Timer();
    private ClientYacht player;
    private JFXDialog finishScreenDialog;
    private FinishDialogController finishDialogController;
    private Timer blinkingTimer = new Timer();
    private ImageView iconToDisplay;
    private Double lastWindDirection;
    private MiniMap miniMap;

    public void initialize() {
        miniMapPane.setVisible(false);
        miniMapButton.setVisible(false);
        contentStackPane.setVisible(false);
        Image loadingImage = new Image("PP.png");
        loadingScreen.setImage(loadingImage);
        //Centers the Image within the image view
        double w = 0;
        double h = 0;
        double ratioX = loadingScreen.getFitWidth() / loadingImage.getWidth();
        double ratioY = loadingScreen.getFitHeight() / loadingImage.getHeight();
        double reduceRatio = 0;
        if(ratioX >= ratioY) {
            reduceRatio = ratioY;
        } else {
            reduceRatio = ratioX;
        }
        w = loadingImage.getWidth() * reduceRatio;
        h = loadingImage.getHeight() * reduceRatio;
        loadingScreen.setX((loadingScreen.getFitWidth() - w) / 2);
        loadingScreen.setY((loadingScreen.getFitHeight() - h) / 2);
        Sounds.stopMusic();
        Sounds.playRaceMusic();

        chatInput.lengthProperty().addListener((obs, oldLen, newLen) -> {
            if (newLen.intValue() > CHAT_LIMIT) {
                chatInput.setText(chatInput.getText().substring(0, CHAT_LIMIT));
            }
        });
        chatHistory = new ChatHistory();
        chatHistoryHolder.getChildren().addAll(chatHistory);
        chatHistory.prefWidthProperty().bind(
            chatHistoryHolder.widthProperty()
        );
        chatHistory.prefHeightProperty().bind(
            chatHistoryHolder.heightProperty()
        );

        contentStackPane.setOnMouseClicked(event -> {
            contentStackPane.requestFocus();
        });
        Platform.runLater(contentStackPane::requestFocus);
        //Makes the chat history non transparent when clicked on
        chatInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                chatHistory.increaseOpacity();
            } else {
                chatHistory.decreaseOpacity();
            }
        });

        lastWindDirection = 0d;

    }

    /**
     * Initialise wind arrow cell.
     */
    private void initialiseWindArrow() {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/views/cells/WindCell.fxml"));
        windCell = new WindCell();
        loader.setController(windCell);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        windCell.init(player, raceState.getWindDirection());
        windCell.setCamera(gameView.getView().getCamera());
        gameView.getView().cameraProperty()
            .addListener((obs, oldVal, newVal) -> windCell.setCamera(newVal));

        windArrowVBox.getChildren().add(windCell.getAssets());
    }

    public void showFinishDialog(ArrayList<ClientYacht> finishedBoats) {
        raceState.setRaceStarted(false);
        createFinishDialog(finishedBoats);
    }

    public void showView(){
        loadingScreenPane.setVisible(false);
        contentStackPane.setVisible(true);
        miniMapPane.setVisible(true);
        miniMapButton.setVisible(true);
        Platform.runLater(() -> contentStackPane.requestFocus());
    }

    /**
     * Create finishScreenDialog and set up finishDialogController.
     */
    private void createFinishDialog(ArrayList<ClientYacht> finishedBoats) {
        FXMLLoader dialog = new FXMLLoader(
            getClass().getResource("/views/dialogs/RaceFinishDialog.fxml"));

        Platform.runLater(() -> {
            try {
                finishScreenDialog = new JFXDialog(contentStackPane, dialog.load(),
                    JFXDialog.DialogTransition.CENTER);
                finishDialogController = dialog.getController();
                finishDialogController.setFinishedBoats(finishedBoats);
                finishScreenDialog.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public void loadRace (
        Map<Integer, ClientYacht> participants, RaceXMLData raceData, RaceState raceState,
        ClientYacht player) {

        this.raceState = raceState;
        this.player = player;

        player.addPowerUpListener(this::displayPowerUpIcon);
        player.addPowerDownListener(this::removeIcon);

        gameView = new GameView3D();
        miniMap = new MiniMap(
            new ArrayList<>(raceData.getCompoundMarks().values()),
            raceData.getMarkSequence(), raceData.getCourseLimit(),
            new ArrayList<>(participants.values()), player
        );

        miniMapButton.setOnMouseClicked((event) -> {
            if (miniMapPane.visibleProperty().get()) {
                miniMapPane.setVisible(false);
                miniMapButton.setText("+");
            } else {
                miniMapPane.setVisible(true);
                miniMapButton.setText("—");
            }
        });

        Platform.runLater(() -> {
            contentStackPane.getChildren().add(0, gameView.getAssets());
            ((SubScene) gameView.getAssets()).widthProperty()
                .bind(ViewManager.getInstance().getStage().widthProperty());
            ((SubScene) gameView.getAssets()).heightProperty()
                .bind(ViewManager.getInstance().getStage().heightProperty());
            miniMapPane.getChildren().add(miniMap.getAssets());
        });
        gameView.setBoats(new ArrayList<>(participants.values()));
        gameView.updateBorder(raceData.getCourseLimit());
        gameView.updateTokens(raceData.getTokens());
        gameView.updateCourse(
            new ArrayList<>(raceData.getCompoundMarks().values()), raceData.getMarkSequence()
        );
        gameView.setBoatAsPlayer(player);

//        raceState.addCollisionListener(gameView::drawCollision);

        raceState.windDirectionProperty().addListener((obs, oldDirection, newDirection) -> {
            gameView.setWindDir(newDirection.doubleValue());
            Platform.runLater(() -> updateWindDirection(newDirection.doubleValue()));
        });
        raceState.windSpeedProperty().addListener((obs, oldSpeed, newSpeed) ->
            Platform.runLater(() -> updateWindSpeed(newSpeed.doubleValue()))
        );
        Platform.runLater(() -> {
            updateWindDirection(raceState.windDirectionProperty().doubleValue());
            updateWindSpeed(raceState.getWindSpeed());
        });
        gameView.setWindDir(raceState.windDirectionProperty().doubleValue());
        Platform.runLater(this::initializeUpdateTimer);

        Platform.runLater(() -> {
            //windCell.setCamera(gameView.getView().getCamera());

            initialiseWindArrow();
        });
    }

    /**
     * Displays the relevant icon, starts blinking it when it is close to turning off and then
     * switches it off after the tokens time out
     *
     * @param yacht The yacht only for which we are displaying the icon
     * @param tokenType The type of token, indicating what icon needs to be displayed
     */
    private void displayPowerUpIcon(ClientYacht yacht, TokenType tokenType) {
        if (yacht == player) {
            if (iconToDisplay != null) {
                iconToDisplay.setVisible(false);
            }

            switch (tokenType) {
                case BOOST:
                    iconToDisplay = velocityIcon;
                    break;
                case HANDLING:
                    iconToDisplay = handlingIcon;
                    break;
                case WIND_WALKER:
                    iconToDisplay = windWalkerIcon;
                    break;
                case BUMPER:
                    iconToDisplay = bumperIcon;
                    break;
                case RANDOM:
                    iconToDisplay = badRandomIcon;
                    break;
                default:
                    iconToDisplay = velocityIcon;
            }

            //Turn icon on
            iconToDisplay.setVisible(true);

            //Start blinking icon towards end
            if (blinkingTimer != null) {
                blinkingTimer.cancel();
            }
            blinkingTimer = new Timer("Blinking Timer");
            blinkingTimer.schedule(new TimerTask() {
                Boolean isVisible = true;

                @Override
                public void run() {
                    isVisible = !isVisible;
                    iconToDisplay.setVisible(isVisible);
                }
            }, (int) (tokenType.getTimeout() * ICON_BLINK_TIMEOUT_RATIO), ICON_BLINK_PERIOD);
        }
    }

    private void removeIcon(ClientYacht yacht) {
        if (yacht == player) {
            blinkingTimer.cancel();
            iconToDisplay.setVisible(false);
            iconToDisplay = null;
        }
    }

    /**
     * Initialises a timer which updates elements of the RaceView such as wind direction, yacht
     * orderings etc.. which are dependent on the info from the stream parser constantly.
     * Updates of each of these attributes are called ONCE EACH SECOND
     */
    private void initializeUpdateTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updatePosition());
                Platform.runLater(() -> updateBoatSpeed());
                Platform.runLater(() -> updateBoatHeading());
                Platform.runLater(() -> updateRaceTime());
            }
        }, 0, 1000);
    }

    /**
     * Updates the wind direction arrow and text as from info from the StreamParser
     * @param direction the from north angle of the wind.
     */
    private void updateWindDirection(double direction) {
        windDirectionLabel.setText(String.format("%.1f°", direction));
//        RotateTransition rt = new RotateTransition(Duration.millis(300), windImageView);
//        rt.setByAngle(direction - lastWindDirection);
//        rt.setCycleCount(3);
//        rt.setAutoReverse(true);
//        rt.play();
//        lastWindDirection = direction;
//        windImageView.setRotate(direction);
    }

    /**
     * Updates the speed of the wind as displayed by the info pane.
     * @param windSpeed Windspeed in knots.
     */
    private void updateWindSpeed(double windSpeed) {
        windSpeedLabel.setText(String.format("%.1f", windSpeed) + " Knots");
    }


    /**
     * Updates the clock for the race
     */
    private void updateRaceTime() {
        if (raceState.getTimeTillStart() <= 0L && !raceState.isRaceStarted()) {
            timerLabel.setText("Race Finished!");
        } else {
            timerLabel.setText(raceState.getRaceTimeStr());
        }
    }

    /**
     * Updates player position with ordinal number up to 23rd position.
     */
    private void updatePosition() {
        if (player.getPosition() == null) {
            positionLabel.setText("Position:\n-");
        } else {
            switch (player.getPosition()) {
                case 1:
                    positionLabel.setText("Position:\n1st");
                    break;
                case 2:
                    positionLabel.setText("Position:\n2nd");
                    break;
                case 3:
                    positionLabel.setText("Position:\n3rd");
                    break;
                case 21:
                    positionLabel.setText("Position:\n21st");
                    break;
                case 22:
                    positionLabel.setText("Position:\n22nd");
                    break;
                case 23:
                    positionLabel.setText("Position:\n23rd");
                    break;
                default:
                    positionLabel.setText("Position:\n" + player.getPosition() + "th");
            }
        }
    }

    /**
     * Updates boat speed value displayed on race view.
     */
    private void updateBoatSpeed() {
        boatSpeedLabel.setText("Boat Speed:\n" + String.valueOf(player.getCurrentVelocity()));
    }

    /**
     * Updates boat heading value displayed on race view.
     */
    private void updateBoatHeading() {
        boatHeadingLabel.setText(String.format("Boat Heading:\n%.1f°", player.getHeading()));
    }


    public void updateTokens(RaceXMLData raceData) {
        gameView.updateTokens(raceData.getTokens());
    }

    public ReadOnlyBooleanProperty getSendPressedProperty() {
        return chatSend.pressedProperty();
    }

    public boolean isChatInputFocused() {
        return chatInput.focusedProperty().getValue();
    }

    public String readChatInput() {
        String chat = chatInput.getText();
        chatInput.clear();
        contentStackPane.requestFocus();
        return chat;
    }

    public void updateChatHistory(Paint playerColour, String newMessage) {
        Platform.runLater(() -> chatHistory.addMessage(playerColour, newMessage));
    }

}