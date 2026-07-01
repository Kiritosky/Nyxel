package plugin.nyxel.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plain serialized config POJO for the baseplate: the feature framework's toggle
 * and per-feature option maps, HUD styling/placement, and the API access block.
 * Feature-specific sections are added back by the features that need them. Primitive
 * / boxed fields only so Gson round-trips cleanly.
 */
public class NyxelConfig {

    /** Per-feature enabled flags, keyed by {@code Feature.id()}. */
    public Map<String, Boolean> featureToggles = new LinkedHashMap<>();

    /**
     * Per-feature option values, keyed by {@code Feature.id()} then option key.
     * Declared via {@code Feature.configOptions()} and accessed through
     * {@link FeatureOptions}. Values are primitives/boxed so Gson round-trips.
     */
    public Map<String, Map<String, Object>> featureOptions = new LinkedHashMap<>();

    /** Persisted HUD element placement, keyed by element id. */
    public Map<String, HudPlacement> hudPlacements = new LinkedHashMap<>();

    public Hud hud = new Hud();
    public Api api = new Api();

    /**
     * Hypixel API access — the baseplate's live-data source. Players never enter a
     * key: every install uses Nyxel's shared backend proxy, which holds the single
     * production key server-side. The direct-key fields exist only for local dev.
     */
    public static class Api {
        /**
         * Backend proxy base URL — baked in so the mod works out of the box for
         * every user (no key, no setup). The proxy holds the production key.
         */
        public String proxyUrl = "https://nyxel-proxy.vercel.app/api/hypixel";
        /** Optional shared token sent to the proxy as {@code X-Nyxel-Token}. */
        public String modToken = "";
        /** Dev-only: personal key for direct calls when {@link #proxyUrl} is cleared. */
        public String hypixelKey = "";
        /** Cache TTL for player/profile data, in seconds. */
        public int cacheSeconds = 300;
    }

    public static class HudPlacement {
        public float x = 5f;
        public float y = 5f;
        public float scale = 1.0f;
        /** ARGB text color override; applied (overriding §-codes) when chroma off. */
        public int color = 0xFFFFFFFF;
        /** Animate the element's text through the color spectrum. */
        public boolean chroma = false;

        public HudPlacement() {
        }

        public HudPlacement(float x, float y, float scale) {
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
    }

    /** Global HUD styling. */
    public static class Hud {
        public boolean textShadow = true;
        public boolean background = true;
    }
}
