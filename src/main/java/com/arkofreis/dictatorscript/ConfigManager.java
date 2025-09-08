package com.arkofreis.dictatorscript;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;

public final class ConfigManager {
    private static final String FILE_NAME = "dictatorscript.properties";

    private static final String KEY_PREFIX = "commandPrefix";
    private static final String KEY_SHULKER = "shulkerColor";
    private static final String KEY_WHISPER = "autoWhisper";
    private static final String KEY_GO_TO_GROUND = "goToGround";
    private static final String KEY_IGNORE_STACKED = "ignoreStackedShulkers";
    private static final String KEY_SET_BLOCK = "setBlock";

    private static boolean loaded = false;

    private static String commandPrefix = ",";
    private static String shulkerColor = "black";
    private static String autoWhisper = "";
    private static boolean goToGround = true;
    private static boolean ignoreStackedShulkers = true;
    private static String setBlock = "";

    private static File getConfigFile() {
        File gameDir = Minecraft.getMinecraft().mcDataDir;
        File configDir = new File(gameDir, "config");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, FILE_NAME);
    }

    public static synchronized void load() {
        if (loaded) return;
        try {
            Properties props = new Properties();
            File file = getConfigFile();
            if (file.exists()) {
                try (InputStream in = new FileInputStream(file)) {
                    props.load(in);
                }
            }
            commandPrefix = props.getProperty(KEY_PREFIX, commandPrefix);
            shulkerColor = props.getProperty(KEY_SHULKER, shulkerColor);
            autoWhisper = props.getProperty(KEY_WHISPER, autoWhisper);
            goToGround = Boolean.parseBoolean(props.getProperty(KEY_GO_TO_GROUND, Boolean.toString(goToGround)));
            ignoreStackedShulkers = Boolean.parseBoolean(props.getProperty(KEY_IGNORE_STACKED, Boolean.toString(ignoreStackedShulkers)));
            setBlock = props.getProperty(KEY_SET_BLOCK, setBlock);
            save();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loaded = true;
        }
    }

    public static synchronized void save() {
        try {
            Properties props = new Properties();
            props.setProperty(KEY_PREFIX, commandPrefix == null ? "," : commandPrefix);
            props.setProperty(KEY_SHULKER, shulkerColor == null ? "black" : shulkerColor);
            props.setProperty(KEY_WHISPER, autoWhisper == null ? "" : autoWhisper);
            props.setProperty(KEY_GO_TO_GROUND, Boolean.toString(goToGround));
            props.setProperty(KEY_IGNORE_STACKED, Boolean.toString(ignoreStackedShulkers));
            props.setProperty(KEY_SET_BLOCK, setBlock == null ? "" : setBlock);
            try (OutputStream out = new FileOutputStream(getConfigFile())) {
                props.store(out, "DictatorScript configuration");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCommandPrefix() {
        if (!loaded) load();
        return (commandPrefix == null || commandPrefix.isEmpty()) ? "," : commandPrefix;
    }

    public static boolean setCommandPrefix(String prefix) {
        if (prefix == null) return false;
        String p = prefix.trim();
        if (p.isEmpty()) return false;
        commandPrefix = p;
        save();
        return true;
    }

    public static String getAutoWhisper() {
        if (!loaded) load();
        return autoWhisper == null ? "" : autoWhisper;
    }

    public static void setAutoWhisper(String msg) {
        autoWhisper = msg == null ? "" : msg;
        save();
    }

    public static String getShulkerColorRaw() {
        if (!loaded) load();
        return shulkerColor == null ? "black" : shulkerColor;
    }

    public static String getShulkerRegistryId() {
        if (!loaded) load();
        String raw = getShulkerColorRaw();
        String token = normalizeColorToken(raw);
        if (token.contains(":")) {
            return token;
        }
        return "minecraft:" + token + "_shulker_box";
    }

    public static boolean setShulkerColor(String input) {
        if (input == null) return false;
        String token = normalizeColorToken(input);
        if (token.contains(":")) {
            shulkerColor = token;
            save();
            return true;
        }
        switch (token) {
            case "white":
            case "light_gray":
            case "gray":
            case "black":
            case "brown":
            case "red":
            case "orange":
            case "yellow":
            case "lime":
            case "green":
            case "cyan":
            case "light_blue":
            case "blue":
            case "purple":
            case "magenta":
            case "pink":
                shulkerColor = token;
                save();
                return true;
            default:
                return false;
        }
    }

    private static String normalizeColorToken(String s) {
        if (s == null) return "black";
        String v = s.trim().toLowerCase(Locale.ROOT);
        if (v.contains(":")) return v;
        v = v.replace(" ", "").replace("-", "").replace("_", "");
        if (v.equals("lightgray")) return "light_gray";
        if (v.equals("lightblue")) return "light_blue";
        return v;
    }

    public static String getShulkerColorDisplay() {
        String raw = getShulkerColorRaw();
        if (raw.contains(":")) return raw;
        String token = normalizeColorToken(raw);
        if (token.equals("light_gray")) return "Light Gray";
        if (token.equals("light_blue")) return "Light Blue";
        return token.substring(0, 1).toUpperCase(Locale.ROOT) + token.substring(1);
    }

    public static boolean getGoToGround() {
        if (!loaded) load();
        return goToGround;
    }

    public static boolean setGoToGround(String input) {
        if (input == null) return false;
        String v = input.trim().toLowerCase(Locale.ROOT);
        boolean parsed;
        if (v.equals("true") || v.equals("yes") || v.equals("y") || v.equals("1")) {
            parsed = true;
        } else if (v.equals("false") || v.equals("no") || v.equals("n") || v.equals("0")) {
            parsed = false;
        } else {
            return false;
        }
        goToGround = parsed;
        save();
        return true;
    }

    public static boolean setIgnoreStackedShulkers(String input) {
        if (input == null) return false;
        String v = input.trim().toLowerCase(Locale.ROOT);
        boolean parsed;
        if (v.equals("true") || v.equals("yes") || v.equals("y") || v.equals("1")) {
            parsed = true;
        } else if (v.equals("false") || v.equals("no") || v.equals("n") || v.equals("0")) {
            parsed = false;
        } else {
            return false;
        }
        ignoreStackedShulkers = parsed;
        save();
        return true;
    }

    public static boolean getIgnoreStackedShulkers() {
        if (!loaded) load();
        return ignoreStackedShulkers;
    }

    public static String getBroadcastMessage() {
        if (!loaded) load();
        return getAutoWhisper();
    }

    public static String getSetBlock() {
        if (!loaded) load();
        return setBlock == null ? "" : setBlock.trim();
    }

    public static void setSetBlock(String blocks) {
        setBlock = (blocks == null) ? "" : blocks.trim();
        save();
    }
}

