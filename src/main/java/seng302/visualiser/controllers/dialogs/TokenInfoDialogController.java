package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import seng302.utilities.Sounds;
import seng302.visualiser.controllers.LobbyController;

/**
 * Created by wmu16 on 28/09/17.
 */
public class TokenInfoDialogController implements Initializable {

    @FXML
    private Label headerLabel;
    @FXML
    private TextArea contentText;
    @FXML
    private Pane tokenPane;
    @FXML
    private Button optionButton;

    private LobbyController lobbyController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        optionButton.setOnMouseReleased(event -> {
            Sounds.playButtonClick();
            lobbyController.closeTokenInfoDialog();
        });

        contentText.setEditable(false);

    }


    public void setContent(String content) {
        contentText.setText(content);
    }

    public void setHeader(String header) {
        this.headerLabel.setText(header);
    }

    public void setToken(Group token) {
        tokenPane.getChildren().clear();

        token.getTransforms().addAll(
            new Translate(138 / 2, 138 / 2, 0),
            new Scale(20, 20, 20));

        tokenPane.getChildren().add(token);
    }

    public void setParentController(LobbyController lobbyController) {
        this.lobbyController = lobbyController;
    }


}
