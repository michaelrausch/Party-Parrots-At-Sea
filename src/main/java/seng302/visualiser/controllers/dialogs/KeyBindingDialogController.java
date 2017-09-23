package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
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
import seng302.visualiser.controllers.ViewManager;

public class KeyBindingDialogController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private JFXDialogLayout keyBindDialog;
    @FXML
    private Label keyBindingDialogHeader;
    @FXML
    private JFXButton zoomInbtn;
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
    private JFXToggleButton turningToggle;
    //---------FXML END---------//

    private GameKeyBind gameKeyBind;
    private List<JFXButton> buttons = new ArrayList<>();
    private Map<Button, KeyAction> buttonActionMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameKeyBind = GameKeyBind.getInstance();
        buttons = new ArrayList<>();
        Collections.addAll(buttons,
            zoomInbtn, zoomOutBtn, vmgBtn, sailInOutBtn, tackGybeBtn, upwindBtn, downwindBtn);
        bindButtonWithAction();
        loadKeyBind();

        buttons.forEach(button -> {
            button.setOnMouseEntered(event -> mouseEnter(button));
            button.setOnMousePressed(event -> buttonPressed(button));
            button.setOnMouseExited(event -> mouseExit(button));
            button.setOnKeyPressed(event -> keyPressed(event, button));
        });

        resetBtn.setOnMouseClicked(event -> {
            gameKeyBind.setToDefault();
            loadKeyBind();
        });
    }

    /**
     * Set buttons' label according to GameKeyBind settings
     */
    private void loadKeyBind() {
        buttons.forEach(
            button -> button
                .setText(gameKeyBind.getKeyCode(buttonActionMap.get(button)).getName()));
    }

    private void bindButtonWithAction() {
        buttonActionMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            buttonActionMap.put(buttons.get(i), KeyAction.getType(i + 1));
        }
    }

    private void showSnackBar(String message) {
        ViewManager.getInstance().showSnackbar(message);
    }

    private void mouseEnter(Button button) {
        button.setStyle(""
            + "-fx-background-color: -fx-pp-theme-color;"
            + "-fx-text-fill: -fx-pp-front-color;"
            + "-fx-font-size: 15;");
    }

    private void buttonPressed(Button button) {
        button.setText("PRESS KEY...");
    }

    private void mouseExit(Button button) {
        button.setText(GameKeyBind.getInstance().getKeyCode(buttonActionMap.get(button)).getName());
        button.setStyle(""
            + "-fx-background-color: -fx-pp-front-color; "
            + "-fx-text-fill: -fx-pp-theme-color; "
            + "-fx-font-size: 13;");
    }

    private void keyPressed(KeyEvent event, Button button) {
        KeyAction buttonAction = buttonActionMap.get(button);
        if (gameKeyBind.bindKeyToAction(event.getCode(), buttonAction)) {
            showSnackBar(button.getId() + " is set to " + event.getCode().getName());
            button.setText(gameKeyBind.getKeyCode(buttonAction).getName());
        } else {
            showSnackBar(event.getCode().getName() + " is already in use");
        }
        event.consume();
    }
}
