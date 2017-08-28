package seng302.utilities;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Static class for playing sounds throughout the program
 *
 * Created by kre39 on 28/08/17.
 */
public class Sounds {

    private static MediaPlayer musicPlayer;
    private static MediaPlayer soundEffect;
    private static MediaPlayer soundPlayer;

    private static boolean musicMuted = false;
    private static boolean soundEffectsMuted = false;


    public static void stopMusic() {
        musicPlayer.stop();
    }

    public static void toggleMuteMusic() {
        musicMuted = !musicMuted;
        musicPlayer.setMute(musicMuted);
    }

    public static void toggleMuteEffects() {
        soundEffectsMuted = !soundEffectsMuted;
        if (soundPlayer != null) {
            soundPlayer.setMute(soundEffectsMuted);
        }
        if (soundEffect != null) {
            soundEffect.setMute(soundEffectsMuted);
        }
    }

    public static boolean isMusicMuted() {
        return musicMuted;
    }

    public static boolean isSoundEffectsMuted() {
        return soundEffectsMuted;
    }

    public static void playRaceMusic() {
//        Media menuMusic = new Media(Sounds.class.getClassLoader().getResource("sounds/Chill-house-music-loop-116-bpm.wav").toString());
        Media raceMusic = new Media(Sounds.class.getClassLoader().getResource("sounds/Music-loop-120-bpm.mp3").toString());
        musicPlayer = new MediaPlayer(raceMusic);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();
        raceMusic = new Media(Sounds.class.getClassLoader().getResource("sounds/Sounds-of-the-ocean.mp3").toString());
        soundEffect = new MediaPlayer(raceMusic);
        soundEffect.setCycleCount(MediaPlayer.INDEFINITE);
//        soundEffect.setVolume(0.3);
        soundEffect.play();
        musicPlayer.setMute(musicMuted);
        soundEffect.setMute(soundEffectsMuted);
    }

    public static void playMenuMusic() {
        Media menuMusic = new Media(Sounds.class.getClassLoader().getResource("sounds/Elevator-music.mp3").toString());
        musicPlayer = new MediaPlayer(menuMusic);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();
        musicPlayer.setMute(musicMuted);
    }

    public static void playButtonClick() {
        Media buttonClick = new Media(Sounds.class.getClassLoader().getResource("sounds/Button-click-sound.mp3").toString());
        soundPlayer = new MediaPlayer(buttonClick);
        soundPlayer.play();
        soundPlayer.setMute(soundEffectsMuted);
    }

    public static void playCapGunSound() {
        Media gunSound = new Media(Sounds.class.getClassLoader().getResource("sounds/Gunshot-sound.mp3").toString());
        soundPlayer = new MediaPlayer(gunSound);
        soundPlayer.play();
        soundPlayer.setMute(soundEffectsMuted);
    }

    public static void playCrashSound() {
        Media crashSound = new Media(Sounds.class.getClassLoader().getResource("sounds/Large-metal-door-slam.mp3").toString());
        soundPlayer = new MediaPlayer(crashSound);
        soundPlayer.play();
        soundPlayer.setMute(soundEffectsMuted);
    }

}
