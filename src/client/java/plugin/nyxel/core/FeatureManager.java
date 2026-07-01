package plugin.nyxel.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import plugin.nyxel.Nyxel;
import plugin.nyxel.config.ConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry that owns every {@link Feature}, persists its enabled state,
 * and fans out the tick / render hooks. There is a single instance held by
 * {@link plugin.nyxel.client.NyxelClient}.
 */
public final class FeatureManager {

    private final List<Feature> features = new ArrayList<>();
    private final Map<String, Boolean> enabled = new LinkedHashMap<>();

    /** Register a feature. Call during init, before {@link #initEnabledState()}. */
    public void register(Feature feature) {
        for (Feature existing : features) {
            if (existing.id().equals(feature.id())) {
                throw new IllegalStateException("Duplicate feature id: " + feature.id());
            }
        }
        features.add(feature);
    }

    /**
     * Load persisted enabled flags from config (falling back to each feature's
     * default) and fire {@link Feature#onEnable()} for those that are on.
     */
    public void initEnabledState() {
        Map<String, Boolean> persisted = ConfigManager.get().featureToggles;
        for (Feature feature : features) {
            boolean on = persisted.getOrDefault(feature.id(), feature.enabledByDefault());
            enabled.put(feature.id(), on);
            if (on) {
                safeEnable(feature);
            }
        }
        persistToggles();
    }

    public boolean isEnabled(String id) {
        return enabled.getOrDefault(id, false);
    }

    /** Toggle a feature on/off, firing the lifecycle hook and persisting the change. */
    public void setEnabled(String id, boolean on) {
        Feature feature = byId(id);
        if (feature == null || isEnabled(id) == on) {
            return;
        }
        enabled.put(id, on);
        if (on) {
            safeEnable(feature);
        } else {
            safeDisable(feature);
        }
        persistToggles();
    }

    public List<Feature> all() {
        return Collections.unmodifiableList(features);
    }

    public Feature byId(String id) {
        for (Feature f : features) {
            if (f.id().equals(id)) {
                return f;
            }
        }
        return null;
    }

    public void onClientTick(MinecraftClient mc) {
        for (Feature f : features) {
            if (isEnabled(f.id())) {
                try {
                    f.onClientTick(mc);
                } catch (Exception e) {
                    Nyxel.LOGGER.error("[{}] tick error", f.id(), e);
                }
            }
        }
    }

    public void onActionBar(String text) {
        for (Feature f : features) {
            if (isEnabled(f.id())) {
                try {
                    f.onActionBar(text);
                } catch (Exception e) {
                    Nyxel.LOGGER.error("[{}] actionbar error", f.id(), e);
                }
            }
        }
    }

    public void onHudRender(DrawContext ctx, float tickDelta) {
        for (Feature f : features) {
            if (isEnabled(f.id())) {
                try {
                    f.onHudRender(ctx, tickDelta);
                } catch (Exception e) {
                    Nyxel.LOGGER.error("[{}] hud error", f.id(), e);
                }
            }
        }
    }

    private void safeEnable(Feature f) {
        try {
            f.onEnable();
        } catch (Exception e) {
            Nyxel.LOGGER.error("[{}] enable error", f.id(), e);
        }
    }

    private void safeDisable(Feature f) {
        try {
            f.onDisable();
        } catch (Exception e) {
            Nyxel.LOGGER.error("[{}] disable error", f.id(), e);
        }
    }

    private void persistToggles() {
        ConfigManager.get().featureToggles.putAll(enabled);
        ConfigManager.save();
    }
}
