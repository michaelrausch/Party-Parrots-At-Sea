package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXColorPicker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class BoatCustomizeController implements Initializable{

    @FXML
    private JFXColorPicker colorPicker;

    @FXML
    void colorChanged(ActionEvent event) {
        Color color = colorPicker.getValue();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorPicker.setValue(Color.BISQUE);
    }
}
