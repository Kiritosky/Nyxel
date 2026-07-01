package plugin.nyxel.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.config.gui.widget.ColorPicker;

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
        return 0xFF000000 | ColorPicker.hsvToRgb(h, 0.8f, 1f);
    }
}
