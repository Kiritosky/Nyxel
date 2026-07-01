package plugin.nyxel.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plain serialized config POJO. One nested section per area; primitive/boxed
 * fields only so Gson round-trips cleanly. Defaults here are the install-time
 * defaults.
 */
public class NyxelConfig {

    /** Per-feature enabled flags, keyed by {@code Feature.id()}. */
    public Map<String, Boolean> featureToggles = new LinkedHashMap<>();

    /** Persisted HUD element placement, keyed by element id. */
    public Map<String, HudPlacement> hudPlacements = new LinkedHashMap<>();

    public Fishing fishing = new Fishing();
    public Economy economy = new Economy();
    public Hud hud = new Hud();
    public Api api = new Api();
    public Garden garden = new Garden();

    /** Hypixel API access. */
    public static class Api {
        /** Personal Hypixel API key from developer.hypixel.net (optional). */
        public String hypixelKey = "";
        /** Cache TTL for player/profile/garden data, in seconds. */
        public int cacheSeconds = 300;
    }

    /** Greenhouse Mutation Helper settings. */
    public static class Garden {
        /** Use the live garden state from the API to pre-fill the planner. */
        public boolean autoFillFromApi = true;
        /** Show the in-world greenhouse placement overlay (phase 2). */
        public boolean overlay = false;
        /** Optional remote dataset override URL; empty = bundled only. */
        public String datasetUrl = "";
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

    public static class Fishing {
        public boolean seaCreatureSound = true;
        public boolean seaCreatureTitle = true;
        public boolean gearWarnings = true;

        /** Trophy fish catch counts, keyed "fishName:tier" (lower-case tier). */
        public Map<String, Integer> trophyCounts = new LinkedHashMap<>();
    }

    public static class Economy {
        /** Cache TTL for Bazaar/AH data, in seconds. */
        public int priceCacheSeconds = 120;
        public boolean showTooltipPrices = true;
    }
}
