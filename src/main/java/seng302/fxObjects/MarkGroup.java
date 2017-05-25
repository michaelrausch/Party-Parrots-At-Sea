package seng302.fxObjects;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import seng302.models.mark.GateMark;
import seng302.models.mark.Mark;
import seng302.models.mark.MarkType;
import seng302.models.mark.SingleMark;
import seng302.GeometryUtils;

/**
 * Grouping of javaFX objects needed to represent a Mark on screen.
 */
public class MarkGroup extends Group {

    private static int MARK_RADIUS        = 5;
    private static int LINE_THICKNESS     = 2;
    private static double DASHED_GAP_LEN  = 2d;
    private static double DASHED_LINE_LEN = 5d;

    private List<Mark> marks = new ArrayList<>();
    private Mark mainMark;

    /**
     * Constructor for singleMark groups
     * @param mark
     * @param points
     */
    public MarkGroup (SingleMark mark, Point2D points) {
        marks.add(mark);
        mainMark = mark;
        Color color = Color.BLACK;
        if (mark.getName().equals("Start")){
            color = Color.GREEN;
        } else if (mark.getName().equals("Finish")){
            color = Color.RED;
        }
        Circle markCircle;
        markCircle = new Circle(
            points.getX(),
            points.getY(),
            MARK_RADIUS,
            color
        );
        super.getChildren().add(markCircle);
    }

    public void addLaylines(Line line1, Line line2) {

        super.getChildren().addAll(line1, line2);
    }


    public void removeLaylines() {
        ArrayList<Node> toRemove = new ArrayList<>();
        for(Node node : super.getChildren()) {
            if (node instanceof Line) {
                Line layLine = (Line) node;

                /***
                 * OOHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHhhh
                 */
                if (layLine.getStrokeWidth() == 0.5){
                    toRemove.add(layLine);
                }
            }
        }
        super.getChildren().removeAll(toRemove);
    }

    public MarkGroup(GateMark mark, Point2D points1, Point2D points2) {
        marks.add(mark.getSingleMark1());
        marks.add(mark.getSingleMark2());
        mainMark = mark;
        Color color = Color.BLACK;
        if (mark.getName().equals("Start")){
            color = Color.GREEN;
        } else if (mark.getName().equals("Finish")){
            color = Color.RED;
        }
        Circle markCircle;
        markCircle = new Circle(
            points1.getX(),
            points1.getY(),
            MARK_RADIUS,
            color
        );
        super.getChildren().add(markCircle);

        markCircle = new Circle(
            points2.getX(),
            points2.getY(),
            MARK_RADIUS,
            color
        );
        super.getChildren().add(markCircle);
        Line line = new Line(
            points1.getX(),
            points1.getY(),
            points2.getX(),
            points2.getY()
        );
        line.setStrokeWidth(LINE_THICKNESS);
        line.setStroke(color);
        if (mark.getMarkType() == MarkType.OPEN_GATE) {
            line.getStrokeDashArray().addAll(DASHED_GAP_LEN, DASHED_LINE_LEN);
        }
        super.getChildren().add(line);

    }

    public void moveMarkTo (double x, double y, long raceId)
    {
        if (mainMark.getMarkType() == MarkType.SINGLE_MARK) {
            Circle markCircle = (Circle) super.getChildren().get(0);
            //One of the test streams produced frequent, jittery movements. Added this as a fix.
            if (Math.abs(markCircle.getCenterX() - x) > 5 || Math.abs(markCircle.getCenterY() - y) > 5) {
                markCircle.setCenterX(x);
                markCircle.setCenterY(y);
            }
        } else {
            Circle markCircle1 = (Circle) super.getChildren().get(0);
            Circle markCircle2 = (Circle) super.getChildren().get(1);
            Line connectingLine = (Line) super.getChildren().get(2);
            if (marks.get(0).getId() == raceId) {
                if (Math.abs(markCircle1.getCenterX() - x) > 5 || Math.abs(markCircle1.getCenterY() - y) > 5) {
                    markCircle1.setCenterX(x);
                    markCircle1.setCenterY(y);
                    connectingLine.setStartX(markCircle1.getCenterX());
                    connectingLine.setStartY(markCircle1.getCenterY());
                }
            } else if (marks.get(1).getId() == raceId) {
                if (Math.abs(markCircle2.getCenterX() - x) > 5 || Math.abs(markCircle2.getCenterY() - y) > 5) {
                    markCircle2.setCenterX(x);
                    markCircle2.setCenterY(y);
                    connectingLine.setEndX(markCircle2.getCenterX());
                    connectingLine.setEndY(markCircle2.getCenterY());
                }
            }
        }
    }

    public boolean hasRaceId (int... raceIds) {
        for (int id : raceIds)
            for (Mark mark : marks)
                if (id == mark.getId())
                    return true;
        return false;
    }

    public long[] getRaceIds () {
        long[] idArray = new long[marks.size()];
        int i = 0;
        for (Mark mark : marks)
            idArray[i++] = mark.getId();
        return idArray;
    }

    public Mark getMainMark() {
        return mainMark;
    }
}