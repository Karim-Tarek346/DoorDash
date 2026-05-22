package game.gui.util;

import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class ResourceLocator {

    private static final String[] ROOTS = {
            "src/game/gui/resources/",
            "game/gui/resources/",
            "../src/game/gui/resources/",
            System.getProperty("user.dir") + "/src/game/gui/resources/"
    };

    private static final Map<String, Image> imageCache = new HashMap<>();

    private ResourceLocator() { }

    public static File findPng(String name) {
        return find("png files/" + name);
    }

    public static File findMonsterPng(String name) {
        return find("png files/monsters/" + name);
    }

    public static File findMp3(String name) {
        return find("mp3 files/" + name);
    }

    public static File findMp3Dir(String name) {
        return find("mp3 files/" + name);
    }

    private static File find(String relative) {
        for (String root : ROOTS) {
            File c = new File(root + relative);
            if (c.exists()) return c;
        }
        return null;
    }

    public static Image image(String relative) {
        Image cached = imageCache.get(relative);
        if (cached != null) return cached;
        File f = find(relative);
        if (f == null || !f.exists()) return null;
        Image img = new Image(f.toURI().toString());
        imageCache.put(relative, img);
        return img;
    }

    public static Image png(String name) {
        return image("png files/" + name);
    }

    public static Image monsterImage(String name) {
        return image("png files/monsters/" + name);
    }
}
