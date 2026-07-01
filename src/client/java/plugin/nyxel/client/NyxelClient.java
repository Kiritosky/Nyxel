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
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureManager;
import plugin.nyxel.core.FeatureModule;
import plugin.nyxel.core.SkyblockState;
import plugin.nyxel.feature.crafting.CraftingModule;
import plugin.nyxel.feature.economy.EconomyModule;
import plugin.nyxel.feature.fishing.FishingModule;
import plugin.nyxel.feature.garden.GardenModule;
import plugin.nyxel.feature.general.GeneralModule;
import plugin.nyxel.feature.hud.HudModule;
import plugin.nyxel.feature.mining.MiningModule;
import plugin.nyxel.feature.slayers.SlayersModule;
import plugin.nyxel.hud.HudElement;
import plugin.nyxel.hud.HudManager;

import java.util.List;

/**
 * Client entrypoint. Builds the core systems, then registers features by iterating
 * a fixed list of {@link FeatureModule}s (each owns one category's features), wiring
 * any that are HUD elements into the {@link HudManager}. Adding a feature means
 * editing its module, not this class.
 */
public class NyxelClient implements ClientModInitializer {

    /** The modules whose features make up the mod. Add new modules here. */
    private static final List<FeatureModule> MODULES = List.of(
            new GardenModule(),
            new FishingModule(),
            new HudModule(),
            new EconomyModule(),
            new CraftingModule(),
            new MiningModule(),
            new SlayersModule(),
            new GeneralModule()
    );

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        SkyblockState state = new SkyblockState();
        HudManager hud = new HudManager();
        FeatureManager features = new FeatureManager();
        PlayerDataService playerData = new PlayerDataService();

        FeatureContext ctx = new FeatureContext(state, playerData);
        for (FeatureModule module : MODULES) {
            for (Feature feature : module.create(ctx)) {
                register(features, hud, feature);
            }
        }

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
