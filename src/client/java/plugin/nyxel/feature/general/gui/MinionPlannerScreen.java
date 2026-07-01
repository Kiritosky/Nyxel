package plugin.nyxel.feature.general.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import plugin.nyxel.feature.general.MinionPlannerFeature;
import plugin.nyxel.feature.general.data.Fuel;
import plugin.nyxel.feature.general.data.Minion;
import plugin.nyxel.feature.general.engine.MinionCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Minion output planner: pick a minion, tier and fuel with buttons and read off
 * the estimated items/hour, storage capacity and time-to-full. Kept deliberately
 * simple (button-driven) — the math lives in {@link MinionCalculator}.
 */
public final class MinionPlannerScreen extends Screen {

    private final MinionPlannerFeature feature;
    private final Screen parent;
    private final List<Minion> minions;
    private final List<Fuel> fuels;

    private int minionIndex = 0;
    private int tier = 1;
    private int fuelIndex = 0;

    private ButtonWidget minionButton;
    private ButtonWidget tierButton;
    private ButtonWidget fuelButton;

    public MinionPlannerScreen(MinionPlannerFeature feature, Screen parent) {
        super(Text.literal("Minion Planner"));
        this.feature = feature;
        this.parent = parent;
        this.minions = new ArrayList<>(feature.repo().all());
        this.fuels = feature.repo().fuels();
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 40;
        int w = 200;
        int h = 20;

        minionButton = ButtonWidget.builder(Text.empty(), b -> cycleMinion(1))
                .dimensions(cx - w / 2, y, w, h).build();
        addDrawableChild(minionButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("Tier -"), b -> changeTier(-1))
                .dimensions(cx - w / 2, y + 24, 60, h).build());
        tierButton = ButtonWidget.builder(Text.empty(), b -> {})
                .dimensions(cx - 40, y + 24, 80, h).build();
        tierButton.active = false;
        addDrawableChild(tierButton);
        addDrawableChild(ButtonWidget.builder(Text.literal("Tier +"), b -> changeTier(1))
                .dimensions(cx + w / 2 - 60, y + 24, 60, h).build());

        fuelButton = ButtonWidget.builder(Text.empty(), b -> cycleFuel(1))
                .dimensions(cx - w / 2, y + 48, w, h).build();
        addDrawableChild(fuelButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - 50, y + 130, 100, h).build());

        updateLabels();
    }

    private void cycleMinion(int dir) {
        if (minions.isEmpty()) {
            return;
        }
        minionIndex = Math.floorMod(minionIndex + dir, minions.size());
        tier = Math.min(tier, current().maxTier());
        updateLabels();
    }

    private void changeTier(int dir) {
        int max = Math.max(1, current().maxTier());
        tier = Math.max(1, Math.min(max, tier + dir));
        updateLabels();
    }

    private void cycleFuel(int dir) {
        if (fuels.isEmpty()) {
            return;
        }
        fuelIndex = Math.floorMod(fuelIndex + dir, fuels.size());
        updateLabels();
    }

    private Minion current() {
        return minions.get(minionIndex);
    }

    private Fuel currentFuel() {
        return fuels.isEmpty() ? null : fuels.get(fuelIndex);
    }

    private void updateLabels() {
        if (minions.isEmpty()) {
            minionButton.setMessage(Text.literal("§cNo minion data"));
            return;
        }
        minionButton.setMessage(Text.literal("§e" + current().name + " §7(click to change)"));
        tierButton.setMessage(Text.literal("§fTier " + tier));
        Fuel f = currentFuel();
        fuelButton.setMessage(Text.literal("§6Fuel: §f" + (f == null ? "None" : f.name)));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§d§lMinion Planner"),
                width / 2, height / 2 - 70, 0xFFFFFFFF);

        if (minions.isEmpty()) {
            return;
        }
        Fuel f = currentFuel();
        double boost = f == null ? 0.0 : f.speedBoost;
        MinionCalculator.Result r = MinionCalculator.compute(current(), tier, boost);

        int y = height / 2 + 44;
        drawStat(ctx, "Items / hour", String.format("%,.1f", r.itemsPerHour()), y);
        drawStat(ctx, "Storage capacity", String.format("%,d", r.capacityItems()), y + 12);
        drawStat(ctx, "Time to full", formatHours(r.hoursToFull()), y + 24);
    }

    private void drawStat(DrawContext ctx, String label, String value, int y) {
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7" + label + ": §f" + value), width / 2, y, 0xFFFFFFFF);
    }

    private static String formatHours(double hours) {
        if (hours <= 0) {
            return "—";
        }
        long totalMinutes = Math.round(hours * 60);
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
