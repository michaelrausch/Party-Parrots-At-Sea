package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.*;
import seng302.model.GameClientAction;

public class KeyBindingDialogController implements Initializable {

    //--------FXML BEGIN--------//
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
    //---------FXML END---------//

    private Map<JFXButton, KeyCode> keys;
    private List<JFXButton> buttons = new ArrayList<>();
    private Map<GameClientAction, KeyCode> keyBind;
    private LinkedHashMap<JFXButton, GameClientAction> buttonAndGameClientActionMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * HAOMING HELP!!! CHANGE FUNCTION NAME PLS :))
     *
     * Takes in a map from GameClient and initialise button mapping to the keys.
     *
     * @param keyBind a map with GameClientAction and KeyCode pair to be used in GameClient.
     */
    public void init(Map<GameClientAction, KeyCode> keyBind) {
        this.keyBind = keyBind;

        buttons = new ArrayList<>();
        Collections
            .addAll(buttons, zoomInbtn, zoomOutBtn, vmgBtn, sailInOutBtn, tackGybeBtn, upwindBtn,
                downwindBtn);

        initializeKeys();
        initializeButtons();
    }

    /**
     * Initialise default button-keybinding pair if not exist, else rebind the existing keybinding
     * to the new button which is created when javafx reinitialise a new controller.
     */
    private void initializeKeys() {
        initButtonAndGameClientActionMap();
        initializeDefaultKeys();
    }

    /**
     * HAOMING CHANGE FUNCTION NAME HERE TOO :) OR BETTER, REMOVE THIS FUNCTION
     *
     * Link buttons and the GameClientAction to be used in accessing keys.
     */
    private void initButtonAndGameClientActionMap() {
        buttonAndGameClientActionMap = new LinkedHashMap<>();
        buttonAndGameClientActionMap.put(zoomInbtn, GameClientAction.ZOOM_IN);
        buttonAndGameClientActionMap.put(zoomOutBtn, GameClientAction.ZOOM_OUT);
        buttonAndGameClientActionMap.put(vmgBtn, GameClientAction.VMG);
        buttonAndGameClientActionMap.put(sailInOutBtn, GameClientAction.SAILS_STATE);
        buttonAndGameClientActionMap.put(tackGybeBtn, GameClientAction.TACK_GYBE);
        buttonAndGameClientActionMap.put(upwindBtn, GameClientAction.UPWIND);
        buttonAndGameClientActionMap.put(downwindBtn, GameClientAction.DOWNWIND);
    }

    /**
     * Initialise default keybinding to a button.
     */
    private void initializeDefaultKeys() {
        keys = new LinkedHashMap<>();
        keys.put(zoomInbtn, keyBind.get(GameClientAction.ZOOM_IN));
        keys.put(zoomOutBtn, keyBind.get(GameClientAction.ZOOM_OUT));
        keys.put(vmgBtn, keyBind.get(GameClientAction.VMG));
        keys.put(sailInOutBtn, keyBind.get(GameClientAction.SAILS_STATE));
        keys.put(tackGybeBtn, keyBind.get(GameClientAction.TACK_GYBE));
        keys.put(upwindBtn, keyBind.get(GameClientAction.UPWIND));
        keys.put(downwindBtn, keyBind.get(GameClientAction.DOWNWIND));
    }

    /**
     * Change button text to match current keybinding. Adds focusedProperty and keyPressed listener
     * to each buttons.
     */
    private void initializeButtons() {
        for (JFXButton jfxButton : buttons) {
            if (keys.get(jfxButton) != null) {
                jfxButton.setText(keys.get(jfxButton).getName());
            } else {
                jfxButton.setText("");
            }
            jfxButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
                jfxButton.setOnKeyPressed(event -> {
                    event.consume();
                    doSomething(event, jfxButton);
                });
            });
        }
    }

    /**
     * HAOMING PLEASE CHANGE THIS DOCSTRING AND THE FUNCTION NAME!!!
     *
     * @param event BLAH
     * @param button BLAH
     */
    private void doSomething(KeyEvent event, JFXButton button) {
        if (keys.containsValue(event.getCode())) {
            keys.replace(button, null);
            keyBind.replace(buttonAndGameClientActionMap.get(button), null);
            button.setText("");
        } else {
            keys.replace(button, event.getCode());
            keyBind.replace(buttonAndGameClientActionMap.get(button), event.getCode());
            button.setText(event.getCode().getName());
        }
    }
}
