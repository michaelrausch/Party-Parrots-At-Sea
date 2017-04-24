package seng302.server.simulator.mark;

public class Corner {

	private int seqID;
	private CompoundMark compoundMark;
	//private int CompoundMarkID;
	private RoundingType roundingType;
	private int zoneSize; // size of the zone around a mark in boat-lengths.

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
}
