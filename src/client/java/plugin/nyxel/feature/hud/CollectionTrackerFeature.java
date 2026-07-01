package plugin.nyxel.feature.hud;

import net.minecraft.client.gui.DrawContext;
import plugin.nyxel.core.ChatRouter;
import plugin.nyxel.core.Feature;
import plugin.nyxel.feature.hud.data.CollectionEntry;
import plugin.nyxel.feature.hud.data.CollectionRepository;
import plugin.nyxel.hud.HudElement;
import plugin.nyxel.hud.HudText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks the most recently levelled SkyBlock collection (from the "COLLECTION
 * LEVEL UP" chat message) and shows the next tier's unlock as a HUD element.
 * Collections are the Ironman economy — the next recipe unlock is the number that
 * matters — but the readout is useful to any player. Tier/unlock data comes from
 * {@link CollectionRepository} (Hypixel resources API, bundled fallback).
 */
public final class CollectionTrackerFeature implements Feature, HudElement {

    // Plain (formatting-stripped) form of "§6§lCOLLECTION LEVEL UP §r§7Name §eIII".
    private static final Pattern LEVEL_UP =
            Pattern.compile("COLLECTION LEVEL UP\\s+(.+?)\\s+([IVXLCDM]+)\\s*$");

    private final CollectionRepository collections = new CollectionRepository();

    private boolean active = false;
    private boolean registered = false;

    private String collectionName = "";
    private int currentTier = 0;
    private String nextUnlock = "";

    public CollectionTrackerFeature() {
        collections.refresh();
    }

    @Override
    public String id() {
        return "hud-collection-tracker";
    }

    @Override
    public String displayName() {
        return "Collection Tracker";
    }

    @Override
    public String description() {
        return "Shows the next unlock for your most recent collection";
    }

    @Override
    public Category category() {
        return Category.HUD;
    }

    @Override
    public void onEnable() {
        active = true;
        if (!registered) {
            ChatRouter.get().subscribe(LEVEL_UP, this::onLevelUp);
            registered = true;
        }
    }

    @Override
    public void onDisable() {
        active = false;
    }

    private void onLevelUp(Matcher m) {
        String name = m.group(1).trim();
        int tier = fromRoman(m.group(2));
        if (tier <= 0) {
            return;
        }
        collectionName = name;
        currentTier = tier;
        nextUnlock = resolveNextUnlock(name, tier);
    }

    /**
     * The unlock at the tier after {@code justReached}. Tier N is 1-based, so the
     * next tier's data sits at list index N. Empty when maxed or unknown.
     */
    private String resolveNextUnlock(String name, int justReached) {
        CollectionEntry entry = collections.byName(name);
        if (entry == null || justReached >= entry.tiers.size()) {
            return "";
        }
        CollectionEntry.Tier next = entry.tiers.get(justReached);
        return next.unlocks == null ? "" : next.unlocks;
    }

    // --- HudElement ---

    @Override
    public boolean isVisible() {
        return active && !collectionName.isEmpty();
    }

    @Override
    public int width() {
        return 150;
    }

    @Override
    public int height() {
        return 22;
    }

    @Override
    public void render(DrawContext ctx) {
        HudText.draw(ctx, "§6" + collectionName + " §7Tier " + currentTier, 0, 0);
        String next = nextUnlock.isEmpty() ? "§aMaxed" : "§7Next: §f" + nextUnlock;
        HudText.draw(ctx, next, 0, 10);
    }

    private static int fromRoman(String s) {
        int total = 0;
        int prev = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            int v = switch (s.charAt(i)) {
                case 'I' -> 1;
                case 'V' -> 5;
                case 'X' -> 10;
                case 'L' -> 50;
                case 'C' -> 100;
                case 'D' -> 500;
                case 'M' -> 1000;
                default -> 0;
            };
            if (v == 0) {
                return -1;
            }
            total += v < prev ? -v : v;
            prev = v;
        }
        return total;
    }
}
