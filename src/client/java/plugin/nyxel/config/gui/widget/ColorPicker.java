package plugin.nyxel.config.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;

/**
 * Compact color picker: a rainbow hue bar plus saturation and value sliders, a
 * preview swatch, and a chroma toggle. Slider-based (rather than a 2D wheel) so it
 * relies only on solid fills. Reused by the HUD editor's per-element panel.
 */
public final class ColorPicker {

    private float hue, sat, val;          // 0..1
    private boolean chroma;
    private float chromaAnim;

    private int lx, ly, lw;               // last laid-out bounds
    private int drag = -1;                // 0 hue, 1 sat, 2 val

    public ColorPicker(int argb, boolean chroma) {
        float[] hsv = rgbToHsv(argb);
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
        this.chroma = chroma;
        this.chromaAnim = chroma ? 1f : 0f;
    }

    public int getColor() {
        return 0xFF000000 | (hsvToRgb(hue, sat, val) & 0xFFFFFF);
    }

    public boolean isChroma() {
        return chroma;
    }

    public int height() {
        return 76;
    }

    public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w) {
        lx = x;
        ly = y;
        lw = w;
        chromaAnim += ((chroma ? 1f : 0f) - chromaAnim) * 0.3f;

        // preview swatch + label
        Render2D.roundedRect(ctx, x, y, 16, 16, 3, getColor());
        ctx.drawText(tr, Text.literal("§7Color"), x + 22, y + 4, NyxelTheme.TEXT, false);

        // hue rainbow bar
        int hbY = y + 20;
        for (int i = 0; i < w; i += 2) {
            int c = 0xFF000000 | hsvToRgb(i / (float) w, 1f, 1f);
            ctx.fill(x + i, hbY, x + Math.min(i + 2, w), hbY + 8, c);
        }
        marker(ctx, x + Math.round(hue * w), hbY, 8);

        // sat + val sliders
        NyxelSlider.render(ctx, x, y + 36, w, sat, NyxelTheme.ACCENT);
        NyxelSlider.render(ctx, x, y + 50, w, val, NyxelTheme.ACCENT);

        // chroma toggle
        int ty = y + 62;
        ctx.drawText(tr, Text.literal("§7Chroma"), x, ty + 2, NyxelTheme.TEXT, false);
        ToggleSwitch.render(ctx, x + w - ToggleSwitch.W, ty, chromaAnim);
    }

    private void marker(DrawContext ctx, int cx, int y, int h) {
        ctx.fill(cx - 1, y - 1, cx + 1, y + h + 1, 0xFFFFFFFF);
    }

    public boolean mouseClicked(double mx, double my, int button) {
        int hbY = ly + 20;
        if (within(mx, my, lx, hbY, lw, 8)) {
            drag = 0;
            setHue(mx);
            return true;
        }
        if (within(mx, my, lx, ly + 36 - 2, lw, 10)) {
            drag = 1;
            setSat(mx);
            return true;
        }
        if (within(mx, my, lx, ly + 50 - 2, lw, 10)) {
            drag = 2;
            setVal(mx);
            return true;
        }
        int ty = ly + 62;
        if (within(mx, my, lx + lw - ToggleSwitch.W, ty, ToggleSwitch.W, ToggleSwitch.H)) {
            chroma = !chroma;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mx, double my) {
        switch (drag) {
            case 0 -> setHue(mx);
            case 1 -> setSat(mx);
            case 2 -> setVal(mx);
            default -> {
                return false;
            }
        }
        return true;
    }

    public void mouseReleased() {
        drag = -1;
    }

    private void setHue(double mx) {
        hue = clamp01((float) (mx - lx) / lw);
    }

    private void setSat(double mx) {
        sat = clamp01((float) (mx - lx) / lw);
    }

    private void setVal(double mx) {
        val = clamp01((float) (mx - lx) / lw);
    }

    private static boolean within(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    // --- HSV/RGB ---

    public static int hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6) % 6;
        float f = h * 6 - (float) Math.floor(h * 6);
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    public static float[] rgbToHsv(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h = 0;
        if (d != 0) {
            if (max == r) {
                h = ((g - b) / d) % 6;
            } else if (max == g) {
                h = (b - r) / d + 2;
            } else {
                h = (r - g) / d + 4;
            }
            h /= 6;
            if (h < 0) {
                h += 1;
            }
        }
        float s = max == 0 ? 0 : d / max;
        return new float[]{h, s, max};
    }
}
