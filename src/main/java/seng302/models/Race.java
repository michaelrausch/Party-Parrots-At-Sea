package seng302.models;

import javafx.animation.AnimationTimer;
import seng302.controllers.Controller;
import seng302.models.mark.Mark;

import java.util.*;

/**
 * Race class containing the boats and legs in the race
 * Created by mra106 on 8/3/2017.
 */
public class Race extends Thread {

    private static final double UPDATE_TIME = 0.016666;     // 1 / 60 ie 60fps


    private ArrayList<Boat> boats; // The boats in the race
    private ArrayList<Boat> finishingOrder; // The order in which the boats finish the race
    private List<Mark> markers; // Marks in the race
    private List<Leg> raceLegs;
    private boolean raceFinished = false; // Race is finished
    private int raceTime = -2; // Current time in the race
    private Double timeScale = null;
    private boolean raceStarted = false;
    private Controller controller;

    /**
     * Race class containing the boats and legs in the race
     */
    public Race(List<Mark> markers, ArrayList<Boat> boats, Double timescale, Controller controller) {
        this.boats = boats;
        this.markers = markers;
        this.raceLegs = makeRaceLegs();
        this.controller = controller;

        this.timeScale = timescale;
        this.finishingOrder = new ArrayList<>();
    }


    /**
     * Makes the race legs out of all the marker points for the course
     * @return ArrayList of raceLegs
     */
    private ArrayList<Leg> makeRaceLegs() {
        ArrayList<Leg> raceLegs = new ArrayList<>();
        for (int i=0; i < markers.size()-1; i++) {
            raceLegs.add(new Leg(markers.get(i), markers.get(i+1)));
        }
        return  raceLegs;
    }


    /**
     * A timer that is called every frame to call the action of moving the boats
     */
    private AnimationTimer at = new AnimationTimer() {
        @Override
        public void handle(long now) {

            //Updating Boat positions
            if(finishingOrder.size() != boats.size()) {
                // update the time
                boats.stream().filter(boat -> !finishingOrder.contains(boat)).forEach(boat -> {
                    updateBoatPosition(boat);
                });
            } else {
                at.stop();
            }
        }

    };


    /**
     * Begins the race simulation
     */
    @Override
    public void run() {
        if (!raceStarted) {
//            controller.getPlayPauseButton().setDisable(true);
            at.start();
        }
    }


    /**
     * Moves the coordinates of the boats.
     * @param boat The boat to update the position of
     */
    private void updateBoatPosition(Boat boat) {
        Double thisHeading = boat.getHeading();
        // TODO: 4/8/17 wmu16 - Add a distance scale factor from lat long distance in Metres to xy equivalent
        Double hypMove = boat.getVelocity() * UPDATE_TIME * timeScale; //* distanceScaleFactor
        boat.setLegDistance(boat.getLegDistance() + hypMove);
        moveBoat(boat, thisHeading, hypMove);
    }

    /**
     * Moves a boat along coordinates by breaking down the distance moved along the hypotenuse into x and y components
     * @param boat The Boat to move
     * @param heading The heading the boat is moving at
     * @param hypMove The distance moved along the hypotenuse (absolute distance)
     */
    private void moveBoat(Boat boat, Double heading, Double hypMove) {
        //If the boat has not yet passed the marker at the end of the leg increase its (x,y) as per normal
        // TODO: 4/8/17 wmu16 - Initialising boat positions and legs and headings etc.
        if(boat.getLegDistance() <= boat.getCurrentLeg().getDistance()) {
            Double xMove = hypMove * Math.sin(Math.toRadians(heading));
            Double yMove = - hypMove * Math.cos(Math.toRadians(heading));
            boat.moveBoatBy(xMove, yMove);

            //If the boat has finished...
        } else if (getNextRaceLeg(boat.getCurrentLeg()).equals(boat.getCurrentLeg())) {
            finishingOrder.add(boat);
            //Otherwise apply the overflow distance of the leg to the next leg
        } else {
            Double overflowDistance = boat.getLegDistance() - boat.getCurrentLeg().getDistance();
            boat.setCurrentLeg(getNextRaceLeg(boat.getCurrentLeg()));
            boat.setHeading(boat.getCurrentLeg().getHeading());
            boat.setLegDistance(overflowDistance);
            moveBoat(boat, boat.getHeading(), overflowDistance);
        }

    }

    /**
     * Returns the next leg in the race
     * @param currentRaceLeg The leg that you are currently on
     * @return The next race leg or the same race leg if it has reached the end
     */
    private Leg getNextRaceLeg(Leg currentRaceLeg) {
        Leg nextRaceLeg = currentRaceLeg;
        for(int i = 0; i< raceLegs.size() - 1; i++) {
            if (raceLegs.get(i).equals(currentRaceLeg)) {
                nextRaceLeg = raceLegs.get(i + 1);
            }
        }
        return nextRaceLeg;
    }


    /**
     * Returns a list of boats in the order that they
     * finished the race (position 0 is first place)
     *
     * @return a list of boats
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
     * Starts a race and generates all events for the race.
     */
    public void startRace() {
        // record start time.



    }

    public void pause() {
        at.stop();
    }

    /**
     * Get a list of marks in the markers
     * @return
     */
    public List<Mark> getMarkers() {
        return markers;
    }


    /**
     * Set a boat as finished
     * @param boat The boat that has finished the race
     */
    public void setBoatFinished(Boat boat){
        this.finishingOrder.add(boat);
    }

    /**
     * Set the race as finished
     */
    public void setRaceFinished(){
        this.raceFinished = true;
    }

    /**
     * Return whether or not the race is finished
     * @return true if the race is finished
     */
    public boolean isRaceFinished(){
        return this.raceFinished;
    }

    /**
     * Set the race time
     * @param raceTime the race time in seconds
     */
    public void setRaceTime(int raceTime){
        this.raceTime = raceTime;
    }

    /**
     * Return the race time
     * @return the race time in seconds
     */
    public int getRaceTime(){
        return this.raceTime;
    }

    /**
     * Increment the race time by one second
     */
    public void incrementRaceTime() {
        this.raceTime += this.timeScale;
    }

    public List<Leg> getRaceLegs() {
        return raceLegs;
    }
}