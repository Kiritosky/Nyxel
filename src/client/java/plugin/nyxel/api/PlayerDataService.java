package plugin.nyxel.api;

import net.minecraft.client.MinecraftClient;
import plugin.nyxel.Nyxel;
import plugin.nyxel.api.model.PlayerInfo;
import plugin.nyxel.config.ConfigManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Caches the player's resolved {@link PlayerInfo} (Ironman flag + garden) from
 * the Hypixel API. Refreshes lazily on a background thread when stale; never
 * blocks the render/tick thread. With no API key configured it stays
 * {@link PlayerInfo#unavailable()} and the rest of the mod works manually.
 */
public final class PlayerDataService {

    private volatile PlayerInfo cached = PlayerInfo.unavailable();
    private volatile long lastFetch = 0L;
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    /** Latest snapshot; triggers a background refresh if stale. */
    public PlayerInfo get() {
        maybeRefresh();
        return cached;
    }

    public boolean isIronman() {
        return get().ironman;
    }

    private void maybeRefresh() {
        String key = ConfigManager.get().api.hypixelKey;
        if (key == null || key.isBlank()) {
            return;
        }
        long ttl = ConfigManager.get().api.cacheSeconds * 1000L;
        if (System.currentTimeMillis() - lastFetch < ttl) {
            return;
        }
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        Thread t = new Thread(() -> fetch(key), "Nyxel-Hypixel");
        t.setDaemon(true);
        t.start();
    }

    private void fetch(String key) {
        try {
            String uuid = ownUuid();
            if (uuid != null) {
                cached = HypixelApi.fetchPlayer(key, uuid);
                lastFetch = System.currentTimeMillis();
            }
        } catch (Exception e) {
            Nyxel.LOGGER.warn("Hypixel API fetch failed: {}", e.getMessage());
        } finally {
            refreshing.set(false);
        }
    }

    /** Undashed UUID of the local player; null when not in a world yet. */
    private static String ownUuid() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return null;
        }
        return mc.player.getUuid().toString().replace("-", "");
    }
}
