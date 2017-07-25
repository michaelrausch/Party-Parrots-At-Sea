package seng302.models;


import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import seng302.utilities.GeoPoint;

public class YachtTest {

    Double windDir;
    Double windSpd;
    List<Yacht> yachts = new ArrayList<Yacht>();

    @Before
    public void setUp() {
        PolarTable.parsePolarFile(getClass().getResourceAsStream("/config/acc_polars.csv"));
        windDir = 90d;
        windSpd = 10d;

        yachts.add(new Yacht("Yacht 1", "Y1", new GeoPoint(-30.0, 20.0), 160.0));
        yachts.add(new Yacht("Yacht 2", "Y2", new GeoPoint(-40.0, -20.0), 100.0));
        yachts.add(new Yacht("Yacht 3", "Y3", new GeoPoint(-35.0, -15.5), 20.0));
    }

}
