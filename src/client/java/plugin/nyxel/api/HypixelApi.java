package plugin.nyxel.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import plugin.nyxel.api.model.GardenData;
import plugin.nyxel.api.model.PlayerInfo;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.NyxelConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Thin wrapper over the Hypixel API v2 SkyBlock endpoints used by Nyxel:
 * {@code /skyblock/profiles} (active profile + Ironman flag) and
 * {@code /skyblock/garden} (garden experience + unlocked plots).
 *
 * <p>Requests are routed by config: if a backend proxy URL is set they go through
 * it with no client-side key; otherwise they go directly to Hypixel with the
 * personal key. All calls are blocking and run from {@link PlayerDataService}'s
 * background thread.
 */
public final class HypixelApi {

    private static final String BASE = "https://api.hypixel.net/v2";

    private HypixelApi() {
    }

    /**
     * GET a Hypixel v2 endpoint (e.g. {@code "skyblock/profiles?uuid=..."}),
     * routed through the proxy when configured, else directly with the key.
     */
    private static JsonObject call(String relativePath) throws Exception {
        NyxelConfig.Api api = ConfigManager.get().api;
        if (api.proxyUrl != null && !api.proxyUrl.isBlank()) {
            String base = api.proxyUrl.endsWith("/")
                    ? api.proxyUrl.substring(0, api.proxyUrl.length() - 1)
                    : api.proxyUrl;
            return HttpJson.get(base + "/" + relativePath, "X-Nyxel-Token", api.modToken);
        }
        return HttpJson.get(BASE + "/" + relativePath, "API-Key", api.hypixelKey);
    }

    /** Resolve the player's active profile (+ garden) into a {@link PlayerInfo}. */
    public static PlayerInfo fetchPlayer(String undashedUuid) throws Exception {
        JsonObject root = call("skyblock/profiles?uuid=" + undashedUuid);
        if (!bool(root, "success") || !root.has("profiles") || root.get("profiles").isJsonNull()) {
            return PlayerInfo.unavailable();
        }
        JsonArray profiles = root.getAsJsonArray("profiles");
        if (profiles.isEmpty()) {
            return PlayerInfo.unavailable();
        }
        JsonObject selected = null;
        for (JsonElement e : profiles) {
            JsonObject p = e.getAsJsonObject();
            if (bool(p, "selected")) {
                selected = p;
                break;
            }
        }
        if (selected == null) {
            selected = profiles.get(0).getAsJsonObject();
        }
        String profileId = str(selected, "profile_id");
        String cute = str(selected, "cute_name");
        boolean ironman = "ironman".equals(str(selected, "game_mode"));
        GardenData garden = fetchGarden(profileId);
        return new PlayerInfo(true, ironman, profileId, cute, garden);
    }

    private static GardenData fetchGarden(String profileId) {
        try {
            JsonObject root = call("skyblock/garden?profile=" + profileId);
            if (!bool(root, "success") || !root.has("garden")) {
                return GardenData.empty();
            }
            JsonObject g = root.getAsJsonObject("garden");
            long exp = g.has("garden_experience") ? g.get("garden_experience").getAsLong() : 0;
            Set<String> plots = new HashSet<>();
            if (g.has("unlocked_plots") && g.get("unlocked_plots").isJsonArray()) {
                for (JsonElement e : g.getAsJsonArray("unlocked_plots")) {
                    plots.add(e.getAsString());
                }
            }
            return new GardenData(exp, plots);
        } catch (Exception e) {
            return GardenData.empty();
        }
    }

    private static boolean bool(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() && o.get(key).getAsBoolean();
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }
}
