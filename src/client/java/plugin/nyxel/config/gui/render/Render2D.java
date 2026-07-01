package plugin.nyxel.config.gui.render;

import net.minecraft.client.gui.DrawContext;

/**
 * 2D drawing helpers for the Nyxel GUI. Rounded corners are genuinely
 * anti-aliased: each corner pixel is filled with an alpha scaled by how much of it
 * falls inside the corner arc (coverage), which is what gives MoulConfig/SkyHanni
 * their smooth, non-blocky panels. All drawing goes through {@link DrawContext}.
 */
public final class Render2D {

    private Render2D() {
    }

    /** Filled rounded rectangle with anti-aliased corners. */
    public static void roundedRect(DrawContext ctx, int x, int y, int w, int h,
                                   int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        int x2 = x + w;
        int y2 = y + h;
        if (r == 0) {
            ctx.fill(x, y, x2, y2, color);
            return;
        }
        // Body: middle band (full width) + top/bottom bands between the corners.
        ctx.fill(x, y + r, x2, y2 - r, color);
        ctx.fill(x + r, y, x2 - r, y + r, color);
        ctx.fill(x + r, y2 - r, x2 - r, y2, color);
        // Four anti-aliased corner arcs.
        corner(ctx, x + r, y + r, r, x, y, color);              // TL
        corner(ctx, x2 - r, y + r, r, x2 - r, y, color);        // TR
        corner(ctx, x + r, y2 - r, r, x, y2 - r, color);        // BL
        corner(ctx, x2 - r, y2 - r, r, x2 - r, y2 - r, color);  // BR
    }

    /** Anti-aliased 1px rounded outline. */
    public static void roundedOutline(DrawContext ctx, int x, int y, int w, int h,
                                      int r, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        int x2 = x + w;
        int y2 = y + h;
        ctx.fill(x + r, y, x2 - r, y + 1, color);          // top
        ctx.fill(x + r, y2 - 1, x2 - r, y2, color);        // bottom
        ctx.fill(x, y + r, x + 1, y2 - r, color);          // left
        ctx.fill(x2 - 1, y + r, x2, y2 - r, color);        // right
        ringCorner(ctx, x + r, y + r, r, x, y, color);
        ringCorner(ctx, x2 - r, y + r, r, x2 - r, y, color);
        ringCorner(ctx, x + r, y2 - r, r, x, y2 - r, color);
        ringCorner(ctx, x2 - r, y2 - r, r, x2 - r, y2 - r, color);
    }

    /** Soft drop shadow behind a rounded rect: concentric fading rings. */
    public static void shadow(DrawContext ctx, int x, int y, int w, int h, int r,
                              int spread) {
        for (int i = spread; i >= 1; i--) {
            int alpha = Math.max(0, 60 - i * (60 / (spread + 1)));
            int color = (alpha << 24);
            roundedRect(ctx, x - i, y - i + 2, w + i * 2, h + i * 2, r + i, color);
        }
    }

    /** Vertical gradient fill (straight corners). */
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

    // --- corner rasterization ---

    /** Fill the r×r box at (bx,by) with a disk of radius r centred at (cx,cy). */
    private static void corner(DrawContext ctx, int cx, int cy, int r,
                               int bx, int by, int color) {
        for (int px = bx; px < bx + r; px++) {
            for (int py = by; py < by + r; py++) {
                double d = dist(px + 0.5, py + 0.5, cx, cy);
                double cov = clamp(r - d + 0.5);
                if (cov > 0) {
                    fill(ctx, px, py, scaleAlpha(color, cov));
                }
            }
        }
    }

    /** Like {@link #corner} but only the 1px-wide arc ring (for outlines). */
    private static void ringCorner(DrawContext ctx, int cx, int cy, int r,
                                   int bx, int by, int color) {
        for (int px = bx; px < bx + r; px++) {
            for (int py = by; py < by + r; py++) {
                double d = dist(px + 0.5, py + 0.5, cx, cy);
                double cov = Math.min(clamp(r - d + 0.5), clamp(d - (r - 1) + 0.5));
                if (cov > 0) {
                    fill(ctx, px, py, scaleAlpha(color, cov));
                }
            }
        }
    }

    private static void fill(DrawContext ctx, int px, int py, int color) {
        ctx.fill(px, py, px + 1, py + 1, color);
    }

    private static int scaleAlpha(int color, double coverage) {
        int a = (color >>> 24) & 0xFF;
        int na = (int) Math.round(a * clamp(coverage));
        return (na << 24) | (color & 0x00FFFFFF);
    }

    private static double dist(double px, double py, double cx, double cy) {
        double dx = px - cx;
        double dy = py - cy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static double clamp(double v) {
        return v < 0 ? 0 : Math.min(v, 1);
    }
}
