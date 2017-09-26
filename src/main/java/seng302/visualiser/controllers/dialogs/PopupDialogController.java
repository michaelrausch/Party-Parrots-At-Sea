package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class PopupDialogController implements Initializable {

    @FXML
    private Label headerLabel;
    @FXML
    private Label contentLabel;
    @FXML
    private Label closeLabel;
    @FXML
    private JFXButton optionButton;

    @FXML
    private JFXDialog popupDialog;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setContent(String content) {
        this.contentLabel.setText(content);
    }

    public void setHeader(String header) {
        this.headerLabel.setText(header);
    }

    public void setOptionButton(JFXButton jfxButton) {
        this.optionButton = jfxButton;
    }

    public void setOptionButtonText(String text) {
        this.optionButton.setText(text);
    }

    public void setOptionButtonEventHandler(EventHandler<? super MouseEvent> eventHandler) {
        this.optionButton.setOnMouseClicked(eventHandler);
    }

    public void setPopupDialog(JFXDialog popupDialog) {
        this.popupDialog = popupDialog;
        this.closeLabel.setOnMouseClicked(event -> this.popupDialog.close());
    }
}
