package plugin.nyxel.feature.garden.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import plugin.nyxel.api.model.PlayerInfo;
import plugin.nyxel.config.gui.NyxelTheme;
import plugin.nyxel.config.gui.render.Render2D;
import plugin.nyxel.config.gui.widget.NyxelSlider;
import plugin.nyxel.feature.garden.MutationHelperFeature;
import plugin.nyxel.feature.garden.data.GreenhouseModel;
import plugin.nyxel.feature.garden.data.Mutation;
import plugin.nyxel.feature.garden.engine.FusionPlanner;
import plugin.nyxel.feature.garden.engine.PlacementSolver;

import java.util.ArrayList;
import java.util.List;

/**
 * The Greenhouse Mutation Helper UI: pick a target mutation (or Rose Dragon) and
 * a quantity; the center shows the recommended greenhouse placement grid and the
 * right panel shows the fusion build-order + material checklist. SkyHanni-style,
 * reusing the config GUI toolkit.
 */
public final class MutationPlannerScreen extends Screen {

    private final MutationHelperFeature feature;
    private final Screen parent;
    private final GreenhouseModel greenhouse;

    private final List<Mutation> targets = new ArrayList<>();
    private String selectedId = "";
    private int quantity = 1;
    private boolean qtyDragging;

    private FusionPlanner.PlanResult plan;
    private PlacementSolver.PlacementResult placement;
    private final List<String> rightLines = new ArrayList<>();
    private float rightScroll;

    public MutationPlannerScreen(MutationHelperFeature feature, Screen parent) {
        super(Text.literal("Mutation Planner"));
        this.feature = feature;
        this.parent = parent;
        this.greenhouse = feature.greenhouseForPlayer();
    }

    @Override
    protected void init() {
        targets.clear();
        Mutation rose = feature.repo().byId("rose_dragon");
        if (rose != null) {
            targets.add(rose);
        }
        for (Mutation m : feature.repo().all()) {
            if (!m.id.equals("rose_dragon")) {
                targets.add(m);
            }
        }
        if (selectedId.isEmpty() && !targets.isEmpty()) {
            selectedId = targets.get(0).id;
        }
        recompute();
    }

    private void recompute() {
        if (selectedId.isEmpty()) {
            return;
        }
        plan = feature.planner().plan(selectedId, quantity);
        Mutation target = feature.repo().byId(selectedId);
        List<PlacementSolver.Request> reqs = new ArrayList<>();
        if (target != null && target.special == null && !target.requirements.isEmpty()) {
            reqs.add(new PlacementSolver.Request(target, quantity));
        }
        placement = feature.solver().solve(greenhouse, reqs);
        buildRightLines(target);
        rightScroll = 0;
    }

    private void buildRightLines(Mutation target) {
        rightLines.clear();
        if (target == null) {
            return;
        }
        rightLines.add("§l§d" + target.name + " §rx" + quantity);
        for (String note : plan.notes) {
            rightLines.add("§e" + note);
        }
        for (String warn : plan.warnings) {
            rightLines.add("§c" + warn);
        }
        rightLines.add("");
        rightLines.add("§l§6Build order");
        for (String id : plan.buildOrderList()) {
            Mutation m = feature.repo().byId(id);
            int c = plan.mutationCounts.getOrDefault(id, 0);
            rightLines.add("§7• §f" + (m != null ? m.name : id) + " §7x" + c);
        }
        if (!plan.materialCounts.isEmpty()) {
            rightLines.add("");
            rightLines.add("§l§bBase materials");
            plan.materialCounts.forEach((item, c) ->
                    rightLines.add("§7• §f" + item + " §7x" + c));
        }
        for (String w : placement.warnings) {
            rightLines.add("§c" + w);
        }
    }

    // --- layout ---
    private int listX() {
        return 10;
    }

    private int listW() {
        return 120;
    }

    private int listTop() {
        return 60;
    }

    private int rightW() {
        return 200;
    }

    private int rightX() {
        return width - rightW() - 10;
    }

    private int gridX() {
        return listX() + listW() + 16;
    }

    private int gridW() {
        return rightX() - gridX() - 16;
    }

    private int gridTop() {
        return 60;
    }

    private int gridAreaH() {
        return height - gridTop() - 20;
    }

    private int cellSize() {
        return Math.max(8, Math.min(gridW() / greenhouse.cols, gridAreaH() / greenhouse.rows));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        // header
        ctx.drawText(textRenderer, Text.literal("§l§dNyxel §r§7Mutation Planner"),
                12, 12, NyxelTheme.TEXT, false);
        PlayerInfo info = feature.player().get();
        String status = info.available
                ? (info.ironman ? "§aIronman §7· " + info.cuteName : "§e" + info.cuteName)
                : "§8No API key (manual mode)";
        ctx.drawText(textRenderer, Text.literal(status), 12, 26, NyxelTheme.TEXT_MUTED, false);

        // quantity slider
        ctx.drawText(textRenderer, Text.literal("§7Qty §f" + quantity), listX(), 40,
                NyxelTheme.TEXT, false);
        NyxelSlider.render(ctx, listX() + 48, 42, listW() - 48,
                (quantity - 1) / 9f, NyxelTheme.ACCENT);

        renderTargetList(ctx, mouseX, mouseY);
        renderGrid(ctx, mouseX, mouseY);
        renderRightPanel(ctx);

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8Esc to close"), width / 2, height - 12, 0xFFFFFFFF);
    }

