package seng302.models.mark;

/**
 * To represent two types of mark
 * Created by Haoming Yin (hyi25) on 17/3/17.
 */


public enum MarkType {

    UNKNOWN(0),
    ROUNDING_MARK(1),
    GATE_MARK(2),
    // above mark types are from AC35 spec.

    // more specific types for gate mark
    WINDWARD(201),
    LEEWARD(202),
    START(203),
    FINISH(204),

    // single_mark is from old team-13 code base, for compatibility, it has not
    // been removed yet
    SINGLE_MARK(5);

    private int type;

    MarkType(int markType) {
        this.type = markType;
    }

    public int getType() {
        return this.type;
    }

}
