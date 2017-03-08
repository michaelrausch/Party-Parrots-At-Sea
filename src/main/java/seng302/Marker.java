package seng302;

import java.util.ArrayList;

class Marker {
    private String name;
    private ArrayList<Boat> boatOrder;

    public Marker(String name) {
        this.name = name;
        this.boatOrder = new ArrayList<Boat>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addBoat(Boat boat) {
        this.boatOrder.add(boat);
    }

    public Boat[] getBoats() {
        return this.boatOrder.toArray(new Boat[this.boatOrder.size()]);
    }
}