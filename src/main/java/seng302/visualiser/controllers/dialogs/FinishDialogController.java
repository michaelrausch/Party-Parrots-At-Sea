package seng302.visualiser.controllers.dialogs;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import java.awt.Label;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import seng302.visualiser.controllers.ViewManager;

public class FinishDialogController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private Label raceFinishLabel;
    @FXML
    private JFXListView finishersList;
    @FXML
    private JFXButton playAgain;
    //---------FXML END---------//

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playAgain.setOnAction(event -> ViewManager.getInstance().goToStartView());
    }
}
