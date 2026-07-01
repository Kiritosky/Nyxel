package plugin.nyxel.hud;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.NyxelConfig;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;
import plugin.nyxel.config.gui.widget.ColorPicker;
import plugin.nyxel.config.gui.widget.NyxelSlider;

/**
 * SkyHanni-style HUD editor: drag elements to reposition (with edge/center
 * snapping + alignment guides), select one to open a side panel with a color/
 * chroma picker, a scale slider, and reset. Scroll over an element to scale it.
 */
public final class HudEditScreen extends Screen {

    private static final int SNAP = 6;
    private static final int PANEL_W = 140;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 2.5f;

    private final HudManager hud;
    private final Screen parent;

    private HudElement dragging;
    private HudElement selected;
    private ColorPicker picker;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean scaleDragging;
    private int guideX = -1;
    private int guideY = -1;

    public HudEditScreen(HudManager hud, Screen parent) {
        super(Text.literal("Nyxel HUD Editor"));
        this.hud = hud;
        this.parent = parent;
    }

    // --- render ---

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        for (HudElement element : hud.elements()) {
            NyxelConfig.HudPlacement p = hud.placement(element.id());
            int w = Math.round(element.width() * p.scale);
            int h = Math.round(element.height() * p.scale);
            boolean sel = element == selected;
            Render2D.roundedRect(ctx, (int) p.x - 2, (int) p.y - 2, w + 4, h + 4, 3,
                    sel ? 0x553355FF : 0x33FFFFFF);
            Render2D.roundedOutline(ctx, (int) p.x - 2, (int) p.y - 2, w + 4, h + 4, 3,
                    sel ? NyxelTheme.ACCENT : 0x55FFFFFF);
            if (element.isVisible()) {
                hud.drawAt(ctx, element);
            } else {
                ctx.drawText(textRenderer, Text.literal(element.displayName()),
                        (int) p.x + 2, (int) p.y + 2, 0xFFAAAAAA, true);
            }
        }

