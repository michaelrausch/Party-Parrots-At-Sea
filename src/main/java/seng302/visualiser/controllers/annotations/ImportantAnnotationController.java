package seng302.visualiser.controllers.annotations;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

;

public class ImportantAnnotationController implements Initializable {

    /*
     * JavaFX Outlets
     */
    @FXML
    private CheckBox boatWakeSelect;

    @FXML
    private CheckBox boatSpeedSelect;

    @FXML
    private CheckBox boatTrackSelect;

    @FXML
    private CheckBox boatNameSelect;

    @FXML
    private CheckBox boatEstTimeToNextMarkSelect;

    @FXML
    private CheckBox boatElapsedTimeSelect;

    @FXML
    private AnchorPane annotationSelectWindow;

    @FXML
    private Button closeButton;

    private ImportantAnnotationDelegate delegate;
    private ImportantAnnotationsState importantAnnotationsState;
    private Stage stage;

    public ImportantAnnotationController(ImportantAnnotationDelegate delegate, Stage stage) {
        this.delegate = delegate;
        importantAnnotationsState = new ImportantAnnotationsState();
        this.stage = stage;
    }

    /**
     * Sets whether or not an annotation is considered important, then
     * sends an update to the delegate
     *
     * @param annotation The annotation
     * @param isSet True if annotation is important
     */
    private void setAnnotation(Annotation annotation, Boolean isSet) {
        importantAnnotationsState.setAnnotationState(annotation, isSet);
        sendUpdate();
    }

    /**
     * Sends an update to the delegate when the important
     * annotations have changed
     */
    private void sendUpdate() {
        this.delegate.importantAnnotationsChanged(importantAnnotationsState);
    }

    /**
     * Load the current state of the 'important annotations'
     *
     * @param currentState hashmap containing the states of each annotation
     */
    public void loadState(ImportantAnnotationsState currentState) {
        this.importantAnnotationsState = currentState;

        // Initialise checkboxes
        for (Annotation annotation : importantAnnotationsState.getAnnotations()) {
            switch (annotation) {
                case WAKE:
                    boatWakeSelect
                        .setSelected(importantAnnotationsState.getAnnotationState(annotation));
                    break;

                case SPEED:
                    boatSpeedSelect
                        .setSelected(importantAnnotationsState.getAnnotationState(annotation));
                    break;

                case TRACK:
                    boatTrackSelect
                        .setSelected(importantAnnotationsState.getAnnotationState(annotation));
                    break;

                case NAME:
                    boatNameSelect
                        .setSelected(importantAnnotationsState.getAnnotationState(annotation));
                    break;

                case ESTTIMETONEXTMARK:
                    boatEstTimeToNextMarkSelect
                        .setSelected(importantAnnotationsState.getAnnotationState(annotation));
                    break;

                case LEGTIME:
                    boatElapsedTimeSelect
                        .setSelected(importantAnnotationsState.getAnnotationState(annotation));

                default:
                    break;
            }
        }
    }

    /**
     * View did load
     *
     * @param location .
     * @param resources .
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boatWakeSelect
            .setOnAction(event -> setAnnotation(Annotation.WAKE, boatWakeSelect.isSelected()));
        boatSpeedSelect
            .setOnAction(event -> setAnnotation(Annotation.SPEED, boatSpeedSelect.isSelected()));
        boatTrackSelect
            .setOnAction(event -> setAnnotation(Annotation.TRACK, boatTrackSelect.isSelected()));
        boatNameSelect
            .setOnAction(event -> setAnnotation(Annotation.NAME, boatNameSelect.isSelected()));
        boatEstTimeToNextMarkSelect.setOnAction(event -> setAnnotation(Annotation.ESTTIMETONEXTMARK,
            boatEstTimeToNextMarkSelect.isSelected()));
        boatElapsedTimeSelect.setOnAction(
            event -> setAnnotation(Annotation.LEGTIME, boatElapsedTimeSelect.isSelected()));
        // TODO: 26/07/17 cir27 - Create a more robust fix for this when the annotation for the game are decided upon.
        boatEstTimeToNextMarkSelect.setVisible(false);
        boatEstTimeToNextMarkSelect.setDisable(true);
        boatElapsedTimeSelect.setVisible(false);
        boatElapsedTimeSelect.setDisable(true);
        closeButton.setOnAction(event -> stage.close());
    }
}
