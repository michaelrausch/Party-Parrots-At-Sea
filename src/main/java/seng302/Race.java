package seng302;

import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.List;


public class Race {
	private ArrayList<Boat> boats;
	private ArrayList<Leg> legs;
	private int numberOfBoats = 0;
	private long startTime = 0;
	private int timeScale = 1;

	public Race(){
		boats = new ArrayList<Boat>();
		legs = new ArrayList<Leg>();
	}

	/*
		Add a boat to the race
		@param boat, the boat to add
	*/
	public void addBoat(Boat boat){
		boats.add(boat);
		numberOfBoats += 1;
	}

	/*
		Returns a list of boats in a random order

		@returns a list of boats
	*/
	public Boat[] getShuffledBoats(){
		// Shuffle the list of boats
		long seed = System.nanoTime();
		Collections.shuffle(this.boats, new Random(seed));

		return boats.toArray(new Boat[boats.size()]);
	}

	/*
		Returns a list of boats in the order that they
		finished the race (position 0 is first place)

		@returns a list of boats
	*/
	public Boat[] getFinishedBoats(){
		return getShuffledBoats();
	}

	/*
		Returns the number of boats in the race

		@returns the number of boats in the race
	*/
	public int getNumberOfBoats(){
		return numberOfBoats;
	}

	/*
		Returns a list of boats in the race

		@returns a list of the boats competing in the race
	*/
	public Boat[] getBoats(){
		return boats.toArray(new Boat[boats.size()]);
	}

	/*
		Prints the order in which the boats finished
	*/ 
	public void displayFinishingOrder(){
		int numberOfBoats = this.getNumberOfBoats();
		Boat[] boats = this.getFinishedBoats();

		System.out.println("--- Finishing Order ---");

		for (int i = 0; i < numberOfBoats; i++) {
			System.out.println("#" + Integer.toString(i+1) + " - " + boats[i].getTeamName());
		}
	}

	/*
		Prints the list of boats competing in the race
	*/ 
	private void displayStartingBoats(){
		int numberOfBoats = this.getNumberOfBoats();
		Boat[] boats = this.getBoats();

		System.out.println("--- Starting Boats ---");

		for (int i = 0; i < numberOfBoats; i++) {
			System.out.println(boats[i].getTeamName());
		}
	}

	/*
		Adds a leg to the race

		@param leg, the leg to add to the race
	*/
	public void addLeg(Leg leg){
		this.legs.add(leg);
	}

	/**
	 * Gets legs array
	 * @return an array of legs
	 */
	public ArrayList<Leg> getLegs() {
		return this.legs;
	}

	/**
	 * Calculates how far a boat has travelled in meter
	 * @param velocity the velocity of boat
	 * @return a float number of distance the boat has been travelled
	 */
	public float getDistanceTravelled(long velocity) {
		long timeDiff = System.currentTimeMillis() - this.startTime;
		long timeElapse = timeDiff / 1000 * this.timeScale;
		return timeElapse * velocity;
	}

	/*
		Start the race and print each marker with the order
		in which the boats passed that marker
	*/
	public void startRace(){
		// record start time.
		this.startTime = System.currentTimeMillis();

		for (Leg leg : this.legs.toArray(new Leg[legs.size()])){
			Boat[] boats = this.getShuffledBoats();

			System.out.println("--- " + leg.getMarkerLabel() + " ---");

			// Print the order in which the boats passed the marker
			for (int i = 0; i < this.getNumberOfBoats(); i++) {
				System.out.println("#" + Integer.toString(i+1) + " - " + boats[i].getTeamName());
			}

			System.out.println("");
		} 
	}
}