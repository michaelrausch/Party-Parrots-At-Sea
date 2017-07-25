package seng302.server.simulator;

import seng302.server.simulator.mark.CompoundMark;
import seng302.server.simulator.mark.Corner;
import seng302.server.simulator.mark.Mark;
import seng302.server.simulator.parsers.RaceParser;
import seng302.utilities.GeoPoint;
import seng302.utilities.GeoUtility;

import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Simulator extends Observable implements Runnable {

	private List<Corner> course;
	private List<Boat> boats;
	private long lapse;
	private boolean isRaceStarted;

	/**
	 * Creates a simulator instance with given time lapse.
	 * @param lapse time duration in millisecond.
	 */
	public Simulator(long lapse) {
		RaceParser rp = new RaceParser("/server_config/race.xml");
		course = rp.getCourse();
		boats = rp.getBoats();
		this.lapse = lapse;
		isRaceStarted = false;

		setLegs();

		// set start line's coordinate to boats
		Double startLat = course.get(0).getCompoundMark().getMark1().getLat();
		Double startLng = course.get(0).getCompoundMark().getMark1().getLng();
		for (Boat boat : boats) {
			boat.setLat(startLat);
			boat.setLng(startLng);
			boat.setLastPassedCorner(course.get(0));
			boat.setHeadingCorner(course.get(1));
			boat.setSpeed(ThreadLocalRandom.current().nextInt(40000, 60000 + 1));
		}
	}

	@Override
	public void run() {

		int numOfFinishedBoats = 0;

		while (numOfFinishedBoats < boats.size()) {

			// if race has started, then boat should start to move.
			if (isRaceStarted) {
				for (Boat boat : boats) {
					numOfFinishedBoats += moveBoat(boat, lapse);
				}
			}

			setChanged();
			notifyObservers(boats);

			try {
				Thread.sleep(lapse);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Moves a boat with given time duration.
	 * @param boat the boat to be moved
	 * @param duration the moving duration in milliseconds
	 * @return 1 if the boat has reached the final line, otherwise return 0
	 */
	private int moveBoat(Boat boat, double duration) {
		if (boat.getHeadingCorner() != null) {

			boat.move(boat.getLastPassedCorner().getBearingToNextCorner(), duration);

			GeoPoint boatPos = new GeoPoint(boat.getLat(), boat.getLng());
			GeoPoint lastMarkPos = boat.getLastPassedCorner().getCompoundMark().getMark1();

			double distanceFromLastMark = GeoUtility.getDistance(boatPos, lastMarkPos);
			// if a boat passes its heading mark
			while (distanceFromLastMark >= boat.getLastPassedCorner().getDistanceToNextCorner()) {
				double compensateDistance = distanceFromLastMark - boat.getLastPassedCorner().getDistanceToNextCorner();
				boat.setLastPassedCorner(boat.getHeadingCorner());
				boat.setHeadingCorner(boat.getLastPassedCorner().getNextCorner());

				// heading corner == null means boat has reached the final mark
				if (boat.getHeadingCorner() == null) {
					boat.setFinished(true);
					return 1;
				}

				// move compensate distance for the mark just passed
				GeoPoint pos = GeoUtility.getGeoCoordinate(
						boat.getLastPassedCorner().getCompoundMark().getMark1(),
						boat.getLastPassedCorner().getBearingToNextCorner(),
						compensateDistance);
				boat.setLat(pos.getLat());
				boat.setLng(pos.getLng());
				distanceFromLastMark = GeoUtility.getDistance(new GeoPoint(boat.getLat(), boat.getLng()),
						boat.getLastPassedCorner().getCompoundMark().getMark1());
			}
		}
		return 0;
	}

	/**
	 * Link all the corners in the course list so that every corner knows its next
	 * corner, as well as the distance and bearing to its next corner. However,
	 * the last corner's heading is null, which means it is the final line.
	 */
	private void setLegs() {
		// get the bearing from one mark to the next heading mark
		for (int i = 0; i < course.size() - 1; i++) {

			Mark mark1 = course.get(i).getCompoundMark().getMark1();
			Mark mark2 = course.get(i + 1).getCompoundMark().getMark1();
			course.get(i).setDistanceToNextCorner(GeoUtility.getDistance(mark1, mark2));

			course.get(i).setNextCorner(course.get(i + 1));

			course.get(i).setBearingToNextCorner(
					GeoUtility.getBearing(course.get(i).getCompoundMark().getMark1(),
					course.get(i + 1).getCompoundMark().getMark1()));
		}
	}

	public List<Boat> getBoats(){
		return boats;
	}

	public void setRaceStarted(boolean raceStarted) {
		isRaceStarted = raceStarted;
	}

	/**
	 * @return A list of marks in the race
	 */
	public Set<CompoundMark> getMarks(){
		Set<CompoundMark> marks =  new HashSet<>();

		for (Corner c : course){
			marks.add(c.getCompoundMark());
			marks.add(c.getCompoundMark());
		}

		return marks;
	}
}
