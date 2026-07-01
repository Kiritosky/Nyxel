package plugin.nyxel.config.gui.widget;

import net.minecraft.client.gui.DrawContext;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;

/**
 * Stateless drawer for a horizontal slider (track + accent fill + knob). The
 * caller owns the value/fraction and hit-testing.
 */
public final class NyxelSlider {

    public static final int H = 6;
    public static final int KNOB = 10;

    private NyxelSlider() {
    }

    /** Draw a track of width {@code w} filled to {@code frac} (0..1). */
    public static void render(DrawContext ctx, int x, int y, int w, float frac,
                              int trackColor) {
        frac = Math.max(0f, Math.min(1f, frac));
        Render2D.roundedRect(ctx, x, y, w, H, H / 2, NyxelTheme.TRACK);
        int fill = Math.round(w * frac);
        Render2D.roundedRect(ctx, x, y, fill, H, H / 2, trackColor);
        int kx = x + Math.round((w - KNOB) * frac);
        // knob with a thin accent ring for a more finished look
        Render2D.roundedRect(ctx, kx - 1, y - 3, KNOB + 2, KNOB + 2, (KNOB + 2) / 2, trackColor);
        Render2D.roundedRect(ctx, kx, y - 2, KNOB, KNOB, KNOB / 2, NyxelTheme.KNOB);
    }
}
