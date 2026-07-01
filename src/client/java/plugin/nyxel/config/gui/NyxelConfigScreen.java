package plugin.nyxel.config.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.NyxelConfig;
import plugin.nyxel.config.gui.option.ButtonOption;
import plugin.nyxel.config.gui.option.FeatureToggleOption;
import plugin.nyxel.config.gui.option.OptionRow;
import plugin.nyxel.config.gui.option.SliderOption;
import plugin.nyxel.config.gui.option.ToggleOption;
import plugin.nyxel.config.gui.render.Render2D;
import plugin.nyxel.config.gui.widget.CategoryButton;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureManager;
import plugin.nyxel.feature.garden.MutationHelperFeature;
import plugin.nyxel.feature.garden.gui.ApiKeyScreen;
import plugin.nyxel.hud.HudEditScreen;
import plugin.nyxel.hud.HudManager;

import java.util.ArrayList;
import java.util.List;

/**
 * SkyHanni/MoulConfig-style settings screen: a dark rounded window with a
 * category sidebar, a search field, and a scrollable list of themed option rows.
 */
public final class NyxelConfigScreen extends Screen {

    private static final int WIN_W = 440;
    private static final int WIN_H = 270;
    private static final int ROW_GAP = 4;

    private final FeatureManager features;
    private final HudManager hud;
    private final Screen parent;

    private Feature.Category currentTab = Feature.Category.GARDEN;
    private final List<OptionRow> rows = new ArrayList<>();
    private TextFieldWidget search;
    private float scroll;

    public NyxelConfigScreen(FeatureManager features, HudManager hud, Screen parent) {
        super(Text.literal("Nyxel"));
        this.features = features;
        this.hud = hud;
        this.parent = parent;
    }

    // --- geometry ---
    private int left() {
        return (width - WIN_W) / 2;
    }

    private int top() {
        return (height - WIN_H) / 2;
    }

    private int contentX() {
        return left() + NyxelTheme.SIDEBAR_W;
    }

    private int contentY() {
        return top() + NyxelTheme.HEADER_H;
    }

    private int viewportX() {
        return contentX() + NyxelTheme.PAD;
    }

    private int viewportY() {
        return contentY() + NyxelTheme.PAD;
    }

    private int viewportW() {
        return WIN_W - NyxelTheme.SIDEBAR_W - NyxelTheme.PAD * 2;
    }

    private int viewportH() {
        return WIN_H - NyxelTheme.HEADER_H - NyxelTheme.PAD * 2;
    }

    @Override
    protected void init() {
        int sw = 150;
        search = new TextFieldWidget(textRenderer,
                left() + WIN_W - sw - NyxelTheme.PAD, top() + 8, sw, 14,
                Text.literal("Search"));
        search.setDrawsBackground(false);
        search.setPlaceholder(Text.literal("§7Search..."));
        search.setChangedListener(s -> {
            scroll = 0;
            buildRows();
        });
        addDrawableChild(search);
        buildRows();
    }

    private void buildRows() {
        rows.clear();
        String q = search == null ? "" : search.getText().trim();
        if (q.isEmpty()) {
            for (Feature f : features.all()) {
                if (f.category() == currentTab) {
                    rows.add(new FeatureToggleOption(features, f));
                }
            }
            addCategoryExtras(currentTab);
        } else {
            for (Feature f : features.all()) {
                FeatureToggleOption o = new FeatureToggleOption(features, f);
                if (o.matches(q)) {
                    rows.add(o);
                }
            }
        }
    }

    private void addCategoryExtras(Feature.Category cat) {
        NyxelConfig cfg = ConfigManager.get();
        switch (cat) {
            case GARDEN -> {
                rows.add(new ButtonOption("Mutation Planner",
                        "Open the greenhouse mutation planner", "Open", () -> {
                    if (features.byId(MutationHelperFeature.ID)
                            instanceof MutationHelperFeature mh) {
                        mh.openPlanner(this);
                    }
                }));
                rows.add(new ButtonOption("Hypixel API Key",
                        "Enable live Ironman / garden data", "Set",
                        () -> client.setScreen(new ApiKeyScreen(this))));
                rows.add(new ToggleOption("Auto-fill from API",
                        "Use the live garden state in the planner",
                        () -> cfg.garden.autoFillFromApi,
                        v -> cfg.garden.autoFillFromApi = v));
            }
            case FISHING -> rows.add(new ToggleOption("Sea Creature Sound",
                    "Play a sound on sea-creature alerts",
                    () -> cfg.fishing.seaCreatureSound,
                    v -> cfg.fishing.seaCreatureSound = v));
            case HUD -> {
                rows.add(new ToggleOption("Text Shadow", "Drop shadow on HUD text",
                        () -> cfg.hud.textShadow, v -> cfg.hud.textShadow = v));
                rows.add(new ToggleOption("Background Box",
                        "Rounded background behind HUD elements",
                        () -> cfg.hud.background, v -> cfg.hud.background = v));
                rows.add(new ButtonOption("HUD Editor",
                        "Move, scale and recolor HUD elements", "Edit",
                        () -> client.setScreen(new HudEditScreen(hud, this))));
            }
            case ECONOMY -> rows.add(new SliderOption("Price Cache (s)",
                    "How long Bazaar prices are cached", 30, 600,
                    () -> cfg.economy.priceCacheSeconds,
                    v -> cfg.economy.priceCacheSeconds = v));
            default -> {
            }
        }
    }

