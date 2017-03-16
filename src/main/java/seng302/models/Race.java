package seng302.models;

import java.lang.reflect.Array;
import java.util.*;

/**
* Race class containing the boats and legs in the race
*/
public class Race {
    private ArrayList<Boat> boats; // The boats in the race
    private ArrayList<Boat> finishingOrder; // The order in which the boats finish the race
    private PriorityQueue<Event> events; // The events that occur in the race
    private ArrayList<Mark> marks; // Marks in the race
    private int numberOfBoats = 0;
    private long startTime = 0;
    private double timeScale = 1;

    /**
    * Race class containing the boats and legs in the race
    */
    public Race() {
        this.boats = new ArrayList<Boat>();
        this.finishingOrder = new ArrayList<Boat>();
        this.marks = new ArrayList<Mark>();

        // create a priority queue with a custom Comparator to order events
        this.events = new PriorityQueue<Event>(new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                Double time1 = o1.getTime();
                Double time2 = o2.getTime();

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

    /**
     * Add a boat to the race
     *
     * @param boat, the boat to add
     */
    public void addBoat(Boat boat) {
        boats.add(boat);
        numberOfBoats += 1;
    }

    /**
     * Returns a list of boats in a random order
     *
     * @returns a list of boats
     */
    public Boat[] getShuffledBoats() {
        // Shuffle the list of boats
        long seed = System.nanoTime();
        Collections.shuffle(this.boats, new Random(seed));

        return boats.toArray(new Boat[boats.size()]);
    }

    /**
     * Returns a list of boats in the order that they
     * finished the race (position 0 is first place)
     *
     * @returns a list of boats
     */
    public Boat[] getFinishedBoats() {
        return this.finishingOrder.toArray(new Boat[this.finishingOrder.size()]);
    }

    /**
     * Returns the number of boats in the race
     *
     * @returns the number of boats in the race
     */
    public int getNumberOfBoats() {
        return numberOfBoats;
    }

    /**
     * Returns a list of boats in the race
     *
     * @return a list of the boats competing in the race
     */
    public Boat[] getBoats() {
        return boats.toArray(new Boat[boats.size()]);
    }

    /**
     * Prints the order in which the boats finished the race
     */
    public void displayFinishingOrder() {
        int numberOfBoats = this.getNumberOfBoats();
        Boat[] boats = this.getFinishedBoats();

        System.out.println("--- Finishing Order ---");

        for (int i = 0; i < Array.getLength(boats); i++) {
            System.out.println("#" + Integer.toString(i + 1) + " - " + boats[i].getTeamName());
        }
    }

    /**
     * Prints the list of boats competing in the race
     */
    public void displayStartingBoats() {
        int numberOfBoats = this.getNumberOfBoats();
        Boat[] boats = this.getBoats();

        System.out.println("######################");
        System.out.println("# Competing Boats    ");
        System.out.println("######################");

        for (int i = 0; i < numberOfBoats; i++) {
            String velocityKnots = String.format("%1.2f", boats[i].getVelocity() * 1.943844492);

            System.out.println(boats[i].getTeamName() + " Velocity: " + velocityKnots + " Knots/s");
        }
    }
    /**
     * Sets time scale 
     *
     * @param timeScale
     */
    public void setTimeScale(double timeScale) {
        this.timeScale = timeScale;
    }

    /**
     * Generate all events that will happen during the race.
     */
    private void generateEvents() {
        //calculate the time every boat passes each leg, and create an event

        for (Boat boat : this.boats) {
            double totalDistance = 0;
            int numberOfMarks = this.marks.size();

            for(int i = 0; i < numberOfMarks; i++){
                Double time = (Double) (1000 * totalDistance / boat.getVelocity());

                // If there are marks after this event
                if (i < numberOfMarks-1) {
                    Event event = new Event(time, boat, marks.get(i), marks.get(i + 1));
                    events.add(event);
                    totalDistance += event.getDistanceBetweenMarks();

                }
                // There are no more marks after this event
                else{
                    Event event = new Event(time, boat, marks.get(i));
                    events.add(event);
                }
            }
        }
    }

    /**
     * Calculates how far a boat has travelled in meters
     *
     * @param velocity the velocity of boat
     * @return a float number of distance the boat has been travelled
     */
    public float getDistanceTravelled(long velocity) {
        long timeDiff = System.currentTimeMillis() - this.startTime;
        long timeElapse = (long) (timeDiff / 1000 * this.timeScale);
        return timeElapse * velocity;
    }

    /**
     * Iterate over events in the race and print the
     * event string for each event
     */
    public void iterateEvents() {
        // iterates all events. ends when no event in events.

        while (!events.isEmpty()) {
            Event peekEvent = events.peek();
            long currentTime = (long) ((System.currentTimeMillis() - this.startTime) * this.timeScale);

            if (currentTime > peekEvent.getTime()) {
                Event nextEvent = events.poll();

                // Display a summary of the event
                System.out.println(nextEvent.getEventString());

                // Display latitude and longitude
                if (!nextEvent.getIsFinishingEvent()){
                    System.out.println(nextEvent.getMark().getLatitude() + ", " + nextEvent.getNextMark().getLongitude());
                }

                System.out.println();

                // If event is a boat finishing the race
                if (nextEvent.getIsFinishingEvent()) {
                    this.finishingOrder.add(nextEvent.getBoat());
                }
            }

            // Wait for 100ms to throttle the while loop
            try {
                Thread.sleep(100);
            } catch (java.lang.InterruptedException e) {
                continue;
            }
        }
    }

    /**
     * Start the race and print each marker with the order
     * in which the boats passed that marker
     */
    public void startRace() {
        // record start time.
        generateEvents();
        this.startTime = System.currentTimeMillis();
        iterateEvents();
    }

    /**
     * Add a mark to the race (in order)
     * @param mark, the mark to add
     */
    public void addMark(Mark mark){
        this.marks.add(mark);
    }
}