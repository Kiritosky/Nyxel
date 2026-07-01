package plugin.nyxel.hud;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.NyxelConfig;
import plugin.nyxel.config.gui.render.Render2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns all {@link HudElement}s, renders them at their persisted placement, and
 * provides read/write of placements for the edit screen. Placements are absolute
 * screen pixels stored in the config keyed by element id.
 */
public final class HudManager {

    private final List<HudElement> elements = new ArrayList<>();

    public void register(HudElement element) {
        elements.add(element);
    }

    public List<HudElement> elements() {
        return elements;
    }

    public NyxelConfig.HudPlacement placement(String id) {
        return ConfigManager.get().hudPlacements
                .computeIfAbsent(id, k -> new NyxelConfig.HudPlacement());
    }

    /** Update position/scale in place, preserving color/chroma. */
    public void setPlacement(String id, float x, float y, float scale) {
        NyxelConfig.HudPlacement p = placement(id);
        p.x = x;
        p.y = y;
        p.scale = scale;
    }

    /** Update color/chroma in place. */
    public void setStyle(String id, int color, boolean chroma) {
        NyxelConfig.HudPlacement p = placement(id);
        p.color = color;
        p.chroma = chroma;
    }

    /** Normal in-game render of all visible elements (called from EventHooks). */
    public void render(DrawContext ctx) {
        for (HudElement element : elements) {
            if (element.isVisible()) {
                drawAt(ctx, element);
            }
        }
    }

    /** Render an element at its placement; shared by HUD render and edit screen. */
    public void drawAt(DrawContext ctx, HudElement element) {
        NyxelConfig cfg = ConfigManager.get();
        NyxelConfig.HudPlacement p = placement(element.id());

        // Rounded background box (drawn in screen space, before the scale transform).
        if (cfg.hud.background) {
            int w = Math.round(element.width() * p.scale);
            int h = Math.round(element.height() * p.scale);
            Render2D.roundedRect(ctx, (int) p.x - 3, (int) p.y - 2,
                    w + 6, h + 4, 4, 0x90101015);
        }

        HudText.beginStyle(cfg.hud.textShadow, p.color, p.chroma);

        Matrix3x2fStack matrices = ctx.getMatrices();
        matrices.pushMatrix();
        matrices.translate(p.x, p.y);
        matrices.scale(p.scale, p.scale);
        element.render(ctx);
        matrices.popMatrix();
    }
}