    private int totalHeight() {
        int h = 0;
        for (OptionRow r : rows) {
            h += r.height() + ROW_GAP;
        }
        return h;
    }

    private int maxScroll() {
        return Math.max(0, totalHeight() - viewportH());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.renderBackground(ctx, mouseX, mouseY, delta);
        int l = left(), t = top();
        // window + header + sidebar
        Render2D.roundedRect(ctx, l, t, WIN_W, WIN_H, NyxelTheme.RADIUS, NyxelTheme.PANEL_BG);
        Render2D.roundedOutline(ctx, l, t, WIN_W, WIN_H, NyxelTheme.RADIUS,
                NyxelTheme.PANEL_BORDER);
        ctx.fill(l, t, l + WIN_W, t + NyxelTheme.HEADER_H, NyxelTheme.HEADER_BG);
        ctx.fill(l, t + NyxelTheme.HEADER_H, l + NyxelTheme.SIDEBAR_W, t + WIN_H,
                NyxelTheme.SIDEBAR_BG);
        ctx.drawText(textRenderer, Text.literal("§l§dNyxel"), l + NyxelTheme.PAD,
                t + 11, NyxelTheme.TEXT, false);
        // search field background
        Render2D.roundedRect(ctx, search.getX() - 4, search.getY() - 3,
                search.getWidth() + 8, 20, NyxelTheme.RADIUS, NyxelTheme.SEARCH_BG);

        // sidebar categories
        Feature.Category[] cats = Feature.Category.values();
        for (int i = 0; i < cats.length; i++) {
            int bx = l + 6;
            int by = t + NyxelTheme.HEADER_H + 6 + i * (CategoryButton.H + 2);
            boolean hover = mouseX >= bx && mouseX <= bx + NyxelTheme.SIDEBAR_W - 12
                    && mouseY >= by && mouseY <= by + CategoryButton.H;
            CategoryButton.render(ctx, textRenderer, bx, by,
                    NyxelTheme.SIDEBAR_W - 12, cats[i].label(),
                    cats[i] == currentTab, hover);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta); // background (panel) + search widget

        // scrollable option list, clipped to the viewport
        Render2D.enableScissor(ctx, viewportX(), viewportY(), viewportW(), viewportH());
        int y = viewportY() - Math.round(scroll);
        for (OptionRow r : rows) {
            r.render(ctx, textRenderer, viewportX(), y, viewportW(), mouseX, mouseY, delta);
            y += r.height() + ROW_GAP;
        }
        Render2D.disableScissor(ctx);

        // scrollbar
        int ms = maxScroll();
        if (ms > 0) {
            int trackH = viewportH();
            int thumbH = Math.max(20, (int) (trackH * (viewportH() / (float) totalHeight())));
            int thumbY = viewportY() + (int) ((trackH - thumbH) * (scroll / ms));
            Render2D.roundedRect(ctx, viewportX() + viewportW() + 2, thumbY, 3, thumbH, 1,
                    NyxelTheme.ACCENT);
        }
    }

    private boolean inViewport(double mx, double my) {
        return mx >= viewportX() && mx <= viewportX() + viewportW()
                && my >= viewportY() && my <= viewportY() + viewportH();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        // sidebar tab selection
        Feature.Category[] cats = Feature.Category.values();
        for (int i = 0; i < cats.length; i++) {
            int bx = left() + 6;
            int by = top() + NyxelTheme.HEADER_H + 6 + i * (CategoryButton.H + 2);
            if (mx >= bx && mx <= bx + NyxelTheme.SIDEBAR_W - 12
                    && my >= by && my <= by + CategoryButton.H) {
                currentTab = cats[i];
                scroll = 0;
                buildRows();
                return true;
            }
        }
        if (inViewport(mx, my)) {
            for (OptionRow r : rows) {
                if (r.mouseClicked(mx, my, click.button())) {
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        for (OptionRow r : rows) {
            if (r.mouseDragged(click.x(), click.y(), click.button())) {
                return true;
            }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (OptionRow r : rows) {
            r.mouseReleased();
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (inViewport(mx, my)) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (float) v * 16));
            return true;
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public void close() {
        ConfigManager.save();
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
