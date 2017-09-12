package seng302.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

/**
 * Used to test the muting functionality of the sounds util
 * Created by kre39 on 12/09/17.
 */
public class SoundsTest {

    @Test
    public void testMutes() throws Exception {
        Sounds.setMutes();
        Assert.assertFalse(Sounds.isMusicMuted());
        Assert.assertFalse(Sounds.isSoundEffectsMuted());
        Sounds.toggleAllSounds();
        Sounds.toggleMuteEffects();
        Sounds.toggleMuteMusic();
        Assert.assertFalse(Sounds.isMusicMuted());
        Assert.assertFalse(Sounds.isSoundEffectsMuted());
    }
}
