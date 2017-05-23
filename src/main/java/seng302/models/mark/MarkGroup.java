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

    private Point2D getPointRotation(Point2D ref, Double distance, Double angle){
        Double newX = ref.getX() + (ref.getX() + distance -ref.getX())*Math.cos(angle) - (ref.getY() + distance -ref.getY())*Math.sin(angle);
        Double newY = ref.getY() + (ref.getX() + distance -ref.getX())*Math.sin(angle) + (ref.getY() + distance -ref.getY())*Math.cos(angle);

        return new Point2D(newX, newY);
    }

    /**
     * Adds a lay-line to the MarkGroup
     * @param startPoint The mark where the lay line starts
     * @param layLineAngle The angle the laylines point
     * @param baseAngle The reference angle
     */
    private void addLayLine(Point2D startPoint, Double layLineAngle, Double baseAngle){

        Point2D ep1 = getPointRotation(startPoint, 50.0, baseAngle + -layLineAngle);
        Point2D ep2 = getPointRotation(startPoint, 50.0, baseAngle + layLineAngle);

        Line line1 = new Line(startPoint.getX(), startPoint.getY(), ep1.getX(), ep1.getY());
        Line line2 = new Line(startPoint.getX(), startPoint.getY(), ep2.getX(), ep2.getY());

        line1.setStrokeWidth(0.5);
        line1.setStroke(Color.GREEN);

        line2.setStrokeWidth(0.5);
        line2.setStroke(Color.GREEN);

        super.getChildren().addAll(line1, line2);
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

        //Laylines
//        if (mark.)

        addLayLine(points1, 12.0, 90.0);
        addLayLine(points2, 12.0, 90.0);
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
            if (marks.get(0).getId() == raceId) {
                markCircle1.setCenterX(x);
                markCircle1.setCenterY(y);
                connectingLine.setStartX(markCircle1.getCenterX());
                connectingLine.setStartY(markCircle1.getCenterY());
            } else if (marks.get(1).getId() == raceId) {
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