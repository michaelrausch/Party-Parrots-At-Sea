package seng302.fxObjects;

import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import seng302.client.ClientPacketParser;
import seng302.models.Yacht;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Collection of annotations for boats.
 */
public class BoatAnnotations extends Group{

    //Text offset constants
    private static final double X_OFFSET_TEXT = 18d;
    private static final double Y_OFFSET_TEXT_INIT = -29d;
    private static final double Y_OFFSET_PER_TEXT = 12d;
    //Background constants
    private static final double TEXT_BUFFER = 3;
    private static final double BACKGROUND_X = X_OFFSET_TEXT - TEXT_BUFFER;
    private static final double BACKGROUND_Y = Y_OFFSET_TEXT_INIT - TEXT_BUFFER;
    private static final double BACKGROUND_H_PER_TEXT = 9.5d;
    private static final double BACKGROUND_W = 125d;
    private static final double BACKGROUND_ARC_SIZE = 10;

    private Rectangle background = new Rectangle();
    private Text teamNameObject;
    private Text velocityObject;
    private Text estTimeToNextMarkObject;
    private Text legTimeObject;
    private boolean isPlayer = false;
    private Yacht boat;

    BoatAnnotations (Yacht boat, Color theme) {
        super.setCache(true);
        this.boat = boat;
        background.setX(BACKGROUND_X);
        background.setY(BACKGROUND_Y);
        background.setWidth(BACKGROUND_W);
        background.setHeight(Math.abs(BACKGROUND_X) + TEXT_BUFFER + BACKGROUND_H_PER_TEXT * 4);
        background.setArcHeight(BACKGROUND_ARC_SIZE);
        background.setArcWidth(BACKGROUND_ARC_SIZE);
        background.setFill(new Color(1, 1, 1, 0.75));
        background.setStroke(theme);
        background.setStrokeWidth(2);
        background.setCache(true);
        background.setCacheHint(CacheHint.SPEED);

        teamNameObject = getTextObject(boat.getShortName(), theme);
        teamNameObject.relocate(X_OFFSET_TEXT, Y_OFFSET_TEXT_INIT + Y_OFFSET_PER_TEXT);

        velocityObject = getTextObject("0 m/s", theme);
        velocityObject.relocate(X_OFFSET_TEXT, Y_OFFSET_TEXT_INIT + Y_OFFSET_PER_TEXT * 2);
        velocityObject.setVisible(false);

        estTimeToNextMarkObject = getTextObject("Next mark: ", theme);
        estTimeToNextMarkObject.relocate(X_OFFSET_TEXT, Y_OFFSET_TEXT_INIT + Y_OFFSET_PER_TEXT * 3);
        estTimeToNextMarkObject.setVisible(false);

        legTimeObject = getTextObject("Last mark: -", theme);
        legTimeObject.relocate(X_OFFSET_TEXT, Y_OFFSET_TEXT_INIT + Y_OFFSET_PER_TEXT * 4);
        legTimeObject.setVisible(false);

        this.setVisible(true, false, false, false);

        super.getChildren().addAll(background, teamNameObject, velocityObject, estTimeToNextMarkObject, legTimeObject);
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
        text.setStrokeWidth(2);
        text.setCacheHint(CacheHint.SPEED);
        text.setCache(true);
        return text;
    }

    void update () {
        teamNameObject.setText("Player: " + boat.getShortName());

        velocityObject.setText(String.format(String.format("%.2f m/s", boat.getVelocityMMS())));

        if (boat.getTimeTillNext() != null) {
            DateFormat format = new SimpleDateFormat("mm:ss");
            String timeToNextMark = format
                .format(boat.getTimeTillNext() - ClientPacketParser.getCurrentTimeLong());
            estTimeToNextMarkObject.setText("Next mark: " + timeToNextMark);
        } else {
            estTimeToNextMarkObject.setText("Next mark: -");
        }

        if (boat.getMarkRoundTime() != null) {
            DateFormat format = new SimpleDateFormat("mm:ss");
            String elapsedTime = format
                .format(ClientPacketParser.getCurrentTimeLong() - boat.getMarkRoundTime());
            legTimeObject.setText("Last mark: " + elapsedTime);
        }else {
            legTimeObject.setText("Last mark: - ");
        }
    }

    void setVisible(boolean nameVisibility, boolean speedVisibility,
                             boolean estTimeVisibility, boolean lastMarkVisibility) {
        int totalVisible = 0;

        /*
            This is a temporary fix until the new annotation group is added along with the visualiser
            overhaul.
         */
        totalVisible = updateVisibility(nameVisibility, teamNameObject, totalVisible);
        if (isPlayer)
            totalVisible = updateVisibility(speedVisibility, velocityObject, totalVisible);
//        totalVisible = updateVisibility(estTimeVisibility, estTimeToNextMarkObject, totalVisible);
//        totalVisible = updateVisibility(lastMarkVisibility, legTimeObject, totalVisible);
        if (totalVisible != 0) {
            background.setVisible(true);
            background.setHeight(Math.abs(BACKGROUND_X) + TEXT_BUFFER + BACKGROUND_H_PER_TEXT * totalVisible);
        } else {
            background.setVisible(false);
        }
    }

    private int updateVisibility (boolean visibility, Text text, int totalVisible) {
        if (visibility){
            totalVisible ++;
            text.setVisible(true);
            text.setLayoutX(X_OFFSET_TEXT);
            text.setLayoutY(Y_OFFSET_TEXT_INIT + Y_OFFSET_PER_TEXT * totalVisible);
        } else {
            text.setVisible(false);
        }
        return totalVisible;
    }

    /**
     * Sets these annotations to show more detailed info.
     */
    public void setAsPlayer () {
        isPlayer = true;
        velocityObject.setVisible(true);
    }
}
