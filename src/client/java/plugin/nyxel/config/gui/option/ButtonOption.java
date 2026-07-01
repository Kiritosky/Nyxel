package plugin.nyxel.config.gui.option;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;

/** A row whose control is a clickable button on the right. */
public final class ButtonOption extends OptionRow {

    private final String label;
    private final Runnable action;

    public ButtonOption(String name, String description, String label, Runnable action) {
        super(name, description);
        this.label = label;
        this.action = action;
    }

    private int btnW() {
        return Math.max(60, Math.min(120, rw / 3));
    }

    private int btnX() {
        return rx + rw - NyxelTheme.PAD - btnW();
    }

    private int btnY() {
        return ry + (height() - 18) / 2;
    }

    @Override
    protected void renderControl(DrawContext ctx, TextRenderer tr, int x, int y, int w,
                                 int mouseX, int mouseY, float delta) {
        boolean hover = within(mouseX, mouseY, btnX(), btnY(), btnW(), 18);
        Render2D.roundedRect(ctx, btnX(), btnY(), btnW(), 18, NyxelTheme.RADIUS,
                hover ? 0x55B14BFF : 0x44303040);
        Render2D.roundedOutline(ctx, btnX(), btnY(), btnW(), 18, NyxelTheme.RADIUS,
                NyxelTheme.ACCENT);
        int tw = tr.getWidth(label);
        ctx.drawText(tr, Text.literal(label), btnX() + (btnW() - tw) / 2, btnY() + 5,
                NyxelTheme.TEXT, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (within(mx, my, btnX(), btnY(), btnW(), 18)) {
            action.run();
            return true;
        }
        return false;
    }
}
