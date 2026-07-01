package plugin.nyxel.api;

import net.minecraft.client.MinecraftClient;
import plugin.nyxel.Nyxel;
import plugin.nyxel.api.model.PlayerInfo;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.config.NyxelConfig;
import plugin.nyxel.core.NyxelExecutor;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Caches the player's resolved {@link PlayerInfo} (Ironman flag + garden) from
 * the Hypixel API. Refreshes lazily on the shared background executor when stale;
 * never blocks the render/tick thread. With neither a proxy URL nor an API key
 * configured it stays {@link PlayerInfo#unavailable()} and the rest of the mod
 * works manually.
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
        NyxelConfig.Api api = ConfigManager.get().api;
        boolean hasProxy = api.proxyUrl != null && !api.proxyUrl.isBlank();
        boolean hasKey = api.hypixelKey != null && !api.hypixelKey.isBlank();
        if (!hasProxy && !hasKey) {
            return; // no data source configured; mod runs manually
        }
        long ttl = api.cacheSeconds * 1000L;
        if (System.currentTimeMillis() - lastFetch < ttl) {
            return;
        }
        if (!refreshing.compareAndSet(false, true)) {
            return;
        }
        NyxelExecutor.run("hypixel-profile", this::fetch);
    }

    private void fetch() {
        try {
            String uuid = ownUuid();
            if (uuid != null) {
                cached = HypixelApi.fetchPlayer(uuid);
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
