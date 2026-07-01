package plugin.nyxel.feature.fishing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.core.Alerts;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.SkyblockState;
import plugin.nyxel.core.TickListener;

import java.util.Locale;
import java.util.Set;

/**
 * Warns (once per zone change) when the equipped fishing rod doesn't match the
 * current zone's fishing medium — e.g. holding a water rod on the Crimson Isle
 * where you fish in lava. Uses {@link SkyblockState} for the lava/water context.
 */
public final class GearChecker implements Feature, TickListener {

    private static final Set<String> LAVA_ROD_KEYWORDS =
            Set.of("magma", "inferno", "hellfire", "lava");

    private final SkyblockState state;

    private boolean active = false;
    private String lastCheckedZone = "";

    public GearChecker(SkyblockState state) {
        this.state = state;
    }

    @Override
    public String id() {
        return "fishing-gear-checker";
    }

    @Override
    public String displayName() {
        return "Gear Checker";
    }

    @Override
    public String description() {
        return "Warns if your rod doesn't match the zone";
    }

    @Override
    public Category category() {
        return Category.FISHING;
    }

    @Override
    public void onEnable() {
        active = true;
        lastCheckedZone = "";
    }

    @Override
    public void onDisable() {
        active = false;
    }

    @Override
    public void onClientTick(MinecraftClient mc) {
        if (!active || !ConfigManager.get().fishing.gearWarnings) {
            return;
        }
        String zone = state.zone();
        if (zone.isEmpty() || zone.equals(lastCheckedZone)) {
            return;
        }
        lastCheckedZone = zone;
        checkRod(mc);
    }

    private void checkRod(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack held = player.getMainHandStack();
        if (held == null || held.isEmpty()) {
            return;
        }
        String name = held.getName().getString().toLowerCase(Locale.ROOT);
        if (!name.contains("rod")) {
            return; // only check when actually holding a fishing rod
        }
        boolean isLavaRod = LAVA_ROD_KEYWORDS.stream().anyMatch(name::contains);
        if (state.isLavaFishingZone() && !isLavaRod) {
            Alerts.show("§c§lWRONG ROD", "§eThis zone fishes in lava — equip a lava rod!",
                    4000, true);
        }
    }
}
