package seng302;

import java.util.*;


public class Race {
	private ArrayList<Boat> boats;
	private ArrayList<Leg> legs;
	private PriorityQueue<Event> events;
	private int numberOfBoats = 0;
	private long startTime = 0;
	private int timeScale = 1;

	public Race() {
		this.boats = new ArrayList<Boat>();
		this.legs = new ArrayList<Leg>();
		// create a priority queue within custom Comparator to order events
		this.events = new PriorityQueue<Event>(new Comparator<Event>() {
			@Override
			public int compare(Event o1, Event o2) {
				Long time1 = o1.getTime();
				Long time2 = o2.getTime();
				// order event asc. by time. if tie appears, then order team
				// name alphabetically.
				if (time1 != time2) {
					return time1.compareTo(time2);
				} else {
					return o1.getBoat().getTeamName().compareTo(o2.getBoat().getTeamName());
				}
			}
		});
	}

	/*
		Add a boat to the race
		@param boat, the boat to add
	*/
	public void addBoat(Boat boat) {
		boats.add(boat);
		numberOfBoats += 1;
	}

	/*
		Returns a list of boats in a random order

		@returns a list of boats
	*/
	public Boat[] getShuffledBoats() {
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
	public Boat[] getFinishedBoats() {
		return getShuffledBoats();
	}

	/*
		Returns the number of boats in the race

		@returns the number of boats in the race
	*/
	public int getNumberOfBoats() {
		return numberOfBoats;
	}

	/*
		Returns a list of boats in the race

		@returns a list of the boats competing in the race
	*/
	public Boat[] getBoats() {
		return boats.toArray(new Boat[boats.size()]);
	}

	/*
		Prints the order in which the boats finished
	*/
	public void displayFinishingOrder() {
		int numberOfBoats = this.getNumberOfBoats();
		Boat[] boats = this.getFinishedBoats();

		System.out.println("--- Finishing Order ---");

		for (int i = 0; i < numberOfBoats; i++) {
			System.out.println("#" + Integer.toString(i + 1) + " - " + boats[i].getTeamName());
		}
	}

	/*
		Prints the list of boats competing in the race
	*/
	private void displayStartingBoats() {
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
	public void addLeg(Leg leg) {
		this.legs.add(leg);
	}

	/**
	 * Gets legs array
	 *
	 * @return an array of legs
	 */
	public ArrayList<Leg> getLegs() {
		return this.legs;
	}

	/**
	 * Temporary method used to generated all the events.
	 */
	private void generateEvents() {

		//calculate the time for every boat passes each leg, and create an event
		for (Boat boat : this.boats) {
			long totalDistance = 0;
			for (Leg leg : this.legs) {
				totalDistance += leg.getDistance();
				long time = (long) (1000 * totalDistance / (boat.getVelocity() * this.timeScale));
				Event event = new Event(time, boat, leg);
				events.add(event);
			}
		}
	}

	/**
	 * Note: this function is useless so far
	 * Calculates how far a boat has travelled in meter
	 *
	 * @param velocity the velocity of boat
	 * @return a float number of distance the boat has been travelled
	 */
	public float getDistanceTravelled(long velocity) {
		long timeDiff = System.currentTimeMillis() - this.startTime;
		long timeElapse = timeDiff / 1000 * this.timeScale;
		return timeElapse * velocity;
	}

	/**
	 * Micheal, here is a demo function shows you how to iterate all events
	 */
	public void iterateEvents() {
		// iterates all events. ends when no event in events.
		while (!events.isEmpty()) {
			Event peekEvent = events.peek();
			long currentTime = System.currentTimeMillis() - this.startTime;
			if (currentTime > peekEvent.getTime()) {
				// pull out the event
				Event nextEvent = events.poll();
				// I just simply print it out for testing
				System.out.println(nextEvent.getTimeString() + ", " +
						nextEvent.getBoat().getTeamName() + " passed " +
						nextEvent.getLeg().getMarkerLabel());
			}
		}
	}

	/*
		Start the race and print each marker with the order
		in which the boats passed that marker
	*/
	public void startRace() {
		// record start time.
		generateEvents();
		this.startTime = System.currentTimeMillis();
		iterateEvents();

		for (Leg leg : this.legs) {
			Boat[] boats = this.getShuffledBoats();

			System.out.println("--- " + leg.getMarkerLabel() + " ---");

			// Print the order in which the boats passed the marker
			for (int i = 0; i < this.getNumberOfBoats(); i++) {
				System.out.println("#" + Integer.toString(i + 1) + " - " + boats[i].getTeamName());
			}

			System.out.println("");
		}
	}
}