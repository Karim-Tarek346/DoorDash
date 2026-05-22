package game.gui.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SoundManager {

    public enum Sfx {
        DICE("dice.mp3"),
        DRAW_CARD("draw card.mp3"),
        SHUFFLE("card shuffle.mp3"),
        WIN_SOMETHING("win something.mp3"),
        LOSE_SOMETHING("lose something.mp3"),
        END("end.mp3"),
        SCARER_WIN("Scarer win.mp3"),
        LAUGHER_WIN("Laugher win.mp3");

        final String file;
        Sfx(String file) { this.file = file; }
    }

    private static SoundManager instance;
    public static SoundManager get() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    private final DoubleProperty musicVolume = new SimpleDoubleProperty(0.55);
    private final DoubleProperty sfxVolume = new SimpleDoubleProperty(0.8);
    private MediaPlayer musicPlayer;
    private final Map<Sfx, AudioClip> sfxClips = new HashMap<>();
    private final List<String> menuPlaylist = new ArrayList<>();
    private int menuTrackIndex = 0;

    private SoundManager() {
        musicVolume.addListener((o, ov, nv) -> {
            if (musicPlayer != null) musicPlayer.setVolume(nv.doubleValue());
        });
    }

    public DoubleProperty musicVolumeProperty() { return musicVolume; }
    public DoubleProperty sfxVolumeProperty() { return sfxVolume; }

    public void preload() {
        for (Sfx s : Sfx.values()) {
            File f = ResourceLocator.findMp3(s.file);
            if (f != null && f.exists()) {
                try {
                    sfxClips.put(s, new AudioClip(f.toURI().toString()));
                } catch (Exception ignored) { }
            }
        }
    }

    public void playSfx(Sfx s) {
        AudioClip clip = sfxClips.get(s);
        if (clip != null) {
            clip.setVolume(sfxVolume.get());
            clip.play();
        }
    }

    public void playMenuPlaylist() {
        File dir = ResourceLocator.findMp3Dir("Menu");
        if (dir == null || !dir.isDirectory()) return;
        File[] files = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".mp3"));
        if (files == null || files.length == 0) return;
        menuPlaylist.clear();
        Arrays.sort(files);
        for (File f : files) menuPlaylist.add(f.toURI().toString());
        Collections.shuffle(menuPlaylist);
        menuTrackIndex = 0;
        playMenuTrack();
    }

    private void playMenuTrack() {
        if (menuPlaylist.isEmpty()) return;
        stopMusic();
        try {
            musicPlayer = new MediaPlayer(new Media(menuPlaylist.get(menuTrackIndex)));
            musicPlayer.setVolume(musicVolume.get());
            musicPlayer.setOnEndOfMedia(() -> {
                menuTrackIndex = (menuTrackIndex + 1) % menuPlaylist.size();
                playMenuTrack();
            });
            musicPlayer.play();
        } catch (Exception ignored) { }
    }

    public void playTheme() {
        File f = ResourceLocator.findMp3("theme.mp3");
        if (f == null || !f.exists()) return;
        stopMusic();
        try {
            musicPlayer = new MediaPlayer(new Media(f.toURI().toString()));
            musicPlayer.setVolume(musicVolume.get());
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.play();
        } catch (Exception ignored) { }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            try {
                musicPlayer.stop();
                musicPlayer.dispose();
            } catch (Exception ignored) { }
            musicPlayer = null;
        }
    }

    public void stopAll() {
        stopMusic();
        for (AudioClip c : sfxClips.values()) c.stop();
    }
}
