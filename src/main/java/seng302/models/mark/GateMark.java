package seng302.models.mark;

/**
 * To represent a gate mark which contains two single marks.
 * Created by ptg19 on 16/03/17.
 * Modified by Haoming Yin (hyi25) on 17/3/2017.
 */
public class GateMark extends Mark {

    private SingleMark singleMark1;
    private SingleMark singleMark2;

    /**
     * Create an instance of Gate Mark which contains two single mark
     * @param name the name of the gate mark
     * @param singleMark1 one single mark inside of the gate mark
     * @param singleMark2 the second mark inside of the gate mark
     */
    public GateMark(String name, SingleMark singleMark1, SingleMark singleMark2, double latitude, double longitude) {
        super(name, MarkType.GATE_MARK, latitude, longitude);
        this.singleMark1 = singleMark1;
        this.singleMark2 = singleMark2;
    }

    public SingleMark getSingleMark1() {
        return singleMark1;
    }

    public void setSingleMark1(SingleMark singleMark1) {
        this.singleMark1 = singleMark1;
    }

    public SingleMark getSingleMark2() {
        return singleMark2;
    }

    public void setSingleMark2(SingleMark singleMark2) {
        this.singleMark2 = singleMark2;
    }

    public double getLatitude(){
        return (this.getSingleMark1().getLatitude() + this.getSingleMark2().getLatitude()) / 2;
    }

    public double getLongitude(){
        return (this.getSingleMark1().getLongitude() + this.getSingleMark2().getLongitude()) / 2;
    }
}
