package seng302.models.mark;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * Created by CJIRWIN on 26/04/2017.
 */
public class MarkGroup extends Group {

    private static int MARK_RADIUS        = 5;
    private static int LINE_THICKNESS     = 2;
    private static double DASHED_GAP_LEN  = 2d;
    private static double DASHED_LINE_LEN = 5d;

    private List<Mark> marks = new ArrayList<>();
    private Mark mainMark;

    public MarkGroup (Mark mark, Point2D... points) {
        marks.add(mark);
        mainMark = mark;
        Color color = Color.BLACK;
        if (mark.getName().equals("Start")){
            color = Color.GREEN;
        } else if (mark.getName().equals("Finish")){
            color = Color.RED;
        }
        Circle markCircle;
        if (mark.getMarkType() == MarkType.SINGLE_MARK) {
            markCircle = new Circle(
                points[0].getX(),
                points[0].getY(),
                MARK_RADIUS,
                color
            );
            super.getChildren().add(markCircle);
        } else {
            markCircle = new Circle(
                points[0].getX(),
                points[0].getY(),
                MARK_RADIUS,
                color
            );
            super.getChildren().add(markCircle);

            markCircle = new Circle(
                points[1].getX(),
                points[1].getY(),
                MARK_RADIUS,
                color
            );
            super.getChildren().add(markCircle);
            Line line = new Line(
                points[0].getX(),
                points[0].getY(),
                points[1].getX(),
                points[1].getY()
            );
            line.setStrokeWidth(LINE_THICKNESS);
            line.setStroke(color);
            if (mark.getMarkType() == MarkType.OPEN_GATE) {
                line.getStrokeDashArray().addAll(DASHED_GAP_LEN, DASHED_LINE_LEN);
            }
            super.getChildren().add(line);
        }
    }

    public void moveMarkTo (double x, double y, long raceId)
    {
        if (mainMark.getMarkType() == MarkType.SINGLE_MARK) {
            Circle markCircle = (Circle) super.getChildren().get(0);
            markCircle.setCenterX(x);
            markCircle.setCenterY(y);
        } else {
            Circle markCircle1 = (Circle) super.getChildren().get(0);
            Circle markCircle2 = (Circle) super.getChildren().get(1);
            Line connectingLine = (Line) super.getChildren().get(2);
            if (marks.get(1).getId() == raceId) {
                markCircle1.setCenterX(x);
                markCircle1.setCenterY(y);
                connectingLine.setStartX(markCircle1.getCenterX());
                connectingLine.setStartY(markCircle1.getCenterY());
            } else if (marks.get(2).getId() == raceId) {
                markCircle2.setCenterX(x);
                markCircle2.setCenterY(y);
                connectingLine.setEndX(markCircle2.getCenterX());
                connectingLine.setEndY(markCircle2.getCenterY());
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
}