        // alignment guides
        if (guideX >= 0) {
            ctx.fill(guideX, 0, guideX + 1, height, 0x99B14BFF);
        }
        if (guideY >= 0) {
            ctx.fill(0, guideY, width, guideY + 1, 0x99B14BFF);
        }

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7Drag to move · Scroll to scale · Click to select · Esc to save"),
                width / 2, 6, 0xFFFFFFFF);

        if (selected != null && picker != null) {
            renderPanel(ctx, mouseX, mouseY);
        }
    }

    private int panelX() {
        return width - PANEL_W - 8;
    }

    private int panelY() {
        return 30;
    }

    private int scaleSliderY() {
        return panelY() + 28 + picker.height() + 14;
    }

    private int resetY() {
        return scaleSliderY() + 16;
    }

    private void renderPanel(DrawContext ctx, int mouseX, int mouseY) {
        int px = panelX();
        int py = panelY();
        int ph = resetY() + 22 - py;
        Render2D.roundedRect(ctx, px, py, PANEL_W, ph, NyxelTheme.RADIUS, NyxelTheme.PANEL_BG);
        Render2D.roundedOutline(ctx, px, py, PANEL_W, ph, NyxelTheme.RADIUS,
                NyxelTheme.PANEL_BORDER);
        ctx.drawText(textRenderer, Text.literal("§l" + selected.displayName()),
                px + 8, py + 8, NyxelTheme.TEXT, false);

        picker.render(ctx, textRenderer, px + 8, py + 28, PANEL_W - 16);

        NyxelConfig.HudPlacement p = hud.placement(selected.id());
        float frac = (p.scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE);
        ctx.drawText(textRenderer, Text.literal("§7Scale §f" + String.format("%.1f", p.scale)),
                px + 8, scaleSliderY() - 10, NyxelTheme.TEXT, false);
        NyxelSlider.render(ctx, px + 8, scaleSliderY(), PANEL_W - 16, frac, NyxelTheme.ACCENT);

        boolean hover = within(mouseX, mouseY, px + 8, resetY(), PANEL_W - 16, 16);
        Render2D.roundedRect(ctx, px + 8, resetY(), PANEL_W - 16, 16, NyxelTheme.RADIUS,
                hover ? 0x55C04646 : 0x44303040);
        ctx.drawText(textRenderer, Text.literal("Reset"),
                px + PANEL_W / 2 - 12, resetY() + 4, NyxelTheme.TEXT, false);
    }

    // --- input ---

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();

        if (selected != null && picker != null) {
            if (picker.mouseClicked(mx, my, click.button())) {
                applyStyle();
                return true;
            }
            int px = panelX();
            if (within(mx, my, px + 8, scaleSliderY() - 3, PANEL_W - 16, NyxelSlider.H + 6)) {
                scaleDragging = true;
                applyScale(mx);
                return true;
            }
            if (within(mx, my, px + 8, resetY(), PANEL_W - 16, 16)) {
                reset();
                return true;
            }
        }

        // topmost element wins
        for (int i = hud.elements().size() - 1; i >= 0; i--) {
            HudElement element = hud.elements().get(i);
            if (hovering(element, mx, my)) {
                NyxelConfig.HudPlacement p = hud.placement(element.id());
                dragging = element;
                select(element);
                dragOffsetX = (float) mx - p.x;
                dragOffsetY = (float) my - p.y;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        double mx = click.x();
        double my = click.y();
        if (scaleDragging) {
            applyScale(mx);
            return true;
        }
        if (picker != null && picker.mouseDragged(mx, my)) {
            applyStyle();
            return true;
        }
        if (dragging != null) {
            moveWithSnap(mx, my);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = null;
        scaleDragging = false;
        guideX = -1;
        guideY = -1;
        if (picker != null) {
            picker.mouseReleased();
        }
        ConfigManager.save();
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        for (HudElement element : hud.elements()) {
            if (hovering(element, mx, my)) {
                NyxelConfig.HudPlacement p = hud.placement(element.id());
                float scale = MathHelper.clamp(p.scale + (float) v * 0.1f, MIN_SCALE, MAX_SCALE);
                hud.setPlacement(element.id(), p.x, p.y, scale);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    // --- helpers ---

    private void moveWithSnap(double mx, double my) {
        NyxelConfig.HudPlacement p = hud.placement(dragging.id());
        int w = Math.round(dragging.width() * p.scale);
        int h = Math.round(dragging.height() * p.scale);
        float nx = MathHelper.clamp((float) mx - dragOffsetX, 0, width - 4);
        float ny = MathHelper.clamp((float) my - dragOffsetY, 0, height - 4);
        guideX = -1;
        guideY = -1;

        // snap to screen left/center/right
        float[] xs = {0, (width - w) / 2f, width - w};
        for (float c : xs) {
            if (Math.abs(nx - c) <= SNAP) {
                nx = c;
                guideX = Math.round(c);
                break;
            }
        }
        float[] ys = {0, (height - h) / 2f, height - h};
        for (float c : ys) {
            if (Math.abs(ny - c) <= SNAP) {
                ny = c;
                guideY = Math.round(c);
                break;
            }
        }
        // snap to other elements' edges
        for (HudElement other : hud.elements()) {
            if (other == dragging) {
                continue;
            }
            NyxelConfig.HudPlacement op = hud.placement(other.id());
            if (guideX < 0 && Math.abs(nx - op.x) <= SNAP) {
                nx = op.x;
                guideX = Math.round(op.x);
            }
            if (guideY < 0 && Math.abs(ny - op.y) <= SNAP) {
                ny = op.y;
                guideY = Math.round(op.y);
            }
        }
        hud.setPlacement(dragging.id(), nx, ny, p.scale);
    }

    private void select(HudElement element) {
        selected = element;
        NyxelConfig.HudPlacement p = hud.placement(element.id());
        picker = new ColorPicker(p.color, p.chroma);
    }

    private void applyStyle() {
        if (selected != null && picker != null) {
            hud.setStyle(selected.id(), picker.getColor(), picker.isChroma());
        }
    }

    private void applyScale(double mx) {
        NyxelConfig.HudPlacement p = hud.placement(selected.id());
        float frac = MathHelper.clamp((float) (mx - (panelX() + 8)) / (PANEL_W - 16), 0f, 1f);
        hud.setPlacement(selected.id(), p.x, p.y, MIN_SCALE + frac * (MAX_SCALE - MIN_SCALE));
    }

    private void reset() {
        NyxelConfig.HudPlacement def = new NyxelConfig.HudPlacement();
        hud.setPlacement(selected.id(), def.x, def.y, def.scale);
        hud.setStyle(selected.id(), def.color, def.chroma);
        picker = new ColorPicker(def.color, def.chroma);
    }

    private boolean hovering(HudElement element, double mx, double my) {
        NyxelConfig.HudPlacement p = hud.placement(element.id());
        int w = Math.round(element.width() * p.scale);
        int h = Math.round(element.height() * p.scale);
        return mx >= p.x && mx <= p.x + w && my >= p.y && my <= p.y + h;
    }

    private static boolean within(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void close() {
        ConfigManager.save();
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
