//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package seng302.visualiser.validators;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.beans.DefaultProperty;
import javafx.scene.control.TextInputControl;

@DefaultProperty("icon")
public class FieldLengthValidator extends ValidatorBase {

    Integer maxLength;

    public FieldLengthValidator(Integer length) {
        maxLength = length;
    }

    protected void eval() {
        if(this.srcControl.get() instanceof TextInputControl) {
            this.evalTextInputField();
        }
    }

    private void evalTextInputField() {
        TextInputControl textField = (TextInputControl)this.srcControl.get();
        if(textField.getLength() > maxLength) {
            this.hasErrors.set(true);
        } else {
            this.hasErrors.set(false);
        }
    }
}
