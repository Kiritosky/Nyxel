package plugin.nyxel.feature.fishing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.core.ChatRouter;
import plugin.nyxel.core.Feature;
import plugin.nyxel.hud.HudElement;
import plugin.nyxel.hud.HudText;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tracks trophy-fish catches per fish and per tier (Bronze→Diamond) by parsing
 * catch chat messages. Counts persist in the config and a HUD element shows
 * overall completion (distinct fish/tier combos caught of the maximum).
 */
public final class TrophyFishTracker implements Feature, HudElement {

    /** The 18 trophy fish, lower-cased for matching. */
    private static final List<String> TROPHY_FISH = List.of(
            "blobfish", "obfuscated 1", "obfuscated 2", "obfuscated 3",
            "sulphur skitter", "flyfish", "lavahorse", "mana ray",
            "slugfish", "soul fish", "karate fish", "skeleton fish",
            "moldfin", "vanille", "gusher", "steaming-hot flounder",
            "golden fish", "volcanic stonefish"
    );

    private static final List<String> TIERS = List.of("bronze", "silver", "gold", "diamond");
    private static final int MAX_COMBOS = 18 * 4;

    // e.g. "You caught a Gold Sulphur Skitter!" — tier then fish name.
    private static final Pattern CATCH = Pattern.compile(
            "(?i)caught a[n]? (Bronze|Silver|Gold|Diamond) ([A-Za-z' \\-]+?)[!.]");

    private boolean subscribed = false;
    private volatile boolean active = false;

    @Override
    public String id() {
        return "fishing-trophy-tracker";
    }

    @Override
    public String displayName() {
        return "Trophy Fish Tracker";
    }

    @Override
    public String description() {
        return "Tracks trophy fish caught per tier";
    }

    @Override
    public Category category() {
        return Category.FISHING;
    }

    @Override
    public void onEnable() {
        active = true;
        if (!subscribed) {
            ChatRouter.get().subscribe(CATCH, m -> {
                if (!active) {
                    return;
                }
                String tier = m.group(1).toLowerCase(Locale.ROOT);
                String fish = m.group(2).trim().toLowerCase(Locale.ROOT);
                if (TROPHY_FISH.contains(fish)) {
                    record(fish, tier);
                }
            });
            subscribed = true;
        }
    }

    @Override
    public void onDisable() {
        active = false;
    }

    private void record(String fish, String tier) {
        String key = fish + ":" + tier;
        var counts = ConfigManager.get().fishing.trophyCounts;
        counts.merge(key, 1, Integer::sum);
        ConfigManager.save();
    }

    private int distinctCaught() {
        var counts = ConfigManager.get().fishing.trophyCounts;
        int n = 0;
        for (String fish : TROPHY_FISH) {
            for (String tier : TIERS) {
                if (counts.getOrDefault(fish + ":" + tier, 0) > 0) {
                    n++;
                }
            }
        }
        return n;
    }

    // --- HudElement ---

    @Override
    public boolean isVisible() {
        return active && BiteDetector.hasBobber(MinecraftClient.getInstance());
    }

    @Override
    public int width() {
        return 120;
    }

    @Override
    public int height() {
        return 12;
    }

    @Override
    public void render(DrawContext ctx) {
        HudText.draw(ctx, "§6Trophy: §f" + distinctCaught() + "§7/" + MAX_COMBOS, 0, 0);
    }
}
