package seng302.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

/**
 * Created by ptg19 on 20/03/17.
 */
public class Controller {
    @FXML private AnchorPane window;
    @FXML private Parent raceView;
    @FXML private RaceController raceViewController;

    //^ this is automatic fxml linking based off http://blog.buildpath.de/fxml-composition-how-to-get-the-controller-of-an-included-fxml-view-nested-controllers/
    // From googling it's probably better to just add a child however you did that in your 301 michael, it kinda depends on how we are going to
    // make an event for changing the screen.
}
