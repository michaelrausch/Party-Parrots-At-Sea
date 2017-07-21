package seng302.models;

import static seng302.utilities.GeoUtility.getGeoCoordinate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import javafx.scene.paint.Color;
import seng302.controllers.RaceViewController;
import seng302.gameServer.GameState;
import seng302.models.mark.Mark;
import seng302.utilities.GeoPoint;

/**
 * Yacht class for the racing boat.
 *
 * Class created to store more variables (eg. boat statuses) compared to the XMLParser boat class,
 * also done outside Boat class because some old variables are not used anymore.
 */
public class Yacht {

    private final Double TURN_STEP = 3.0;

    private Integer sourceID;
    private GeoPoint location;
    private Double heading;
    private Double lastHeading;
    private Double velocity;

    private Boolean sailIn;


    /**
     * @param location latlon location of the boat stored in a geopoint
     * @param heading heading of the boat in degrees from 0 to 365 with 0 being north
     */
    public Yacht(GeoPoint location, Double heading) {
        this.location = location;
        this.heading = heading;
        this.velocity = 0.0;
        this.sailIn = false;
    }

    /**
     * @param timeInterval since last update in milliseconds
     */
    public void update(Long timeInterval) {
        if (sailIn) {
            Double secondsElapsed = timeInterval / 1000000.0;
            Double metersCovered = velocity * secondsElapsed;
            location = getGeoCoordinate(location, heading, metersCovered);
        }
    }

    /**
     * Adjusts the yachts velocity based on the wind direction and speed from the polar table.
     *
     * @param windDir current wind Direction TODO: 20/07/17 ajm412: (TWA or AWA, not 100% sure?)
     * @param windSpd current wind Speed
     */
    public void updateYachtVelocity(Double windDir, Double windSpd) {
        Double closestSpd = PolarTable.getClosestMatch(windSpd);
        Map<Double, Double> polarsFromClosestSpd = PolarTable.getPolarTable().get(closestSpd);

        Double closest = 0d;
        Double closest_key = 0d;

        for (Double key : polarsFromClosestSpd.keySet()) {
            Double difference = Math.abs(key - windDir);
            if (difference <= closest) {
                closest = difference;
                closest_key = key;
            }
        }
//        System.out.println("Closest angle " + closest_key);
//        System.out.println("WindDir " + windDir);
        velocity = polarsFromClosestSpd.get(closest_key);
    }

    public Double getHeading() {
        return heading;
    }

    public void adjustHeading(Double amount) {
        lastHeading = heading;
        heading = (heading + amount) % 360.0;
    }

    public void tackGybe(Double windDirection) {
        adjustHeading(-2 * ((heading - windDirection) % 360));
    }

    public void toggleSailIn() {
        sailIn = !sailIn;
    }

    public void turnUpwind() {
        Double normalizedHeading = (heading - GameState.windDirection) % 360;
        if (normalizedHeading == 0) {
            if (lastHeading < 180) {
                adjustHeading(-TURN_STEP);
            } else {
                adjustHeading(TURN_STEP);
            }
        } else if (normalizedHeading == 180) {
            if (lastHeading < 180) {
                adjustHeading(TURN_STEP);
            } else {
                adjustHeading(-TURN_STEP);
            }
        } else if (normalizedHeading < 180) {
            adjustHeading(-TURN_STEP);
        } else {
            adjustHeading(TURN_STEP);
        }
    }

    public void turnDownwind() {
        if ((heading - GameState.windDirection) % 360 < 180) {
            adjustHeading(TURN_STEP);
        } else {
            adjustHeading(-TURN_STEP);
        }
    }
}
