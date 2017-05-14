package seng302.controllers.annotations;

import java.util.HashMap;
import java.util.Map;

public class ImportantAnnotationsState {
    public static final Boolean DEFAULT_ANNOTATION_STATE = true;
    private Map<Annotation, Boolean> currentState;

    /**
     * Stores the users preference for the annotations
     * they consider to be important
     */
    public ImportantAnnotationsState(){
        this.currentState = new HashMap<>();
        initialiseState();
    }

    /**
     * Set each annotation to the default annotation state
     */
    private void initialiseState(){
        for (Annotation annotation : getAnnotations()){
            currentState.put(annotation, DEFAULT_ANNOTATION_STATE);
        }
    }

    /**
     * Sets the state (visibility) of an annotation
     * @param annotation The annotation to set
     * @param visible Whether or not the annotation should be visible
     */
    public void setAnnotationState(Annotation annotation, Boolean visible){
        this.currentState.put(annotation, visible);
    }

    /**
     * Returns the state (visibility) of a specific annotation
     * @param annotation The annotation to check
     * @return True if visible, else false
     */
    public Boolean getAnnotationState(Annotation annotation){
        return this.currentState.containsKey(annotation) && this.currentState.get(annotation);
    }

    /**
     * @return Return an array containing all defined annotations
     */
    public Annotation[] getAnnotations(){
        return Annotation.class.getEnumConstants();
    }
}
