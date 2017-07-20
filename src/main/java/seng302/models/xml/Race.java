package seng302.models.xml;

import seng302.models.Yacht;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Race {
    private List<Yacht> yachts;

    public Race(){
        yachts = new ArrayList<>();
    }

    public void addBoat(Yacht yacht){
        yachts.add(yacht);
    }

    public List<Yacht> getBoats(){
        return Collections.unmodifiableList(yachts);
    }
}
