package plugin.nyxel.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed accessor for the namespaced per-feature option map
 * ({@link NyxelConfig#featureOptions}). Centralizes the Gson quirk that JSON
 * numbers deserialize as {@link Double} and handles missing values via defaults,
 * so features and option rows never touch the raw {@code Map<String,Object>}.
 * Every setter persists immediately, matching the rest of the config system.
 */
public final class FeatureOptions {

    private FeatureOptions() {
    }

    private static Map<String, Object> map(String featureId) {
        return ConfigManager.get().featureOptions
                .computeIfAbsent(featureId, k -> new LinkedHashMap<>());
    }

    public static boolean getBool(String featureId, String key, boolean def) {
        Object v = map(featureId).get(key);
        return v instanceof Boolean b ? b : def;
    }

    public static void setBool(String featureId, String key, boolean value) {
        map(featureId).put(key, value);
        ConfigManager.save();
    }

    public static int getInt(String featureId, String key, int def) {
        Object v = map(featureId).get(key);
        if (v instanceof Number n) {
            return n.intValue();
        }
        return def;
    }

    public static void setInt(String featureId, String key, int value) {
        map(featureId).put(key, value);
        ConfigManager.save();
    }
}
