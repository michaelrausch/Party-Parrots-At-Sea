package seng302.visualiser.controllers.cells;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import seng302.visualiser.fxObjects.assets_3D.Model;
import seng302.visualiser.fxObjects.assets_3D.ModelFactory;

public class WindCell {

    //--------FXML BEGIN--------//
    @FXML
    private Pane windPane;
    //---------FXML END---------//

    /**
     * Initialise WindCell fxml and load 3D wind arrow into a group.
     */
    public void initialize() {
        Group group = new Group();
        windPane.getChildren().add(group);
        Model windArrowModel = ModelFactory.makeWindArrow();
        group.getChildren().add(windArrowModel.getAssets());
    }
}
