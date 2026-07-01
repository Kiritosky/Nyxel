package plugin.nyxel.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * A toggleable unit of functionality. Features are registered with the
 * {@link FeatureManager}, which owns their lifecycle and persisted enabled state.
 *
 * <p>Implementations override only the hooks they need. Heavy work belongs in the
 * per-tick / per-render hooks; one-off setup (registering chat patterns, HUD
 * elements) belongs in {@link #onEnable()}.
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

    /** Called when the feature transitions to enabled (or at startup if already on). */
    default void onEnable() {
    }

    /** Called when the feature transitions to disabled. */
    default void onDisable() {
    }

    /** Per client tick, only while enabled. */
    default void onClientTick(MinecraftClient mc) {
    }

    /** Per HUD render frame, only while enabled. */
    default void onHudRender(DrawContext ctx, float tickDelta) {
    }

    /** Action-bar (overlay) text update, only while enabled. */
    default void onActionBar(String text) {
    }

    /** Config GUI categories. */
    enum Category {
        GARDEN("Garden"),
        FISHING("Fishing"),
        HUD("HUD & Overlays"),
        ECONOMY("Economy"),
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
