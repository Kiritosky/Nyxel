package plugin.nyxel.feature.fishing;

import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.NyxelConfig;
import plugin.nyxel.core.Alerts;
import plugin.nyxel.core.ChatRouter;
import plugin.nyxel.core.Feature;

import java.util.regex.Pattern;

/**
 * Watches chat for sea-creature spawn messages and shows a centered alert (with
 * optional sound) naming the creature. Subscribes to chat lazily so the handler
 * is registered exactly once even across enable/disable cycles.
 */
public final class SeaCreatureAlertsFeature implements Feature {

    // Spawn messages are plain lines; match any non-empty line and test the table.
    private static final Pattern ANY_LINE = Pattern.compile("^(.+)$");

    private boolean subscribed = false;
    private volatile boolean active = false;

    private String lastCreature = "";

    @Override
    public String id() {
        return "fishing-sea-creature-alerts";
    }

    @Override
    public String displayName() {
        return "Sea Creature Alerts";
    }

    @Override
    public String description() {
        return "Alert + sound when a sea creature spawns";
    }

    @Override
    public Category category() {
        return Category.FISHING;
    }

    @Override
    public void onEnable() {
        active = true;
        if (!subscribed) {
            ChatRouter.get().subscribe(ANY_LINE, m -> {
                if (!active) {
                    return;
                }
                String creature = SeaCreatures.match(m.group(1));
                if (creature != null) {
                    onSpawn(creature);
                }
            });
            subscribed = true;
        }
    }

    @Override
    public void onDisable() {
        active = false;
    }

    public String lastCreature() {
        return lastCreature;
    }

    private void onSpawn(String creature) {
        lastCreature = creature;
        NyxelConfig.Fishing cfg = ConfigManager.get().fishing;
        if (cfg.seaCreatureTitle) {
            Alerts.show("§b§lSEA CREATURE!", "§e" + creature, 2500, cfg.seaCreatureSound);
        } else if (cfg.seaCreatureSound) {
            Alerts.show("", "", 1, true);
        }
    }
}
