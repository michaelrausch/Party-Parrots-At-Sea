package seng302.visualiser.fxObjects;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Grouping of string objects over a semi transparent background.
 */
public class AnnotationBox extends Group{

    @FunctionalInterface
    public interface AnnotationFormatter<T> {
        String transformString (T input);
    }

    /**
     * Class stores a text object and relationship for updating the text object if needed
     *
     * @param <T> The type of observable value passed to the annotation, if there is one.
     */
    public class Annotation<T> {
        private Text text;
        private ObservableValue<T> source;
        private AnnotationFormatter<T> format;

        /**
         * Constructor for observing annotation
         * @param textObject the javaFX text object the annotation is displayed in
         * @param source observable value that the annotation is taken from
         * @param formatter interface describing how to format the source data if needed
         */
        public Annotation (Text textObject, ObservableValue<T> source, AnnotationFormatter<T> formatter) {
            this.text = textObject;
            this.source = source;
            this.format = formatter;
            source.addListener((obs, oldVal, newVal) ->
                text.setText(format.transformString(newVal))
            );
        }

        /**
         * Constructor for a static annotation
         * @param textObject the javaFX text object the annotation is displayed in
         * @param annotationText the static value of the test object
         */
        public Annotation (Text textObject, String annotationText) {
            textObject.setText(annotationText);
            text = textObject;
        }

        private Text getText () {
            return text;
        }
    }

    //Text offset constants
    private static final double X_OFFSET_TEXT = 18d;
    private static final double Y_OFFSET_TEXT_INIT = -29d;
    private static final double Y_OFFSET_PER_TEXT = 12d;
    //Background constants
    private static final double TEXT_BUFFER = 3;
    private static final double BACKGROUND_X = X_OFFSET_TEXT - TEXT_BUFFER;
    private static final double BACKGROUND_Y = Y_OFFSET_TEXT_INIT - TEXT_BUFFER;
    private static final double BACKGROUND_H_PER_TEXT = 9.5d;
    private static final double BACKGROUND_ARC_SIZE = 10;

    private int visibleAnnotations = 0;
    private double backgroundWidth = 125d;

    private Rectangle background = new Rectangle();
    private Paint theme = Color.BLACK;

    private Map<String, Annotation> annotationsByName = new HashMap<>();

    public AnnotationBox() {
//        this.setCache(true);
        background.setX(BACKGROUND_X);
        background.setY(BACKGROUND_Y);
        background.setWidth(backgroundWidth);
        background.setHeight(Math.abs(BACKGROUND_X) + TEXT_BUFFER + BACKGROUND_H_PER_TEXT * 4);
        background.setArcHeight(BACKGROUND_ARC_SIZE);
        background.setArcWidth(BACKGROUND_ARC_SIZE);
        background.setFill(new Color(1, 1, 1, 0.75));
        background.setStroke(theme);
        background.setStrokeWidth(2);
//        background.setCache(true);
//        background.setCacheHint(CacheHint.SPEED);
        this.getChildren().add(background);
    }

    public void addAnnotation (String annotationName, Annotation annotation) {
        annotationsByName.put(annotationName, annotation);
        this.getChildren().add(annotation.getText());
        visibleAnnotations++;
        update();
    }

    public void addAnnotation (String annotationName, String annotationText) {
        Text text = getTextObject();
        annotationsByName.put(annotationName, new Annotation(text, annotationText));
        this.getChildren().add(text);
        visibleAnnotations++;
        update();
    }

    public <E> void addAnnotation (String annotationName, ObservableValue<E> observable) {
        addAnnotation(annotationName, observable, E::toString);
    }

    public <E> void addAnnotation (String annotationName, ObservableValue<E> observable,
        AnnotationFormatter<E> formatter) {
        Text newText = getTextObject();
        annotationsByName.put(annotationName, new Annotation<>(newText, observable, formatter));
        this.getChildren().add(newText);
        visibleAnnotations++;
        update();
    }

    public void setAnnotationVisibility (String annotationName, boolean visibility) {
        Text textField = annotationsByName.get(annotationName).text;
        boolean currentState = textField.visibleProperty().get();

        if (visibility != currentState) {
            if (visibility)
                visibleAnnotations++;
            else
                visibleAnnotations--;
        }
        textField.setVisible(visibility);
        update();
    }

    public void removeAnnotation (String annotationName) {
        this.getChildren().remove(annotationsByName.remove(annotationName).getText());
        annotationsByName.remove(annotationName);
        visibleAnnotations--;
        update();
    }

    public void bindLocation () {

    }

    public void setLocation (double x, double y) {
        this.setTranslateX(x);
        this.setTranslateY(y);
    }

    public void setWidth (double width) {
        backgroundWidth = width;
        background.setWidth(backgroundWidth);
    }

    private void update () {
        background.setVisible(visibleAnnotations != 0);
        background.setHeight(Math.abs(BACKGROUND_X) + TEXT_BUFFER + BACKGROUND_H_PER_TEXT * visibleAnnotations);
        for (int i = 1; i <= visibleAnnotations; i++) {
            Text text = (Text) this.getChildren().get(i);
            if (text.visibleProperty().get())
                text.relocate(X_OFFSET_TEXT, Y_OFFSET_TEXT_INIT * Y_OFFSET_PER_TEXT * (i + 1));
        }
    }

    /**
     * Return a text object with caching and a color applied
     *
     * @return The text object
     */
    private Text getTextObject() {
        Text text = new Text();
        text.setFill(theme);
        text.setStrokeWidth(2);
//        text.setCacheHint(CacheHint.SPEED);
//        text.setCache(true);
        return text;
    }

    public void setFill (Paint value) {
        theme = value;
        background.setStroke(theme);
        annotationsByName.forEach((name, annotation) -> annotation.getText().setFill(theme));
    }
}
