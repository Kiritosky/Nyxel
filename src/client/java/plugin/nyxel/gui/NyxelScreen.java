package plugin.nyxel.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Nyxel's config screen: a centered panel with a brand-gradient bar, a category
 * sidebar and scrollable toggle cards. Categories are {@link Feature.Category};
 * each feature in a category shows as a toggle, so the screen extends itself as
 * features are added — no screen edits needed.
 *
 * <p>Design ported from the MIT-licensed <b>SkyOS</b> project
 * (https://github.com/olb-freelocs/skyos, Copyright (c) 2025 Freelocs) — see
 * {@code licenses/skyos-MIT.txt}. Translated from Kotlin/Mojang-mappings to
 * Java/Yarn and rewired onto Nyxel's feature framework.
 */
public final class NyxelScreen extends Screen {

    // Layout
    private static final int SIDEBAR_W = 160;
    private static final int HEADER_H = 52;
    private static final int GRADIENT_H = 3;
    private static final int ITEM_H = 22;
    private static final int OPTION_H = 44;
    private static final int PADDING = 12;
    private static final int TOGGLE_W = 34;
    private static final int TOGGLE_H = 14;
    private static final int KNOB_PAD = 2;

    private final FeatureManager features;
    private final Screen parent;
    private final Feature.Category[] categories = Feature.Category.values();

    private int selectedCategory = 0;
    private int contentScroll = 0;
    private int sidebarScroll = 0;

    private int panelX, panelY, panelW, panelH;

    public NyxelScreen(FeatureManager features, Screen parent) {
        super(Text.literal("Nyxel"));
        this.features = features;
        this.parent = parent;
    }

    // A single toggle row in the content area.
    private record Row(String label, String description,
                       BooleanSupplier getter, Consumer<Boolean> setter) {
    }

    /** Rows for the selected category: one toggle per feature in it. */
    private List<Row> rowsFor(Feature.Category cat) {
        List<Row> rows = new ArrayList<>();
        for (Feature f : features.all()) {
            if (f.category() == cat) {
                rows.add(new Row(f.displayName(), f.description(),
                        () -> features.isEnabled(f.id()),
                        v -> features.setEnabled(f.id(), v)));
            }
        }
        return rows;
    }

    @Override
    protected void init() {
        panelW = clamp((int) (width * 0.56), 480, 900);
        panelH = clamp((int) (height * 0.80), 340, 700);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    // ── Rendering ──────────────────────────────────────────────────────────────

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, NyxelColors.BG_OVERLAY);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, NyxelColors.BG_PANEL);

        // Brand gradient bar
        float segW = panelW / (float) NyxelColors.BRAND_GRADIENT.length;
        for (int i = 0; i < NyxelColors.BRAND_GRADIENT.length; i++) {
            int x1 = panelX + (int) (i * segW);
            int x2 = panelX + (int) ((i + 1) * segW);
            ctx.fill(x1, panelY, x2, panelY + GRADIENT_H, NyxelColors.BRAND_GRADIENT[i]);
        }

        drawBorder(ctx, panelX, panelY, panelW, panelH, NyxelColors.BORDER);

        // Sidebar background + divider
        ctx.fill(panelX, panelY + GRADIENT_H, panelX + SIDEBAR_W, panelY + panelH,
                NyxelColors.BG_SIDEBAR);
        ctx.fill(panelX + SIDEBAR_W, panelY + GRADIENT_H, panelX + SIDEBAR_W + 1,
                panelY + panelH, NyxelColors.BORDER);

        renderHeader(ctx);
        renderSidebar(ctx, mouseX, mouseY);
        renderContent(ctx, mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderHeader(DrawContext ctx) {
        int hx = panelX + PADDING;
        int hy = panelY + GRADIENT_H;
        int titleY = hy + (HEADER_H - 8) / 2;
        ctx.drawText(textRenderer, Text.literal("§fNyx§bel"), hx, titleY - 5,
                NyxelColors.FG_PRIMARY, false);
        ctx.drawText(textRenderer, Text.literal("§7SkyBlock QoL"), hx, titleY + 6,
                NyxelColors.FG_DISABLED, false);
        ctx.fill(panelX, panelY + GRADIENT_H + HEADER_H, panelX + SIDEBAR_W,
                panelY + GRADIENT_H + HEADER_H + 1, NyxelColors.BORDER);
    }

    private void renderSidebar(DrawContext ctx, int mx, int my) {
        int sx = panelX;
        int sy = panelY + GRADIENT_H + HEADER_H + 4;
        ctx.enableScissor(panelX, sy, panelX + SIDEBAR_W, panelY + panelH);

        for (int i = 0; i < categories.length; i++) {
            int itemY = sy + i * ITEM_H - sidebarScroll;
            if (itemY + ITEM_H < sy || itemY > panelY + panelH) {
                continue;
            }
            boolean selected = i == selectedCategory;
            boolean hovered = mx >= sx && mx < sx + SIDEBAR_W && my >= itemY && my < itemY + ITEM_H;

            if (selected) {
                ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, NyxelColors.SIDEBAR_ACTIVE);
                ctx.fill(sx, itemY, sx + 2, itemY + ITEM_H, NyxelColors.accentFor(i));
            } else if (hovered) {
                ctx.fill(sx, itemY, sx + SIDEBAR_W, itemY + ITEM_H, NyxelColors.SIDEBAR_HOVER);
            }
            int color = selected ? NyxelColors.FG_PRIMARY : NyxelColors.FG_SECONDARY;
            ctx.drawText(textRenderer, categories[i].label(), sx + PADDING,
                    itemY + (ITEM_H - 8) / 2, color, false);
        }
        ctx.disableScissor();
    }

    private void renderContent(DrawContext ctx, int mx, int my) {
        int cx = panelX + SIDEBAR_W + 1;
        int contentW = panelW - SIDEBAR_W - 1;
        int cy = panelY + GRADIENT_H + HEADER_H + 1;
        int contentH = panelH - GRADIENT_H - HEADER_H - 1;

        ctx.enableScissor(cx, cy, cx + contentW, cy + contentH);

        int accent = NyxelColors.accentFor(selectedCategory);
        ctx.drawText(textRenderer, Text.literal(categories[selectedCategory].label()),
                cx + PADDING, cy + PADDING - contentScroll, accent, false);

        List<Row> rows = rowsFor(categories[selectedCategory]);
        if (rows.isEmpty()) {
            String msg = "No features here yet.";
            ctx.drawText(textRenderer, Text.literal(msg),
                    cx + (contentW - textRenderer.getWidth(msg)) / 2, cy + contentH / 2,
                    NyxelColors.FG_DISABLED, false);
        } else {
            int optY = cy + PADDING + 16 - contentScroll;
            for (Row row : rows) {
                renderRow(ctx, row, cx + PADDING, optY, contentW - PADDING * 2, mx, my);
                optY += OPTION_H;
            }
        }
        ctx.disableScissor();
    }

    private void renderRow(DrawContext ctx, Row row, int x, int y, int w, int mx, int my) {
        ctx.fill(x, y, x + w, y + OPTION_H - 4, NyxelColors.BG_CARD);
        drawBorder(ctx, x, y, w, OPTION_H - 4, NyxelColors.BORDER);
        ctx.drawText(textRenderer, Text.literal(row.label()), x + PADDING, y + 8,
                NyxelColors.FG_PRIMARY, false);
        if (!row.description().isEmpty()) {
            ctx.drawText(textRenderer, Text.literal(row.description()), x + PADDING, y + 20,
                    NyxelColors.FG_MUTED, false);
        }
        int tX = x + w - TOGGLE_W - PADDING;
        int tY = y + (OPTION_H - 4 - TOGGLE_H) / 2;
        renderToggle(ctx, tX, tY, row.getter().getAsBoolean());
    }

    private void renderToggle(DrawContext ctx, int x, int y, boolean on) {
        int knob = TOGGLE_H - KNOB_PAD * 2;
        if (on) {
            int mid = x + TOGGLE_W / 2;
            ctx.fill(x, y, mid, y + TOGGLE_H, NyxelColors.TOGGLE_ON_L);
            ctx.fill(mid, y, x + TOGGLE_W, y + TOGGLE_H, NyxelColors.TOGGLE_ON_R);
            int kx = x + TOGGLE_W - KNOB_PAD - knob;
            ctx.fill(kx, y + KNOB_PAD, kx + knob, y + TOGGLE_H - KNOB_PAD, NyxelColors.TOGGLE_KNOB);
        } else {
            ctx.fill(x, y, x + TOGGLE_W, y + TOGGLE_H, NyxelColors.TOGGLE_OFF);
            ctx.fill(x + KNOB_PAD, y + KNOB_PAD, x + KNOB_PAD + knob, y + TOGGLE_H - KNOB_PAD,
                    NyxelColors.TOGGLE_KNOB);
        }
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) {
            return super.mouseClicked(click, doubled);
        }
        int mx = (int) click.x();
        int my = (int) click.y();

        // Sidebar category selection
        int sy = panelY + GRADIENT_H + HEADER_H + 4;
        if (mx >= panelX && mx < panelX + SIDEBAR_W && my >= sy) {
            int idx = (my - sy + sidebarScroll) / ITEM_H;
            if (idx >= 0 && idx < categories.length) {
                selectedCategory = idx;
                contentScroll = 0;
                return true;
            }
        }

        // Toggle clicks
        int cx = panelX + SIDEBAR_W + 1 + PADDING;
        int contentW = panelW - SIDEBAR_W - 1 - PADDING * 2;
        int cy = panelY + GRADIENT_H + HEADER_H + 1 + PADDING;
        List<Row> rows = rowsFor(categories[selectedCategory]);
        for (int i = 0; i < rows.size(); i++) {
            int optY = cy + 16 + i * OPTION_H - contentScroll;
            int tX = cx + contentW - TOGGLE_W;
            int tY = optY + (OPTION_H - 4 - TOGGLE_H) / 2;
            if (mx >= tX && mx < tX + TOGGLE_W && my >= tY && my < tY + TOGGLE_H) {
                Row row = rows.get(i);
                row.setter().accept(!row.getter().getAsBoolean());
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int delta = -(int) (vertical * 8);
        if (mouseX < panelX + SIDEBAR_W) {
            sidebarScroll = Math.max(0, sidebarScroll + delta);
        } else {
            contentScroll = Math.max(0, contentScroll + delta);
        }
        return true;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
