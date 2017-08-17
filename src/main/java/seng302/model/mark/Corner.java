package seng302.model.mark;

/**
 * Stores the data for the cornering of a mark.
 */
public class Corner {

    private Integer seqID;
    private Integer compoundMarkID;
    private String rounding;
    private Integer zoneSize;

    public Corner(Integer seqID, Integer compoundMarkID, String rounding, Integer zoneSize) {
        this.seqID = seqID;
        this.compoundMarkID = compoundMarkID;
        this.rounding = rounding;
        this.zoneSize = zoneSize;
    }

    public Integer getSeqID() {
        return seqID;
    }

    public Integer getCompoundMarkID() {
        return compoundMarkID;
    }

    public String getRounding() {
        return rounding;
    }

    public Integer getZoneSize() {
        return zoneSize;
    }
}