package seng302.visualiser.controllers.dialogs;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import seng302.model.ClientYacht;
import seng302.visualiser.controllers.ViewManager;

public class FinishDialogController implements Initializable {

    //--------FXML BEGIN--------//
    @FXML
    private Label raceFinishLabel;
    @FXML
    private JFXListView<Label> finishersList;
    @FXML
    private JFXButton playAgain;
    //---------FXML END---------//

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playAgain.setOnAction(event -> {
            System.out.println("CALLED HERE");
            ViewManager.getInstance().goToStartView();
        });
    }

    public void setFinishedBoats(ArrayList<ClientYacht> finishedBoats) {
        finishersList.getItems().clear();
        for (int i = 0; i < finishedBoats.size(); i++) {
            finishersList.getItems().add(new Label(Integer.toString(i+1) +".  " + finishedBoats.get(i).getBoatName()));
        }
    }
}
