package plugin.nyxel.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import plugin.nyxel.Nyxel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads/saves {@link NyxelConfig} as {@code config/nyxel.json} using Gson (which
 * ships with the game, so no extra dependency). Static singleton accessor.
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH =
            FabricLoader.getInstance().getConfigDir().resolve("nyxel.json");

    private static NyxelConfig config;

    private ConfigManager() {
    }

    public static NyxelConfig get() {
        if (config == null) {
            load();
        }
        return config;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try {
                String json = Files.readString(PATH);
                config = GSON.fromJson(json, NyxelConfig.class);
            } catch (Exception e) {
                Nyxel.LOGGER.error("Failed to read config, using defaults", e);
            }
        }
        if (config == null) {
            config = new NyxelConfig();
            save();
        }
    }

    public static void save() {
        if (config == null) {
            return;
        }
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(config));
        } catch (IOException e) {
            Nyxel.LOGGER.error("Failed to write config", e);
        }
    }
}
