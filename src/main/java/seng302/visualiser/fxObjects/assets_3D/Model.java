package seng302.visualiser.fxObjects.assets_3D;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;

/**
 * Created by CJIRWIN on 7/09/2017.
 */
public class Model {

    AnimationTimer animationTimer;
    Group assets;

    Model (Group assets, AnimationTimer animation) {
        this.assets = assets;
        this.animationTimer = animation;
        if (animation != null) {
            animation.start();
        }
    }

    public void setAnimation(AnimationTimer animation) {
        animationTimer = animation;
        if (animation != null) {
            animation.start();
        }
    }

    /**
     * Stops the animation of this model.
     */
    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    public Group getAssets() {
        return this.assets;
    }
}
