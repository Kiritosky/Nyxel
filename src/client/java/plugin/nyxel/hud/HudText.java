package plugin.nyxel.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Centralized HUD text drawing. {@link HudManager} sets the per-element style
 * (global shadow + the element's color/chroma) via {@link #beginStyle} before
 * calling {@code element.render}; elements then draw with {@link #draw} instead of
 * {@code DrawContext.drawText}.
 *
 * <p>When a color override is active (element has chroma on, or a non-white color)
 * the text's {@code §} formatting is stripped and the override color is used;
 * otherwise the {@code §} codes drive the color and only the shadow flag applies.
 */
public final class HudText {

    private static final int DEFAULT = 0xFFFFFFFF;

    private static boolean shadow = true;
    private static boolean override = false;
    private static int color = DEFAULT;

    private HudText() {
    }

    public static void beginStyle(boolean shadow, int placementColor, boolean chroma) {
        HudText.shadow = shadow;
        if (chroma) {
            HudText.color = chromaColor();
            HudText.override = true;
        } else if ((placementColor & 0xFFFFFF) != (DEFAULT & 0xFFFFFF)) {
            HudText.color = 0xFF000000 | (placementColor & 0xFFFFFF);
            HudText.override = true;
        } else {
            HudText.color = DEFAULT;
            HudText.override = false;
        }
    }

    public static void draw(DrawContext ctx, String text, int x, int y) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (override) {
            String plain = text.replaceAll("§.", "");
            ctx.drawText(mc.textRenderer, Text.literal(plain), x, y, color, shadow);
        } else {
            ctx.drawText(mc.textRenderer, Text.literal(text), x, y, DEFAULT, shadow);
        }
    }

    public static int chromaColor() {
        float h = (System.currentTimeMillis() % 3000L) / 3000f;
        return 0xFF000000 | hsvToRgb(h, 0.8f, 1f);
    }

    /** Minimal HSV→RGB (0..1 inputs) returning a 0xRRGGBB int. */
    private static int hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6) % 6;
        float f = h * 6 - (int) (h * 6);
        float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
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
}

