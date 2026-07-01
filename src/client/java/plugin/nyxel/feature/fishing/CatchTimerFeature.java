package plugin.nyxel.feature.fishing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.TickListener;
import plugin.nyxel.hud.HudElement;
import plugin.nyxel.hud.HudText;

/**
 * Tracks fishing session stats — total catches, catches/hour, and time since the
 * last catch — and exposes them as a movable HUD element. Catches are detected
 * via {@link BiteDetector} (a bite is the proxy for a catch).
 */
public final class CatchTimerFeature implements Feature, TickListener, HudElement {

    private final BiteDetector detector = new BiteDetector();

    private boolean active = false;
    private long sessionStart = 0L;
    private long lastCatch = 0L;
    private int catches = 0;

    @Override
    public String id() {
        return "fishing-catch-timer";
    }

    @Override
    public String displayName() {
        return "Catch Timer & Stats";
    }

    @Override
    public String description() {
        return "HUD with catches, per-hour and last-catch timer";
    }

    @Override
    public Category category() {
        return Category.FISHING;
    }

    @Override
    public void onEnable() {
        active = true;
        sessionStart = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        active = false;
    }

    @Override
    public void onClientTick(MinecraftClient mc) {
        if (detector.tick(mc)) {
            catches++;
            lastCatch = System.currentTimeMillis();
        }
    }

    // --- HudElement ---

    @Override
    public boolean isVisible() {
        return active && BiteDetector.hasBobber(MinecraftClient.getInstance());
    }

    @Override
    public int width() {
        return 130;
    }

    @Override
    public int height() {
        return 42;
    }

    @Override
    public void render(DrawContext ctx) {
        HudText.draw(ctx, "§b§lFishing", 0, 0);
        HudText.draw(ctx, "§7Catches: §f" + catches, 0, 12);
        HudText.draw(ctx, "§7Per hour: §f" + perHour(), 0, 22);
        HudText.draw(ctx, "§7Since last: §f" + sinceLast(), 0, 32);
    }

    private int perHour() {
        long elapsedMs = System.currentTimeMillis() - sessionStart;
        if (elapsedMs <= 0 || catches == 0) {
            return 0;
        }
        double hours = elapsedMs / 3_600_000.0;
        return (int) Math.round(catches / hours);
    }

    private String sinceLast() {
        if (lastCatch == 0L) {
            return "—";
        }
        long secs = (System.currentTimeMillis() - lastCatch) / 1000L;
        return secs + "s";
    }
}
