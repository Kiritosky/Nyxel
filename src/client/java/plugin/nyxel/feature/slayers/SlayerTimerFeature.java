package plugin.nyxel.feature.slayers;

import plugin.nyxel.core.Alerts;
import plugin.nyxel.core.ChatRouter;
import plugin.nyxel.core.Feature;

import java.util.regex.Pattern;

/**
 * Anchor feature for the slayers area: tracks slayer quest timing from chat and
 * alerts on boss spawn and completion. Dungeon map and party finder live in the
 * same area as WIP stubs.
 *
 * <p>NOTE: trigger phrases are game data; verify against the current game.
 */
public final class SlayerTimerFeature implements Feature {

    private static final Pattern STARTED = Pattern.compile("(?i)slayer quest started");
    private static final Pattern SPAWNED = Pattern.compile("(?i)slay the boss");
    private static final Pattern DONE =
            Pattern.compile("(?i)slayer quest complete|nice! you killed");

    private boolean subscribed = false;
    private volatile boolean active = false;
    private long questStart = 0L;

    @Override
    public String id() {
        return "slayers-timer";
    }

    @Override
    public String displayName() {
        return "Slayer Quest Timer";
    }

    @Override
    public String description() {
        return "Times slayer quests and alerts on boss spawn";
    }

    @Override
    public Category category() {
        return Category.DUNGEONS;
    }

    @Override
    public void onEnable() {
        active = true;
        if (!subscribed) {
            ChatRouter.get().subscribe(STARTED, m -> {
                if (active) {
                    questStart = System.currentTimeMillis();
                }
            });
            ChatRouter.get().subscribe(SPAWNED, m -> {
                if (active) {
                    Alerts.show("§c§lSLAYER BOSS", "§eSlay it!", 2500, true);
                }
            });
            ChatRouter.get().subscribe(DONE, m -> {
                if (active && questStart > 0) {
                    long secs = (System.currentTimeMillis() - questStart) / 1000L;
                    Alerts.show("§a§lQUEST DONE", "§7" + secs + "s", 3000, true);
                    questStart = 0L;
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
