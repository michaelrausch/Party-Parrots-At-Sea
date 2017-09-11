package seng302.utilities;

import javafx.scene.media.AudioClip;
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
    private static AudioClip hoverSoundPlayer = new AudioClip(Sounds.class.getClassLoader().getResource("sounds/sound-over.wav").toExternalForm());;

    private static boolean musicMuted = false;
    private static boolean soundEffectsMuted = false;


    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    public static void setMutes() {
        if (soundPlayer != null) {
            soundPlayer.setMute(soundEffectsMuted);
        }
        if (soundEffect != null) {
            soundEffect.setMute(soundEffectsMuted);
        }
        if (musicPlayer != null) {
            musicPlayer.setMute(musicMuted);
        }
    }

    public static void stopSoundEffects() {
        if (soundEffect != null) {
            soundEffect.stop();
        }
    }

    public static void toggleMuteMusic() {
        musicMuted = !musicMuted;
        if (musicPlayer != null) {
            musicPlayer.setMute(musicMuted);
        }
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
        musicPlayer.setVolume(0.3);
        musicPlayer.play();
        raceMusic = new Media(Sounds.class.getClassLoader().getResource("sounds/Sounds-of-the-ocean.mp3").toString());
        soundEffect = new MediaPlayer(raceMusic);
        soundEffect.setCycleCount(MediaPlayer.INDEFINITE);
        soundEffect.setVolume(0.3);
        soundEffect.play();
        musicPlayer.setMute(musicMuted);
        soundEffect.setMute(soundEffectsMuted);
    }

    public static void playMenuMusic() {
        Media menuMusic = new Media(
            Sounds.class.getClassLoader().getResource("sounds/Elevator-music.mp3").toString());
        musicPlayer = new MediaPlayer(menuMusic);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setVolume(0.3);
        musicPlayer.play();
    }


    public static void playFinishMusic() {
        Media finishMusic = new Media(Sounds.class.getClassLoader().getResource("sounds/Happy-birthday-song.mp3").toString());
        musicPlayer = new MediaPlayer(finishMusic);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setVolume(0.3);
        musicPlayer.play();
        musicPlayer.setMute(musicMuted);
    }

    public static void playButtonClick() {
        if (!soundEffectsMuted) {
            Media buttonClick = new Media(
                Sounds.class.getClassLoader().getResource("sounds/Button-click-sound.mp3")
                    .toString());
            soundPlayer = new MediaPlayer(buttonClick);
            soundPlayer.setVolume(0.5);
            soundPlayer.play();
            soundPlayer.setMute(soundEffectsMuted);
        }
    }

    public static void playFinishSound() {
        if (!soundEffectsMuted) {
            Media finishSound = new Media(
                Sounds.class.getClassLoader().getResource("sounds/Sms-notification.mp3")
                    .toString());
            soundPlayer = new MediaPlayer(finishSound);
            soundPlayer.setVolume(0.5);
            soundPlayer.play();
        }
    }


    public static void playMarkRoundingSound() {
        if (!soundEffectsMuted) {
            Media markRoundingSound = new Media(
                Sounds.class.getClassLoader().getResource("sounds/sms-tone.mp3").toString());
            soundPlayer = new MediaPlayer(markRoundingSound);
            soundPlayer.play();
        }
    }

    public static void playCapGunSound() {
        if (!soundEffectsMuted) {
            Media gunSound = new Media(
                Sounds.class.getClassLoader().getResource("sounds/Gunshot-sound.mp3").toString());
            soundPlayer = new MediaPlayer(gunSound);
            soundPlayer.play();
        }
    }

    public static void playCrashSound() {
        if (!soundEffectsMuted) {
            Media crashSound = new Media(
                Sounds.class.getClassLoader().getResource("sounds/Large-metal-door-slam.mp3")
                    .toString());
            soundPlayer = new MediaPlayer(crashSound);
            soundPlayer.play();
        }
    }

    public static void playTokenPickupSound() {
        if (!soundEffectsMuted) {
            Media pickupSound = new Media(
                Sounds.class.getClassLoader().getResource("sounds/Coin-pick-up-sound-effect.mp3")
                    .toString());
            soundPlayer = new MediaPlayer(pickupSound);
            soundPlayer.play();
        }
    }

    public static void playHoverSound() {
        if (!soundEffectsMuted) {
            hoverSoundPlayer.setVolume(2.5);
            hoverSoundPlayer.play();
        }
    }


}
