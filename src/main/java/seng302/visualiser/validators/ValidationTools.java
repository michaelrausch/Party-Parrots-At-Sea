package seng302.visualiser.validators;


import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;

public class ValidationTools {
    /**
     *
     * @return
     */
    public static Boolean validateTextField(JFXTextField textField) {
        textField.validate();
        for (ValidatorBase validator : textField.getValidators()) {
            if (validator.getHasErrors()) {
                return false;
            }
        }
        return true;
    }
}
