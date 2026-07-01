package plugin.nyxel.core;

import net.minecraft.client.MinecraftClient;

/**
 * Capability interface for {@link Feature}s that need per-client-tick work. The
 * {@link FeatureManager} buckets features by the capability interfaces they
 * implement, so tick dispatch only touches features that opt in.
 */
public interface TickListener {

    /** Per client tick, only while the owning feature is enabled. */
    void onClientTick(MinecraftClient mc);
}
