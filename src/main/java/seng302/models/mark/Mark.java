package seng302.models.mark;

/**
 * An abstract class to represent general marks
 * Created by Haoming Yin (hyi25) on 17/3/17.
 */
public abstract class Mark {

    private String name;
    private MarkType markType;

    /**
     * Create a mark instance by passing its name and type
     * @param name the name of the mark
     * @param markType the type of mark. either GATE_MARK or SINGLE_MARK.
     */
    public Mark (String name, MarkType markType) {
        this.name = name;
        this.markType = markType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MarkType getMarkType() {
        return markType;
    }

    public void setMarkType(MarkType markType) {
        this.markType = markType;
    }
}
