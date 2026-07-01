package plugin.nyxel.feature.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.SkyblockState;
import plugin.nyxel.hud.HudElement;
import plugin.nyxel.hud.HudText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses skill-XP gains from the action bar (e.g. "+12.3 Farming (45%)") and
 * shows a HUD element with the current skill and an XP/hour estimate. Anchor
 * feature proving the HUD + action-bar pipeline.
 */
public final class SkillTrackerFeature implements Feature, HudElement {

    private static final Pattern GAIN =
            Pattern.compile("\\+([\\d,]+(?:\\.\\d+)?) (\\w+) \\(");

    private final SkyblockState state;

    private boolean active = false;
    private String skill = "";
    private double accumulatedXp = 0;
    private long startTime = 0L;

    public SkillTrackerFeature(SkyblockState state) {
        this.state = state;
    }

    @Override
    public String id() {
        return "hud-skill-tracker";
    }

    @Override
    public String displayName() {
        return "Skill XP/hr Tracker";
    }

    @Override
    public String description() {
        return "Shows current skill and XP per hour";
    }

    @Override
    public Category category() {
        return Category.HUD;
    }

    @Override
    public void onEnable() {
        active = true;
        reset();
    }

    @Override
    public void onDisable() {
        active = false;
    }

    @Override
    public void onActionBar(String text) {
        Matcher m = GAIN.matcher(text);
        if (!m.find()) {
            return;
        }
        String s = m.group(2);
        double amount;
        try {
            amount = Double.parseDouble(m.group(1).replace(",", ""));
        } catch (NumberFormatException e) {
            return;
        }
        if (!s.equals(skill)) {
            skill = s;
            reset();
        }
        accumulatedXp += amount;
    }

    private void reset() {
        accumulatedXp = 0;
        startTime = System.currentTimeMillis();
    }

    // --- HudElement ---

    @Override
    public boolean isVisible() {
        return active && state.onSkyblock() && !skill.isEmpty();
    }

    @Override
    public int width() {
        return 130;
    }

    @Override
    public int height() {
        return 22;
    }

    @Override
    public void render(DrawContext ctx) {
        HudText.draw(ctx, "§a" + skill, 0, 0);
        HudText.draw(ctx, "§7XP/h: §f" + perHour(), 0, 10);
    }

    private String perHour() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed <= 0 || accumulatedXp <= 0) {
            return "—";
        }
        double hours = elapsed / 3_600_000.0;
        double rate = accumulatedXp / hours;
        if (rate >= 1_000_000) {
            return String.format("%.1fM", rate / 1_000_000);
        }
        if (rate >= 1_000) {
            return String.format("%.1fk", rate / 1_000);
        }
        return String.format("%.0f", rate);
    }
}
