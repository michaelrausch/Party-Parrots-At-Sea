package seng302.controllers.annotations;

/**
 * An ImportantAnnotationDelegate handles updating the important annotations
 * displayed to the user on behalf of the ImportantAnnotationController
 */
public interface ImportantAnnotationDelegate {
    /**
     * The important annotations have been changed, update the
     * annotations displayed to the user
     * @param importantAnnotationsState The current state of the selected annotations
     */
    void importantAnnotationsChanged(ImportantAnnotationsState importantAnnotationsState);


}
