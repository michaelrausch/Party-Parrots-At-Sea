package seng302.visualiser.fxObjects.assets_2D;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Grouping of string objects over a semi transparent background.
 */
public class AnnotationBox extends Group {

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
                Platform.runLater(() -> text.setText(format.transformString(newVal)))
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
    private static final double X_OFFSET_TEXT = 20d;
    private static final double Y_OFFSET_TEXT_INIT = -35d;
    private static final double Y_OFFSET_PER_TEXT = 12d;
    //Background constants
    private static final double TEXT_BUFFER = 3;
    private static final double BACKGROUND_X = X_OFFSET_TEXT - TEXT_BUFFER;
    private static final double BACKGROUND_Y = Y_OFFSET_TEXT_INIT - TEXT_BUFFER;
    private static final double BACKGROUND_H_PER_TEXT = 9.5d;
    private static final double BACKGROUND_ARC_SIZE = 10;

    private int visibleAnnotations = 0;
    private double backgroundWidth = 145d;

    private Rectangle background = new Rectangle();
    private Paint theme = Color.BLACK;

    private Map<String, Annotation> annotationsByName = new HashMap<>();

    /**
     * Creates an empty annotation box. The box is offset from (0,0) by (17, -38).
     */
    public AnnotationBox() {
        this.setCache(true);
        background.setX(BACKGROUND_X);
        background.setY(BACKGROUND_Y);
        background.setWidth(backgroundWidth);
        background.setHeight(Math.abs(BACKGROUND_X) + TEXT_BUFFER + BACKGROUND_H_PER_TEXT * 4);
        background.setArcHeight(BACKGROUND_ARC_SIZE);
        background.setArcWidth(BACKGROUND_ARC_SIZE);
        background.setFill(new Color(1, 1, 1, 0.75));
        background.setStroke(theme);
        background.setStrokeWidth(2);
        background.setCache(true);
        background.setCacheHint(CacheHint.SCALE);
        this.getChildren().add(background);
    }

    /**
     * Adds an annotation to the box. Use the name to reference the annotation for removal or\
     * changing visibility.
     * @param annotationName the name of the annotation.
     * @param annotation the annotation.
     */
    public void addAnnotation (String annotationName, Annotation annotation) {
        annotationsByName.put(annotationName, annotation);
        Platform.runLater(() -> {
            this.getChildren().add(annotation.getText());
            visibleAnnotations++;
            update();
        });
    }

    /**
     * Adds an annotation with a constant text.
     * @param annotationName The name of the annotation. Will be used to reference it later.
     * @param annotationText The desired text.
     */
    public void addAnnotation (String annotationName, String annotationText) {
        Text text = getTextObject();
        addAnnotation(annotationName, new Annotation(text, annotationText));
    }

    /**
     * Adds an annotation with the given name. The annotation will contain the value of the given
     * ObservableValue. The formatter should return a String and takes an object of the same type as
     * the ObservableValue as a parameter. The String is how you want the annotation to look.
     * @param annotationName The annotation name.
     * @param observable The observable value the annotation will display.
     * @param formatter A formatting function for the observable value.
     * @param <E> The type of ObservableValue.
     */
    public <E> void addAnnotation (String annotationName, ObservableValue<E> observable,
        AnnotationFormatter<E> formatter) {
        Text newText = getTextObject();
        addAnnotation(annotationName, new Annotation<>(newText, observable, formatter));
    }

    /**
     * Sets the visibility of the annotation with the given name if it exists.
     * @param annotationName The name of the annotation
     * @param visibility the desired visibility
     */
    public void setAnnotationVisibility (String annotationName, boolean visibility) {
        if (annotationsByName.containsKey(annotationName)) {
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
    }

    /**
     * Removes the annotation with the given name if it exits.
     * @param annotationName The name given when the annotation was created.
     */
    public void removeAnnotation (String annotationName) {
        if (annotationName.contains(annotationName)) {
            Platform.runLater(() -> {
                this.getChildren().remove(annotationsByName.remove(annotationName).getText());
                visibleAnnotations--;
                update();
            });
            annotationsByName.remove(annotationName);
        }
    }

    /**
     * Moves the annotation.
     * @param x x location
     * @param y y location
     */
    public void setLocation (double x, double y) {
        Platform.runLater(()-> this.relocate(x + BACKGROUND_X, y + BACKGROUND_Y));
    }

    /**
     * Changes the width of the annotation box. Default is 145.
     * @param width new width.
     */
    public void setWidth (double width) {
        backgroundWidth = width;
        Platform.runLater(() -> background.setWidth(backgroundWidth));
    }

    private void update () {
        background.setVisible(visibleAnnotations != 0);
        background.setHeight(Math.abs(BACKGROUND_X) + TEXT_BUFFER + BACKGROUND_H_PER_TEXT * visibleAnnotations);
        for (int i = 1; i <= visibleAnnotations; i++) {
            Text text = (Text) this.getChildren().get(i);
            if (text.visibleProperty().get()) {
                    text.setX(X_OFFSET_TEXT);
                    text.setY(Y_OFFSET_TEXT_INIT + Y_OFFSET_PER_TEXT * i);
//                });
            }
        }
    }

    /**
     * Returns a text object for an annotation.
     * @return The text object
     */
    private Text getTextObject() {
        Text text = new Text();
        text.setFill(theme);
        text.setStrokeWidth(2);
//        text.setCacheHint(CacheHint.QUALITY);
        text.setCache(true);
        return text;
    }

    /**
     * Set the colour of the annotation box's border and text colour.
     * @param value desired colour.
     */
    public void setFill (Paint value) {
        theme = value;
        background.setStroke(theme);
        annotationsByName.forEach((name, annotation) -> annotation.getText().setFill(theme));
    }
}
