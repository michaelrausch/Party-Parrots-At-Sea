package seng302.server.simulator.mark;

public class CompoundMark {

	private int markID;
	private String name;

	private Mark mark1;
	private Mark mark2;

	public CompoundMark(int markID, String name) {
		this.markID = markID;
		this.name = name;
	}

	public void addMark(int seqId, Mark mark) {
		if (seqId == 1) {
			setMark1(mark);
		} else if (seqId == 2) {
			setMark2(mark);
		}
	}

	/**
	 * Prints out compoundMark's info and its marks, good for testing
	 * @return a string showing its details
	 */
	@Override
	public String toString(){
		if (mark2 == null)
			return String.format("CompoundMark: %d (%s), [%s]",
					markID, name, mark1.toString());
		return String.format("CompoundMark: %d (%s), [%s; %s]",
				markID, name, mark1.toString(), mark2.toString());
	}

	public int getMarkID() {
		return markID;
	}

	public void setMarkID(int markID) {
		this.markID = markID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Mark getMark1() {
		return mark1;
	}

	public void setMark1(Mark mark1) {
		this.mark1 = mark1;
		mark1.setSeqID(1);
	}

	public Mark getMark2() {
		return mark2;
	}

	public void setMark2(Mark mark2) {
		this.mark2 = mark2;
		mark2.setSeqID(2);
	}
}
