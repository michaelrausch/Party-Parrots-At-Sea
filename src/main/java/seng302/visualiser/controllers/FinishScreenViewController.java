package seng302.visualiser.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import seng302.model.Yacht;
import seng302.model.stream.parser.StreamParser;

public class FinishScreenViewController implements Initializable {

    @FXML
    private GridPane finishScreenGridPane;
    @FXML
    private TableView<Yacht> finishOrderTable;
    @FXML
    private TableColumn<Yacht, String> posCol;
    @FXML
    private TableColumn<Yacht, String> boatNameCol;
    @FXML
    private TableColumn<Yacht, String> shortNameCol;
    @FXML
    private TableColumn<Yacht, String> countryCol;

    ObservableList<Yacht> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        finishScreenGridPane.getStylesheets()
            .add(getClass().getResource("/css/master.css").toString());
        finishOrderTable.getStylesheets().add(getClass().getResource("/css/master.css").toString());

        // set up data for table
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
        finishOrderTable.refresh();
    }

    public void setFinishers (List<Yacht> participants) {
        List<Yacht> sorted = new ArrayList<>(participants);
        sorted.sort(Comparator.comparingInt(Yacht::getPositionInteger));
        finishOrderTable.getItems().setAll(sorted);
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
