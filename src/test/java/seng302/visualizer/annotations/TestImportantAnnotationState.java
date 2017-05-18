package seng302.visualizer.annotations;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import seng302.controllers.annotations.Annotation;
import seng302.controllers.annotations.ImportantAnnotationsState;

import static org.junit.Assert.assertEquals;

public class TestImportantAnnotationState {
    private ImportantAnnotationsState importantAnnotationsState;

    @Before
    public void setUpForTest(){
        importantAnnotationsState = new ImportantAnnotationsState();
    }

    @After
    public void tearDownAfterTest(){
        importantAnnotationsState = null;
    }

    /**
     * Check whether each annotation has its default value set to the default value when
     * the class is initialized
     */
    @Test
    public void testDefaultValueSet(){
        for (Annotation annotation : importantAnnotationsState.getAnnotations()){
            assertEquals(ImportantAnnotationsState.DEFAULT_ANNOTATION_STATE,
                    importantAnnotationsState.getAnnotationState(annotation));
        }
    }

    /**
     * Check whether an annotations state can be changed
     */
    @Test
    public void testAnnotationStateChange(){
        Annotation[] annotations = importantAnnotationsState.getAnnotations();

        // do not run test if there are no annotations
        if (annotations.length <= 0){
            return;
        }

        Boolean currentAnnotationState = importantAnnotationsState.getAnnotationState(annotations[0]);
        importantAnnotationsState.setAnnotationState(annotations[0], !currentAnnotationState);

        assertEquals(!currentAnnotationState, importantAnnotationsState.getAnnotationState(annotations[0]));
    }
}
