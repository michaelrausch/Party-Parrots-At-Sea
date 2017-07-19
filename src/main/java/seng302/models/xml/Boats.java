package seng302.models.xml;

import seng302.models.mark.Mark;

import java.util.ArrayList;
import java.util.List;

public class Boats {
    private List<Mark> marks;

    public Boats(){
        marks = new ArrayList<>();
    }

    public void addMark(Mark m){
        marks.add(m);
    }

    public List<Mark> getMarks(){
        return this.marks;
    }
}
