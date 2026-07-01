package plugin.nyxel.config.gui.option;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;

import java.util.Locale;

/**
 * One row in the config screen's right pane: a name, an optional description, and
 * a control drawn on the right. Subclasses implement the control + interaction.
 * Geometry from the last {@code render} is cached so the mouse handlers (which
 * receive no coordinates of their own) can hit-test.
 */
public abstract class OptionRow {

    protected final String name;
    protected final String description;

    protected int rx, ry, rw;

    protected OptionRow(String name, String description) {
        this.name = name;
        this.description = description == null ? "" : description;
    }

    public int height() {
        return NyxelTheme.ROW_HEIGHT;
    }

    /** True if the row matches a search query (name or description, §-stripped). */
    public boolean matches(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        return strip(name).toLowerCase(Locale.ROOT).contains(q)
                || strip(description).toLowerCase(Locale.ROOT).contains(q);
    }

    public void render(DrawContext ctx, TextRenderer tr, int x, int y, int w,
                       int mouseX, int mouseY, float delta) {
        rx = x;
        ry = y;
        rw = w;
        boolean hover = within(mouseX, mouseY, x, y, w, height());
        Render2D.roundedRect(ctx, x, y, w, height(), NyxelTheme.RADIUS,
                hover ? NyxelTheme.ROW_HOVER : NyxelTheme.ROW_BG);
        ctx.drawText(tr, Text.literal(name), x + NyxelTheme.PAD, y + 6,
                NyxelTheme.TEXT, false);
        if (!description.isEmpty()) {
            ctx.drawText(tr, Text.literal(description), x + NyxelTheme.PAD, y + 19,
                    NyxelTheme.TEXT_MUTED, false);
        }
        renderControl(ctx, tr, x, y, w, mouseX, mouseY, delta);
    }

    protected abstract void renderControl(DrawContext ctx, TextRenderer tr,
                                          int x, int y, int w,
                                          int mouseX, int mouseY, float delta);

    public boolean mouseClicked(double mx, double my, int button) {
        return false;
    }

    public boolean mouseDragged(double mx, double my, int button) {
        return false;
    }

    public void mouseReleased() {
    }

    protected static boolean within(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    protected static String strip(String s) {
        return s.replaceAll("§.", "");
    }
}
