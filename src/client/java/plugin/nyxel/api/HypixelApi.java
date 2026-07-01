package plugin.nyxel.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import plugin.nyxel.api.model.PlayerInfo;

import java.util.Map;

/**
 * Thin, typed helpers over {@link ApiClient} for the Hypixel SkyBlock endpoints.
 * Resolves the player's active profile (+ Ironman flag). Blocking; runs from
 * {@link PlayerDataService}'s background task.
 */
public final class HypixelApi {

    private HypixelApi() {
    }

    /** Resolve the player's active profile into a {@link PlayerInfo}. */
    public static PlayerInfo fetchPlayer(String undashedUuid) throws Exception {
        JsonObject root = ApiClient.get("skyblock/profiles", Map.of("uuid", undashedUuid));
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
        return new PlayerInfo(true, ironman, profileId, cute);
    }

    private static boolean bool(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() && o.get(key).getAsBoolean();
    }

    private static String str(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }
}
