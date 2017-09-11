package seng302.visualiser.validators;

import com.jfoenix.validation.base.ValidatorBase;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;

@DefaultProperty("icon")
public class HostNameFieldValidator extends ValidatorBase {


    public HostNameFieldValidator() {
    }


    protected void eval() {
        if(this.srcControl.get() instanceof TextInputControl) {
            this.evalTextInputField();
        }
    }

    protected void evalTextInputField() {
        TextInputControl textField = (TextInputControl)this.srcControl.get();
        try{
            InetAddress.getByName(textField.getText());
            this.hasErrors.set(false);
        } catch (UnknownHostException e) {
            this.hasErrors.set(true);
        }
    }
}
