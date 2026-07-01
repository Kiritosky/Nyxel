package plugin.nyxel.config.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;

/** Stateless drawer for a sidebar category entry. */
public final class CategoryButton {

    public static final int H = 22;

    private CategoryButton() {
    }

    public static void render(DrawContext ctx, TextRenderer tr, int x, int y, int w,
                              String label, boolean selected, boolean hover) {
        if (selected) {
            Render2D.roundedRect(ctx, x, y, w, H, 4, NyxelTheme.ACCENT_SOFT);
            Render2D.roundedRect(ctx, x, y + 3, 3, H - 6, 1, NyxelTheme.ACCENT); // accent bar
        } else if (hover) {
            Render2D.roundedRect(ctx, x, y, w, H, 4, NyxelTheme.ROW_HOVER);
        }
        int color = selected ? NyxelTheme.TEXT : NyxelTheme.TEXT_MUTED;
        ctx.drawText(tr, Text.literal(label), x + 10, y + (H - 8) / 2, color, false);
    }
}
