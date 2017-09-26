package seng302.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.input.KeyCode;

public class GameKeyBind {

    private static GameKeyBind instance;
    private Map<KeyCode, KeyAction> keyToActionMap;
    private Map<KeyAction, KeyCode> actionToKeyMap;
    private Boolean continuouslyTurning;


    private GameKeyBind() {
        setToDefault();
    }

    public void setToDefault() {
        actionToKeyMap = new HashMap<>();
        keyToActionMap = new HashMap<>();
        continuouslyTurning = false;
        // default key bindings
        ArrayList<KeyCode> keys = new ArrayList<>();
        keys.add(KeyCode.Z);
        keys.add(KeyCode.X);
        keys.add(KeyCode.SPACE);
        keys.add(KeyCode.SHIFT);
        keys.add(KeyCode.ENTER);
        keys.add(KeyCode.PAGE_UP);
        keys.add(KeyCode.PAGE_DOWN);
        keys.add(KeyCode.F1);
        keys.add(KeyCode.D);
        keys.add(KeyCode.A);
        keys.add(KeyCode.W);
        keys.add(KeyCode.S);
        for (int i = 0; i < 12; i++) {
            actionToKeyMap.put(KeyAction.getType(i + 1), keys.get(i));
            keyToActionMap.put(keys.get(i), KeyAction.getType(i + 1));
        }
    }

    public static GameKeyBind getInstance() {
        if (instance == null) {
            instance = new GameKeyBind();
        }
        return instance;
    }

    public KeyCode getKeyCode(KeyAction keyAction) {
        return instance.actionToKeyMap.get(keyAction);
    }

    /**
     * Binds a key to a key action
     *
     * @return true if successfully bind
     */
    public boolean bindKeyToAction(KeyCode keyCode, KeyAction keyAction) {
        if (instance.keyToActionMap.containsKey(keyCode)) {
            // if the key has been bound to other action, return false
            return false;
        } else {
            instance.keyToActionMap.put(keyCode, keyAction); // add key -> action
            KeyCode oldKeyCode = instance.actionToKeyMap
                .get(keyAction); // get old key for the action
            instance.keyToActionMap.remove(oldKeyCode); // remove the old key -> action
            instance.actionToKeyMap
                .replace(keyAction, keyCode); // replace the old key by the newer one
            return true;
        }
    }

    public void toggleTurningMode() {
        continuouslyTurning = !continuouslyTurning;
    }

    public Boolean isContinuouslyTurning() {
        return continuouslyTurning;
    }

}
