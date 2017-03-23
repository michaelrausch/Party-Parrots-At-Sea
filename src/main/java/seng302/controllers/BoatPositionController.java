package seng302.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import seng302.models.Boat;
import seng302.models.Event;

import java.util.*;

/**
 * Created by ptg19 on 23/03/17.
 */
public class BoatPositionController {
    @FXML
    private VBox positionVbox;

    private ArrayList<Boat> boatOrder = new ArrayList<>();

    public void initialize() {
    }

    public void handleEvent(Event event){
        Boat boat = event.getBoat();
        boatOrder.remove(boat);
        boat.setMarkLastPast(event.getMarkPosInRace());
        boatOrder.add(boat);
        boatOrder.sort(new Comparator<Boat>() {
            @Override
            public int compare(Boat b1, Boat b2) {
                return b2.getMarkLastPast() - b1.getMarkLastPast();
            }
        });
    }
}