    private void renderTargetList(DrawContext ctx, int mouseX, int mouseY) {
        Render2D.roundedRect(ctx, listX() - 4, listTop() - 6, listW() + 8,
                targets.size() * 14 + 12, NyxelTheme.RADIUS, NyxelTheme.PANEL_BG);
        int y = listTop();
        for (Mutation m : targets) {
            boolean sel = m.id.equals(selectedId);
            boolean hover = mouseX >= listX() && mouseX <= listX() + listW()
                    && mouseY >= y - 1 && mouseY <= y + 11;
            if (sel) {
                Render2D.roundedRect(ctx, listX() - 2, y - 1, listW() + 4, 12, 2, 0x44B14BFF);
            } else if (hover) {
                Render2D.roundedRect(ctx, listX() - 2, y - 1, listW() + 4, 12, 2,
                        NyxelTheme.ROW_HOVER);
            }
            int color = sel ? NyxelTheme.TEXT : NyxelTheme.TEXT_MUTED;
            ctx.drawText(textRenderer, Text.literal(m.name), listX(), y, color, false);
            y += 14;
        }
    }

    private void renderGrid(DrawContext ctx, int mouseX, int mouseY) {
        int cs = cellSize();
        int gx = gridX();
        int gy = gridTop();
        String hoverLabel = null;
        for (int r = 0; r < greenhouse.rows; r++) {
            for (int c = 0; c < greenhouse.cols; c++) {
                int x = gx + c * cs;
                int y = gy + r * cs;
                String label = placement == null ? null : placement.at(c, r);
                int col;
                if (label == null) {
                    col = 0x33202030;
                } else if (label.startsWith("★")) {
                    col = NyxelTheme.ACCENT;
                } else {
                    col = 0xFF3FA34D;
                }
                Render2D.roundedRect(ctx, x, y, cs - 2, cs - 2, 2, col);
                if (label != null && mouseX >= x && mouseX <= x + cs
                        && mouseY >= y && mouseY <= y + cs) {
                    hoverLabel = label;
                }
            }
        }
        if (hoverLabel != null) {
            ctx.drawText(textRenderer, Text.literal("§f" + hoverLabel),
                    gx, gy + greenhouse.rows * cs + 4, NyxelTheme.TEXT, false);
        } else {
            ctx.drawText(textRenderer,
                    Text.literal("§8★ = mutation forms here · green = crop to place"),
                    gx, gy + greenhouse.rows * cs + 4, NyxelTheme.TEXT_MUTED, false);
        }
    }

    private void renderRightPanel(DrawContext ctx) {
        int x = rightX();
        int y = listTop() - 6;
        int w = rightW();
        int h = height - y - 20;
        Render2D.roundedRect(ctx, x, y, w, h, NyxelTheme.RADIUS, NyxelTheme.PANEL_BG);
        Render2D.enableScissor(ctx, x + 6, y + 6, w - 12, h - 12);
        int ly = y + 8 - Math.round(rightScroll);
        for (String line : rightLines) {
            ctx.drawText(textRenderer, Text.literal(line), x + 8, ly, NyxelTheme.TEXT, false);
            ly += 11;
        }
        Render2D.disableScissor(ctx);
    }

    // --- input ---
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        // target list
        int y = listTop();
        for (Mutation m : targets) {
            if (mx >= listX() && mx <= listX() + listW() && my >= y - 1 && my <= y + 11) {
                selectedId = m.id;
                recompute();
                return true;
            }
            y += 14;
        }
        // quantity slider
        if (within(mx, my, listX() + 48, 39, listW() - 48, 12)) {
            qtyDragging = true;
            applyQty(mx);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (qtyDragging) {
            applyQty(click.x());
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        qtyDragging = false;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (mx >= rightX()) {
            float max = Math.max(0, rightLines.size() * 11 - (height - listTop() - 40));
            rightScroll = Math.max(0, Math.min(max, rightScroll - (float) v * 14));
            return true;
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    private void applyQty(double mx) {
        float frac = (float) (mx - (listX() + 48)) / (listW() - 48);
        int q = 1 + Math.round(Math.max(0f, Math.min(1f, frac)) * 9);
        if (q != quantity) {
            quantity = q;
            recompute();
        }
    }

    private static boolean within(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
