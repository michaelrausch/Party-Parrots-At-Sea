package seng302.fxObjects;

import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import seng302.models.Yacht;
import seng302.models.stream.StreamParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cir27 on 23/05/17.
 */
public class BoatAnnotations extends Group{

    private static final double TEAMNAME_X_OFFSET = 18d;
    private static final double TEAMNAME_Y_OFFSET = -29d;
    private static final double VELOCITY_X_OFFSET = 18d;
    private static final double VELOCITY_Y_OFFSET = -17d;
    private static final double ESTTIMETONEXTMARK_X_OFFSET = 18d;
    private static final double ESTTIMETONEXTMARK_Y_OFFSET = -5d;
    private static final double LEGTIME_X_OFFSET = 18d;
    private static final double LEGTIME_Y_OFFSET = 7d;

//    private Rectangle background = new Rectangle();
    private Text teamNameObject;
    private Text velocityObject;
    private Text estTimeToNextMarkObject;
    private Text legTimeObject;
    private List<Node> kids;

    private boolean visible = true;

    BoatAnnotations (Yacht boat, Color theme) {
        super.setCache(true);
//        background.setX(15d);
//        background.setY(-32d);
//        background.setWidth(150);
//        background.setHeight(55);
//        background.setArcHeight(10);
//        background.setArcWidth(10);
//        background.setFill(new Color(1, 1, 1, 0.75));
//        background.setStroke(theme);
//        background.setStrokeWidth(2);
//        background.setCache(true);
//        background.setCacheHint(CacheHint.SPEED);

        teamNameObject = getTextObject(boat.getShortName(), theme);
        teamNameObject.relocate(TEAMNAME_X_OFFSET, TEAMNAME_Y_OFFSET);

        velocityObject = getTextObject("", theme);
        velocityObject.relocate(VELOCITY_X_OFFSET, VELOCITY_Y_OFFSET);
        //On change listener
        boat.getReadOnlyVelocityProperty().addListener((obs, oldVal, newVal) ->
                velocityObject.setText(String.format("%.2f m/s", newVal.doubleValue()))
        );
        //Invalidation listener
        boat.getReadOnlyVelocityProperty().addListener(obs ->
            velocityObject.setText("")
        );

        estTimeToNextMarkObject = getTextObject("Next mark: ", theme);
        estTimeToNextMarkObject.relocate(ESTTIMETONEXTMARK_X_OFFSET, ESTTIMETONEXTMARK_Y_OFFSET);
        boat.getReadOnlyNextMarkProperty().addListener((obs, oldVal, newVal) -> {
            DateFormat format = new SimpleDateFormat("mm:ss");
            String timeToNextMark = format
                    .format(newVal.longValue() - StreamParser.getCurrentTimeLong());
            estTimeToNextMarkObject.setText("Next mark: " + timeToNextMark);
        });
        boat.getReadOnlyNextMarkProperty().addListener(obs ->
            estTimeToNextMarkObject.setText("Next mark: - ")
        );

        legTimeObject = getTextObject("Last mark: -", theme);
        legTimeObject.relocate(LEGTIME_X_OFFSET, LEGTIME_Y_OFFSET);
        boat.getReadOnlyMarkRoundingProperty().addListener((obs, oldTime, newTime) -> {
            DateFormat format = new SimpleDateFormat("mm:ss");
            String elapsedTime = format
                    .format(StreamParser.getCurrentTimeLong() - newTime.longValue());
            legTimeObject.setText("Last mark: " + elapsedTime);
        });
        boat.getReadOnlyMarkRoundingProperty().addListener(obs ->
            legTimeObject.setText("Last mark: - ")
        );

        kids = new ArrayList<>();
//        kids.add(background);
        kids.add(velocityObject);
        kids.add(teamNameObject);
        kids.add(estTimeToNextMarkObject);
        kids.add(legTimeObject);

//        super.getChildren().addAll(background, teamNameObject, velocityObject, estTimeToNextMarkObject, legTimeObject);
    }

    /**
     * Return a text object with caching and a color applied
     *
     * @param defaultText The default text to display
     * @param fill The text fill color
     * @return The text object
     */
    private Text getTextObject(String defaultText, Color fill) {
        Text text = new Text(defaultText);
        text.setFill(fill);
//        text.setCacheHint(CacheHint.SPEED);
        text.setCache(true);
        return text;
    }

    public void setTeamNameObjectVisible(Boolean visible) {
        teamNameObject.setVisible(visible);
    }

    public void setVelocityObjectVisible(Boolean visible) {
        velocityObject.setVisible(visible);
    }

    public void setEstTimeToNextMarkObjectVisible(Boolean visible) {
        estTimeToNextMarkObject.setVisible(visible);
    }

    public void setLegTimeObjectVisible(Boolean visible) {
        legTimeObject.setVisible(visible);
    }

    public void toggleVisible() {
        visible = !visible;
        this.setVisible(visible);
    }

    public List<Node> getkiddies () {
        return kids;
    }
}
