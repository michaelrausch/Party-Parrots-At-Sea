package seng302.models;

import java.util.ArrayList;

/**
* Represents the marker at the beginning of a leg
*/
public class Marker{
    private String name;
    private ArrayList<Boat> boatOrder;

	/**
	* Represents the marker at the beginning of a leg
	*
	* @param name, the name of the marker
	*/
	public Marker(String name){
		this.name = name;
		this.boatOrder = new ArrayList<Boat>();
	}

	/**
	* Set the name of the marker
	*
	* @param name, the name of the marker
	*/
	public void setName(String name){
		this.name = name;
	}

	/**
	* Get the name of the marker
	*
	* @return the name of the marker
	*/
	public String getName(){
		return this.name;
	}

	/**
	* Add a boat as they pass the marker
	*
	* @param boat, the boat that passed the marker
	*/
	public void addBoat(Boat boat){
		this.boatOrder.add(boat);
	}

	/**
	* Get a list of boats in the order they passed the marker
	*
	* @return An array of boats in the order they passed the marker
	*/
	public Boat[] getBoats(){
		return this.boatOrder.toArray(new Boat[this.boatOrder.size()]);
	}
}