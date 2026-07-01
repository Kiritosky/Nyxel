package plugin.nyxel.core;

import plugin.nyxel.config.OptionSpec;

import java.util.List;

/**
 * A toggleable unit of functionality. Features are registered with the
 * {@link FeatureManager}, which owns their lifecycle and persisted enabled state.
 *
 * <p>Implementations override only what they need. Per-tick or action-bar work is
 * opt-in via the {@link TickListener} / {@link ActionBarListener} capability
 * interfaces; HUD rendering is opt-in via {@link plugin.nyxel.hud.HudElement}.
 * One-off setup (registering chat patterns, tooltip callbacks) belongs in
 * {@link #onEnable()}.
 */
public interface Feature {

    /** Stable identifier used as the config key. Must be unique and kebab-case. */
    String id();

    /** Human-readable name shown in the config GUI. */
    String displayName();

    /** Category bucket for the config GUI (e.g. "Fishing", "Economy"). */
    Category category();

    /** One-line description shown under the name in the config GUI. */
    default String description() {
        return "";
    }

    /** Whether this feature defaults to on when first installed. */
    default boolean enabledByDefault() {
        return true;
    }

    /**
     * Declarative per-feature config options, rendered generically under the
     * feature's toggle in the config screen and persisted namespaced under the
     * feature id. Empty by default.
     */
    default List<OptionSpec> configOptions() {
        return List.of();
    }

    /** Called when the feature transitions to enabled (or at startup if already on). */
    default void onEnable() {
    }

    /** Called when the feature transitions to disabled. */
    default void onDisable() {
    }

    /** Config GUI categories. */
    enum Category {
        GARDEN("Garden"),
        FISHING("Fishing"),
        HUD("HUD & Overlays"),
        ECONOMY("Economy"),
        CRAFTING("Crafting & Recipes"),
        DUNGEONS("Dungeons & Slayers"),
        MINING("Mining & Farming"),
        GENERAL("General");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }
}
