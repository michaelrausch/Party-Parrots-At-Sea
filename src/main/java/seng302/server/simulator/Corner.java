package seng302.server.simulator;

import seng302.model.mark.CompoundMark;

public class Corner {

	private int seqID;
	private CompoundMark compoundMark;
	//private int CompoundMarkID;
	private RoundingType roundingType;
	private int zoneSize; // size of the zone around a mark in boat-lengths.

	// TODO: this shouldn't be used in the future!!!!
	private double bearingToNextCorner, distanceToNextCorner;
	private Corner nextCorner;

	public Corner(int seqID, CompoundMark compoundMark, RoundingType roundingType, int zoneSize) {
		this.seqID = seqID;
		this.compoundMark = compoundMark;
		this.roundingType = roundingType;
		this.zoneSize = zoneSize;
	}

	/**
	 * Prints out corner's info and its compound mark, good for testing
	 * @return a string showing its details
	 */
	@Override
	public String toString() {
		return String.format("Corner: %d - %s - %d, %s\n",
				seqID, roundingType.getType(), zoneSize, compoundMark.toString());
	}

	public int getSeqID() {
		return seqID;
	}

	public void setSeqID(int seqID) {
		this.seqID = seqID;
	}

	public CompoundMark getCompoundMark() {
		return compoundMark;
	}

	public void setCompoundMark(CompoundMark compoundMark) {
		this.compoundMark = compoundMark;
	}

	public RoundingType getRoundingType() {
		return roundingType;
	}

	public void setRoundingType(RoundingType roundingType) {
		this.roundingType = roundingType;
	}

	public int getZoneSize() {
		return zoneSize;
	}

	public void setZoneSize(int zoneSize) {
		this.zoneSize = zoneSize;
	}


	// TODO: next six setters & getters shouldn't be used in the future.
	public double getBearingToNextCorner() {
		return bearingToNextCorner;
	}

	public void setBearingToNextCorner(double bearingToNextCorner) {
		this.bearingToNextCorner = bearingToNextCorner;
	}

	public double getDistanceToNextCorner() {
		return distanceToNextCorner;
	}

	public void setDistanceToNextCorner(double distanceToNextCorner) {
		this.distanceToNextCorner = distanceToNextCorner;
	}

	public Corner getNextCorner() {
		return nextCorner;
	}

	public void setNextCorner(Corner nextCorner) {
		this.nextCorner = nextCorner;
	}
}
