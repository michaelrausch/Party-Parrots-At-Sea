package seng302.visualiser.validators;

import com.jfoenix.validation.base.ValidatorBase;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;

@DefaultProperty("icon")
public class NumberRangeValidator extends ValidatorBase {

    Integer lowerLimit;
    Integer upperLimit;

    public NumberRangeValidator(Integer lower, Integer upper) {
        lowerLimit = lower;
        upperLimit = upper;
    }


    protected void eval() {
        if(this.srcControl.get() instanceof TextInputControl) {
            this.evalTextInputField();
        }
    }

    protected void evalTextInputField() {
        TextInputControl textField = (TextInputControl)this.srcControl.get();
        try {
            Integer portNum = Integer.parseInt(textField.getText());
            if (lowerLimit <= portNum && portNum <= upperLimit) {
                this.hasErrors.set(false);
            } else {
                this.hasErrors.set(true);
            }
        } catch (NumberFormatException e) {
            this.hasErrors.set(true);
        }
    }
}
