package seng302.server.simulator;

import seng302.server.simulator.mark.Corner;
import seng302.server.simulator.mark.Mark;
import seng302.server.simulator.mark.Position;
import seng302.server.simulator.parsers.RaceParser;

import java.util.List;
import java.util.Observable;

public class Simulator extends Observable implements Runnable {

	private List<Corner> course;
	private List<Boat> boats;
	private long lapse;

	/**
	 * Creates a simulator instance with given time lapse.
	 * @param lapse time duration in millisecond.
	 */
	public Simulator(long lapse) {
		RaceParser rp = new RaceParser("/server_config/race.xml");
		course = rp.getCourse();
		boats = rp.getBoats();
		this.lapse = lapse;

		setLegs();

		// set start line's coordinate to boats
		Double startLat = course.get(0).getCompoundMark().getMark1().getLat();
		Double startLng = course.get(0).getCompoundMark().getMark1().getLng();
		for (Boat boat : boats) {
			boat.setLat(startLat);
			boat.setLng(startLng);
			boat.setLastPassedCorner(course.get(0));
			boat.setHeadingCorner(course.get(1));
			boat.setSpeed(50000);
		}
	}

	@Override
	public void run() {

		int numOfFinishedBoats = 0;

		while (numOfFinishedBoats < boats.size()) {
			for (Boat boat : boats) {
				numOfFinishedBoats += moveBoat(boat, lapse);
			}
			System.out.println(boats.get(0));

			setChanged();
			notifyObservers(boats);

			// sleep for 1 second.
			try {
				Thread.sleep(lapse);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Moves a boat with give time duration.
	 *
	 * @param boat the boat to be moved
	 * @param duration the moving duration in millisecond
	 * @return 1 if boat reached final line, otherwise 0
	 */
	private int moveBoat(Boat boat, double duration) {
		if (boat.getHeadingCorner() != null) {

			boat.move(boat.getLastPassedCorner().getBearingToNextCorner(), duration);

			Position boatPos = new Position(boat.getLat(), boat.getLng());
			Position lastMarkPos = boat.getLastPassedCorner().getCompoundMark().getMark1();

			double distanceFromLastMark = GeoUtility.getDistance(boatPos, lastMarkPos);
			// if a boat passes its heading mark
			while (distanceFromLastMark >= boat.getLastPassedCorner().getDistanceToNextCorner()) {
				double compensateDistance = distanceFromLastMark - boat.getLastPassedCorner().getDistanceToNextCorner();
				boat.setLastPassedCorner(boat.getHeadingCorner());
				boat.setHeadingCorner(boat.getLastPassedCorner().getNextCorner());

				// heading corner == null means boat has reached the final mark
				if (boat.getHeadingCorner() == null) return 1;

				// move compensate distance for the mark just passed
				Position pos = GeoUtility.getGeoCoordinate(
						boat.getLastPassedCorner().getCompoundMark().getMark1(),
						boat.getLastPassedCorner().getBearingToNextCorner(),
						compensateDistance);
				boat.setLat(pos.getLat());
				boat.setLng(pos.getLng());
				distanceFromLastMark = GeoUtility.getDistance(new Position(boat.getLat(), boat.getLng()),
						boat.getLastPassedCorner().getCompoundMark().getMark1());
			}
		}
		return 0;
	}

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

}
