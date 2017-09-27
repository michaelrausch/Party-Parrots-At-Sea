package seng302.visualiser.controllers.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import seng302.utilities.Sounds;
import seng302.visualiser.controllers.LobbyController;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;
import seng302.visualiser.fxObjects.assets_3D.ModelType;

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

    public void setToken(ModelType token) {
        tokenPane.getChildren().clear();

        Group tokenObject = ModelFactory.importModel(token).getAssets();

        tokenObject.getTransforms().addAll(
            new Translate(138 / 2, 138 / 2, 0),
            new Scale(20, 20, 20));

        if (token == ModelType.WIND_WALKER_PICKUP) {
            tokenObject.getTransforms().addAll(
                new Rotate(-70, new Point3D(1, 0, 0)),
                new Translate(0, 2, 0)
            );
        } else if (token == ModelType.RANDOM_PICKUP) {
            tokenObject.getTransforms().addAll(
                new Rotate(-90, new Point3D(1, 0, 0)),
                new Translate(0, 0, 1)
            );
        }

        tokenPane.getChildren().add(tokenObject);
    }

    public void setParentController(LobbyController lobbyController) {
        this.lobbyController = lobbyController;
    }


}
