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
