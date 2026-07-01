package plugin.nyxel.feature.fishing;

import plugin.nyxel.core.Alerts;
import plugin.nyxel.core.ChatRouter;
import plugin.nyxel.core.Feature;

import java.util.regex.Pattern;

/**
 * Surfaces fishing hotspot events. When a hotspot-related message appears in
 * chat it shows an alert so the player can take advantage of the buff. Bait
 * reminders are driven from the same chat signal.
 *
 * <p>NOTE: the trigger phrase is game data; adjust the pattern if Hypixel changes
 * its hotspot messaging.
 */
public final class HotspotHelper implements Feature {

    private static final Pattern HOTSPOT = Pattern.compile("(?i)hotspot");

    private boolean subscribed = false;
    private volatile boolean active = false;

    @Override
    public String id() {
        return "fishing-hotspot-helper";
    }

    @Override
    public String displayName() {
        return "Hotspot Helper";
    }

    @Override
    public String description() {
        return "Alerts on fishing hotspots + bait reminder";
    }

    @Override
    public Category category() {
        return Category.FISHING;
    }

    @Override
    public void onEnable() {
        active = true;
        if (!subscribed) {
            ChatRouter.get().subscribe(HOTSPOT, m -> {
                if (active) {
                    Alerts.show("§d§lHOTSPOT", "§eUse bait to maximise it!", 3000, true);
                }
            });
            subscribed = true;
        }
    }

    @Override
    public void onDisable() {
        active = false;
    }
}
