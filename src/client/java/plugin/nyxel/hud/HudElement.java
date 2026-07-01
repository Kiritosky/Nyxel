package plugin.nyxel.hud;

import net.minecraft.client.gui.DrawContext;

/**
 * A single movable on-screen overlay. Features implement this and register it
 * with the {@link HudManager}. Coordinates passed to {@link #render} are already
 * translated/scaled, so elements draw relative to (0, 0).
 */
public interface HudElement {

    /** Stable id, also the config key for its placement. */
    String id();

    /** Label shown in the HUD edit screen. */
    String displayName();

    /** Whether the element should currently render (e.g. only while fishing). */
    boolean isVisible();

    /** Unscaled width in pixels, used for hit-testing in the edit screen. */
    int width();

    /** Unscaled height in pixels, used for hit-testing in the edit screen. */
    int height();

    /** Draw at the origin; the manager applies translation and scale. */
    void render(DrawContext ctx);
}
