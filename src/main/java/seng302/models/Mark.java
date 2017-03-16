package seng302.models;

import java.util.ArrayList;

/**
* Represents the marker at the beginning of a leg
*/
public class Mark {
	private double lat;
	private double lon;
    private String name;
    private ArrayList<Boat> boatOrder;

	/**
	* Represents the marker at the beginning of a leg
	*
	* @param name, the name of the marker
	*/
	public Mark(String name, double lat, double lon){
		this.name = name;
		this.lat = lat;
		this.lon = lon;
		this.boatOrder = new ArrayList<Boat>();
	}

	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return this.name;
	}
	public void addBoat(Boat boat){
		this.boatOrder.add(boat);
	}
	public Boat[] getBoats(){
		return this.boatOrder.toArray(new Boat[this.boatOrder.size()]);
	}
}