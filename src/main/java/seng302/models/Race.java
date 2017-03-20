package seng302.models;

import seng302.models.mark.Mark;

import java.util.*;

/**
 * Race class containing the boats and legs in the race
 * Created by mra106 on 8/3/2017.
 */
public class Race {
    private ArrayList<Boat> boats; // The boats in the race
    private ArrayList<Boat> finishingOrder; // The order in which the boats finish the race
    private HashMap<Boat, List> events = new HashMap<>(); // The events that occur in the race
    private List<Mark> course; // Marks in the race
    private long startTime = 0;
    private double timeScale = 1;

    /**
     * Race class containing the boats and legs in the race
     */
    public Race() {
        this.boats = new ArrayList<>();
        this.finishingOrder = new ArrayList<>();
        this.course = new ArrayList<>();
    }

    /**
     * Add a boat to the race
     *
     * @param boat, the boat to add
     */
    public void addBoat(Boat boat) {
        boats.add(boat);
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
     * Returns a list of boats in the race
     *
     * @return a list of the boats competing in the race
     */
    public Boat[] getBoats() {
        return boats.toArray(new Boat[boats.size()]);
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

        for (Boat boat : this.boats) {
            double totalDistance = 0;
            int numberOfMarks = this.course.size();

            for (int i = 0; i < numberOfMarks; i++) {
                Double time =  (1000 * totalDistance / boat.getVelocity());

                // If there are singleMarks after this event
                if (i < numberOfMarks - 1) {
                    Event event = new Event(time, boat, course.get(i), course.get(i + 1));

                    try {
                        events.get(boat).add(event);

                    } catch (NullPointerException e) {
                        events.put(boat, new ArrayList<>(Arrays.asList(event)));
                    }
                    totalDistance += event.getDistanceBetweenMarks();
                }

                // There are no more marks after this event

                else{
                    Event event = new Event(time, boat, course.get(i), course.get(i));
                    events.get(boat).add(event);
                }
            }
        }
    }


    /**
     * Starts a race and generates all events for the race.
     */
    public void startRace() {
        // record start time.
        this.startTime = System.currentTimeMillis();
        generateEvents();
    }

    public void addCourse(List<Mark> course) {
        this.course = course;
    }

    public List<Mark> getCourse() {
        return course;
    }

    public HashMap<Boat, List> getEvents() {
        return events;
    }
}