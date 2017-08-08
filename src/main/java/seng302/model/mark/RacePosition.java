package seng302.model.mark;

/**
 * Represents a boats position between two marks
 */
public class RacePosition {
    private Integer positionIndex;
    private Mark nextMark;
    private Mark previousMark;
    private Boolean isFinishingLeg;

    public RacePosition(Integer positionIndex, Mark nextMark, Mark previousMark){
        this.positionIndex = positionIndex;
        this.nextMark = nextMark;
        this.previousMark = previousMark;
        isFinishingLeg = false;
    }

    /**
     * @return The position of the boat (0...number of marks in race - 1)
     */
    public Integer getPositionIndex(){
        return positionIndex;
    }

    /**
     * @return The mark the boat is heading to
     *         will return NULL if this is the finishing legg
     */
    public Mark getNextMark(){
        return nextMark;
    }

    /**
     * @return The mark the boat is heading away from,
     *         Will return NULL if this is the starting leg
     */
    public Mark getPreviousMark(){
        return previousMark;
    }

    /**
     * Sets a flag that this is the last leg in the race
     */
    public void setFinishingLeg(){
        isFinishingLeg = true;
    }

    /**
     * @return true if this is the last leg in the race
     */
    public boolean getIsFinishingLeg() {
        return isFinishingLeg;
    }
}
