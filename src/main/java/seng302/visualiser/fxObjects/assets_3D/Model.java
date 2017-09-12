package seng302.visualiser.fxObjects.assets_3D;

import javafx.animation.AnimationTimer;
import javafx.scene.Group;

/**
 * Class for generic imported 3D model. Animation terminates on if removed from scene.
 */
public class Model {

    AnimationTimer animationTimer;
    Group assets;

    Model (Group assets, AnimationTimer animation) {
        this.assets = assets;
        this.animationTimer = animation;
        if (animation != null) {
            animation.start();
            assets.sceneProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    animationTimer.stop();
                    animationTimer = null;
                }
            });
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
