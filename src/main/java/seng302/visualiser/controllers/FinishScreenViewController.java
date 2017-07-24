package seng302.visualiser.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import seng302.model.Boat;
import seng302.model.stream.parsers.StreamParser;
import seng302.model.stream.parsers.xml.XMLParser.RaceXMLObject.Participant;

public class FinishScreenViewController implements Initializable {

    @FXML
    private GridPane finishScreenGridPane;
    @FXML
    private TableView<Boat> finishOrderTable;
    @FXML
    private TableColumn<Boat, String> posCol;
    @FXML
    private TableColumn<Boat, String> boatNameCol;
    @FXML
    private TableColumn<Boat, String> shortNameCol;
    @FXML
    private TableColumn<Boat, String> countryCol;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        finishScreenGridPane.getStylesheets()
            .add(getClass().getResource("/css/master.css").toString());
        finishOrderTable.getStylesheets().add(getClass().getResource("/css/master.css").toString());

        // set up data for table
        ObservableList<Boat> data = FXCollections.observableArrayList();
        finishOrderTable.setItems(data);

        // setting table col data
        posCol.setCellValueFactory(
            new PropertyValueFactory<>("position")
        );
        boatNameCol.setCellValueFactory(
            new PropertyValueFactory<>("boatName")
        );
        shortNameCol.setCellValueFactory(
            new PropertyValueFactory<>("shortName")
        );
        countryCol.setCellValueFactory(
            new PropertyValueFactory<>("country")
        );

        // check if the boat is racing
        ArrayList<Participant> participants = StreamParser.getXmlObject().getRaceXML()
            .getParticipants();
        ArrayList<Integer> participantIDs = new ArrayList<>();
        for (Participant p : participants) {
            participantIDs.add(p.getsourceID());
        }

        // add data to table
        for (Boat boat : StreamParser.getBoatsPos().values()) {
            if (participantIDs.contains(boat.getSourceID())) {
                data.add(boat);
            }
        }
        finishOrderTable.refresh();
    }

    private void setContentPane(String jfxUrl) {
        try {
            // get the main controller anchor pane (FinishView -> MainView)
            AnchorPane contentPane = (AnchorPane) finishScreenGridPane.getParent();
            contentPane.getChildren().removeAll();
            contentPane.getChildren().clear();
            contentPane.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            contentPane.getChildren()
                .addAll((Pane) FXMLLoader.load(getClass().getResource(jfxUrl)));
        } catch (javafx.fxml.LoadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToStartScreenView() {
        setContentPane("/views/StartScreenView.fxml");
    }
}
