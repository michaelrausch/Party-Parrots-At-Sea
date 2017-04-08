package seng302.models;

/**
 * Created by wmu16 on 4/8/17.
 */
import javafx.scene.canvas.Canvas;
import javafx.util.Pair;
import seng302.models.mark.Mark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class for describing the course information for the canvas
 * Created by wmu16 on 22/03/17.
 */
public class Course {

    private ArrayList<Leg> Legs;
    private ArrayList<Mark> marks;
    public static final Integer SCALE =  160000;


    public Course(ArrayList<Mark> marks) {
        this.marks = marks;
        this.Legs = makeLegs();
    }

    /**
     * Makes the race legs out of all the marker marks for the course
     * @return ArrayList of Legs
     */
    private ArrayList<Leg> makeLegs() {
        ArrayList<Leg> Legs = new ArrayList<>();
        for (int i = 0; i < marks.size()-1; i++) {
            Legs.add(new Leg(marks.get(i), marks.get(i+1)));
        }
        return  Legs;
    }


    public ArrayList<Mark> getMarks() {
        return marks;
    }

    public double getDistanceScaleFactor() {
        return SCALE;
    }

    public ArrayList<Leg> getLegs() {
        return Legs;
    }
}