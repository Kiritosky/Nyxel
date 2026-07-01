package plugin.nyxel.config.gui.widget;

import net.minecraft.client.gui.DrawContext;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;

/**
 * Stateless drawer for the animated pill toggle. The caller owns the {@code anim}
 * value (0 = off side, 1 = on side) and eases it toward the boolean each frame.
 */
public final class ToggleSwitch {

    public static final int W = 28;
    public static final int H = 14;

    private ToggleSwitch() {
    }

    public static void render(DrawContext ctx, int x, int y, float anim) {
        int track = lerpColor(NyxelTheme.ACCENT_OFF, NyxelTheme.ACCENT_ON, anim);
        Render2D.roundedRect(ctx, x, y, W, H, H / 2, track);
        // subtle inner glow on the "on" side so the green reads as active
        if (anim > 0.02f) {
            int glow = (Math.round(0x40 * anim) << 24) | (NyxelTheme.ACCENT_ON & 0x00FFFFFF);
            Render2D.roundedRect(ctx, x + 1, y + 1, W - 2, H - 2, (H - 2) / 2, glow);
        }
        int knob = H - 4;
        int knobX = x + 2 + Math.round((W - 4 - knob) * anim);
        Render2D.roundedRect(ctx, knobX, y + 2, knob, knob, knob / 2, NyxelTheme.KNOB);
    }

    /** Linear interpolate two ARGB colors. */
    public static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int oa = Math.round(aa + (ba - aa) * t);
        int or = Math.round(ar + (br - ar) * t);
        int og = Math.round(ag + (bg - ag) * t);
        int ob = Math.round(ab + (bb - ab) * t);
        return (oa << 24) | (or << 16) | (og << 8) | ob;
    }
}
