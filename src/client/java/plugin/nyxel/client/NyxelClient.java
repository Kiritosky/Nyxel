package plugin.nyxel.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import plugin.nyxel.Nyxel;
import plugin.nyxel.api.PlayerDataService;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.core.EventHooks;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureManager;
import plugin.nyxel.core.SkyblockState;
import plugin.nyxel.core.StubFeature;
import plugin.nyxel.feature.economy.PriceTooltipFeature;
import plugin.nyxel.feature.garden.MutationHelperFeature;
import plugin.nyxel.feature.fishing.CatchTimerFeature;
import plugin.nyxel.feature.fishing.GearChecker;
import plugin.nyxel.feature.fishing.HotspotHelper;
import plugin.nyxel.feature.fishing.SeaCreatureAlertsFeature;
import plugin.nyxel.feature.fishing.TrophyFishTracker;
import plugin.nyxel.feature.hud.SkillTrackerFeature;
import plugin.nyxel.feature.mining.CommissionHudFeature;
import plugin.nyxel.feature.slayers.SlayerTimerFeature;
import plugin.nyxel.hud.HudElement;
import plugin.nyxel.hud.HudManager;

/**
 * Client entrypoint. Builds the core systems, registers every feature (wiring
 * any that are HUD elements into the {@link HudManager}), restores enabled state
 * from config, and hooks the Fabric events.
 */
public class NyxelClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        SkyblockState state = new SkyblockState();
        HudManager hud = new HudManager();
        FeatureManager features = new FeatureManager();
        PlayerDataService playerData = new PlayerDataService();

        // Garden (flagship Ironman feature).
        register(features, hud, new MutationHelperFeature(playerData));

        // Fishing (primary area).
        register(features, hud, new SeaCreatureAlertsFeature());
        register(features, hud, new CatchTimerFeature());
        register(features, hud, new TrophyFishTracker());
        register(features, hud, new HotspotHelper());
        register(features, hud, new GearChecker(state));

        // Other areas: working anchors.
        register(features, hud, new SkillTrackerFeature(state));
        register(features, hud, new PriceTooltipFeature());
        register(features, hud, new SlayerTimerFeature());
        register(features, hud, new CommissionHudFeature(state));

        // Other areas: WIP stubs (structure complete, behaviour pending).
        register(features, hud, new StubFeature(
                "dungeons-map", "Dungeon Map", Feature.Category.DUNGEONS));
        register(features, hud, new StubFeature(
                "dungeons-party-finder", "Party Finder", Feature.Category.DUNGEONS));
        register(features, hud, new StubFeature(
                "economy-auction-flips", "Auction Flip Helper", Feature.Category.ECONOMY));
        register(features, hud, new StubFeature(
                "mining-pest-helper", "Pest Helper", Feature.Category.MINING));
        register(features, hud, new StubFeature(
                "farming-visitor-helper", "Garden Visitor Helper", Feature.Category.MINING));

        features.initEnabledState();

        KeyBinding openConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nyxel.config", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.MISC));

        EventHooks.register(features, state, hud, openConfig);

        Nyxel.LOGGER.info("[{}] client init: {} features", Nyxel.MOD_NAME,
                features.all().size());
    }

    /** Register a feature and, if it is also a HUD element, wire it into the HUD. */
    private static void register(FeatureManager features, HudManager hud, Feature feature) {
        features.register(feature);
        if (feature instanceof HudElement element) {
            hud.register(element);
        }
    }
}
