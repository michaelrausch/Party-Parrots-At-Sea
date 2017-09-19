package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.*;

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

    private static Map<JFXButton, KeyCode> keys;  // static button and saved keybinding pair
    private List<JFXButton> buttons = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        if (keys == null) {
            initializeDefaultKeys();
        } else {
            Map<JFXButton, KeyCode> tempKey = new LinkedHashMap<>();
            int buttonIndex = 0;
            for (JFXButton jfxButton : keys.keySet()) {
                tempKey.put(buttons.get(buttonIndex), keys.get(jfxButton));
                buttonIndex++;
            }
            keys = tempKey;
        }
    }

    /**
     * Initialise default keybinding to a button.
     */
    private void initializeDefaultKeys() {
        keys = new LinkedHashMap<>();
        keys.put(zoomInbtn, KeyCode.Z);
        keys.put(zoomOutBtn, KeyCode.X);
        keys.put(vmgBtn, KeyCode.SPACE);
        keys.put(sailInOutBtn, KeyCode.SHIFT);
        keys.put(tackGybeBtn, KeyCode.ENTER);
        keys.put(upwindBtn, KeyCode.PAGE_UP);
        keys.put(downwindBtn, KeyCode.PAGE_DOWN);
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
            button.setText("");
        } else {
            keys.replace(button, event.getCode());
            button.setText(event.getCode().getName());
        }
    }
}
