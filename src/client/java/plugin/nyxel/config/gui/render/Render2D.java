package plugin.nyxel.config.gui.render;

import net.minecraft.client.gui.DrawContext;

/**
 * Small 2D drawing helpers for the Nyxel GUI. Rounded corners are approximated by
 * omitting the {@code r×r} corner squares of an otherwise-filled rectangle —
 * cheap and good enough for the soft-corner MoulConfig look. Uses
 * {@link DrawContext#fill} / {@code fillGradient} / scissor.
 */
public final class Render2D {

    private Render2D() {
    }

    /** Filled rounded rectangle. */
    public static void roundedRect(DrawContext ctx, int x, int y, int w, int h,
                                   int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        int x2 = x + w;
        int y2 = y + h;
        // center column (full height), then left/right strips inset vertically
        ctx.fill(x + r, y, x2 - r, y2, color);
        ctx.fill(x, y + r, x + r, y2 - r, color);
        ctx.fill(x2 - r, y + r, x2, y2 - r, color);
    }

    /** 1px rounded outline. */
    public static void roundedOutline(DrawContext ctx, int x, int y, int w, int h,
                                      int r, int color) {
        int x2 = x + w;
        int y2 = y + h;
        ctx.fill(x + r, y, x2 - r, y + 1, color);          // top
        ctx.fill(x + r, y2 - 1, x2 - r, y2, color);        // bottom
        ctx.fill(x, y + r, x + 1, y2 - r, color);          // left
        ctx.fill(x2 - 1, y + r, x2, y2 - r, color);        // right
        // corner pixels to soften
        ctx.fill(x + 1, y + 1, x + r, y + 2, color);
        ctx.fill(x2 - r, y + 1, x2 - 1, y + 2, color);
        ctx.fill(x + 1, y2 - 2, x + r, y2 - 1, color);
        ctx.fill(x2 - r, y2 - 2, x2 - 1, y2 - 1, color);
    }

    /** Vertical gradient fill. */
    public static void verticalGradient(DrawContext ctx, int x, int y, int w, int h,
                                        int top, int bottom) {
        ctx.fillGradient(x, y, x + w, y + h, top, bottom);
    }

    public static void enableScissor(DrawContext ctx, int x, int y, int w, int h) {
        ctx.enableScissor(x, y, x + w, y + h);
    }

    public static void disableScissor(DrawContext ctx) {
        ctx.disableScissor();
    }
}
