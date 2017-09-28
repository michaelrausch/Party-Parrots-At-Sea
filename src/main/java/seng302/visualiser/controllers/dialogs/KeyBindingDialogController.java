package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import seng302.model.GameKeyBind;
import seng302.model.KeyAction;
import seng302.visualiser.GameClient;
import seng302.visualiser.controllers.ViewManager;

public class KeyBindingDialogController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private Label keyBindingDialogHeader;
    @FXML
    private Label closeLabel;
    @FXML
    private JFXButton zoomInBtn;
    @FXML
    private JFXButton zoomOutBtn;
    @FXML
    private JFXButton vmgBtn;
    @FXML
    private JFXButton sailInOutBtn;
    @FXML
    private JFXButton tackGybeBtn;
    @FXML
    private JFXButton upwindBtn;
    @FXML
    private JFXButton downwindBtn;
    @FXML
    private JFXButton resetBtn;
    @FXML
    private JFXButton confirmBtn;
    @FXML
    private Label upwindLabel;
    @FXML
    private Label downwindLabel;
    @FXML
    private JFXToggleButton turningToggle;
    @FXML
    private JFXButton viewButton;
    @FXML
    private JFXButton rightButton;
    @FXML
    private JFXButton leftButton;
    @FXML
    private JFXButton forwardButton;
    @FXML
    private JFXButton backwardButton;
    //---------FXML END---------//

    private GameKeyBind gameKeyBind;
    private List<JFXButton> buttons = new ArrayList<>();
    private Map<Button, KeyAction> buttonActionMap;
    private GameClient gameClient; // to send turning mode packet

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameKeyBind = GameKeyBind.getInstance();
        buttons = new ArrayList<>();
        Collections.addAll(buttons,
            zoomInBtn, zoomOutBtn, vmgBtn, sailInOutBtn, tackGybeBtn, upwindBtn, downwindBtn,
            viewButton, rightButton, leftButton, forwardButton, backwardButton);
        bindButtonWithAction();
        loadKeyBind();

        buttons.forEach(button -> {
            button.setOnMouseEntered(event -> mouseEnter(button));
            button.setOnMousePressed(event -> buttonPressed(button));
            button.setOnMouseExited(event -> mouseExit(button));
            button.setOnKeyPressed(event -> keyPressed(event, button));
        });

        turningToggle.setOnMouseClicked(event -> toggleTurningMode());

        resetBtn.setOnMouseClicked(event -> {
            gameKeyBind.setToDefault();
            loadKeyBind();
            showSnackBar("All keys reset!", false);
        });

        closeLabel.setOnMouseClicked(event -> ViewManager.getInstance().closeKeyBindingDialog());
        confirmBtn.setOnMouseClicked(event -> ViewManager.getInstance().closeKeyBindingDialog());
    }

    /**
     * Set buttons' label according to GameKeyBind settings
     */
    private void loadKeyBind() {
        buttons.forEach(
            button -> button
                .setText(gameKeyBind.getKeyCode(buttonActionMap.get(button)).getName()));
        turningToggle.setSelected(gameKeyBind.isContinuouslyTurning());
        if (gameKeyBind.isContinuouslyTurning()) {
            upwindLabel.setText("ClOCKWISE TURNING");
            downwindLabel.setText("ANTICLOCKWISE TURNING");
        } else {
            upwindLabel.setText("UPWIND");
            downwindLabel.setText("DOWNWIND");
        }
    }

    /**
     * Bind buttons with specific action in a map.
     */
    private void bindButtonWithAction() {
        buttonActionMap = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            buttonActionMap.put(buttons.get(i), KeyAction.getType(i + 1));
        }
    }

    /**
     * Prompt success / failure message for reassigning key action
     */
    private void showSnackBar(String message, Boolean isWarning) {
        ViewManager.getInstance().showSnackbar(message, isWarning);
    }

    /**
     * When a mouse enters the button, the color and font size should change to highlight
     * @param button
     */
    private void mouseEnter(Button button) {
        button.setStyle(""
            + "-fx-background-color: -fx-pp-theme-color;"
            + "-fx-text-fill: -fx-pp-front-color;"
            + "-fx-font-size: 15;");
    }

    /**
     * Prompt "press key..." to inform users assign a new key bind by pressing a key
     * @param button
     */
    private void buttonPressed(Button button) {
        button.setText("PRESS KEY...");
    }


    /**
     * When mouse leaves the button, return the button to the normal state in terms of text,
     * color and font size
     * @param button
     */
    private void mouseExit(Button button) {
        button.setText(GameKeyBind.getInstance().getKeyCode(buttonActionMap.get(button)).getName());
        button.setStyle(""
            + "-fx-background-color: -fx-pp-front-color; "
            + "-fx-text-fill: -fx-pp-theme-color; "
            + "-fx-font-size: 13;");
        keyBindingDialogHeader.requestFocus();
    }

    /**
     * When a key is pressed, check if the new binding conflicts to any existed settings, if not
     * assign the selected action with the new key binding to GameKeyBind.
     * @param event
     * @param button
     */
    private void keyPressed(KeyEvent event, Button button) {
        event.consume();
        KeyAction buttonAction = buttonActionMap.get(button);
        if (gameKeyBind.bindKeyToAction(event.getCode(), buttonAction)) {
            showSnackBar(button.getId() + " is set to " + event.getCode().getName(), false);
            button.setText(gameKeyBind.getKeyCode(buttonAction).getName());
        } else {
            loadKeyBind();
            showSnackBar(event.getCode().getName() + " is already in use", true);
        }
    }

    /**
     * When the turning mode is toggled, update gameKeyBind and send out packet to notify the server
     */
    private void toggleTurningMode() {
        gameKeyBind.toggleTurningMode();
        gameClient.sendToggleTurningModePacket();
        loadKeyBind();
    }

    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }

}
