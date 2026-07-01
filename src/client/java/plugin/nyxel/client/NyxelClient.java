package plugin.nyxel.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import plugin.nyxel.Nyxel;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.core.EventHooks;
import plugin.nyxel.core.FeatureManager;
import plugin.nyxel.core.SkyblockState;
import plugin.nyxel.data.neu.NeuRepoService;
import plugin.nyxel.hud.HudManager;

/**
 * Client entrypoint — the baseplate. Builds the core systems, kicks off the NEU
 * data load, and wires the Fabric events. No features are registered yet; they are
 * added on top of this foundation. Live Hypixel data flows through
 * {@link plugin.nyxel.api.ApiClient} (proxy-first).
 */
public class NyxelClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();

        SkyblockState state = new SkyblockState();
        HudManager hud = new HudManager();
        FeatureManager features = new FeatureManager();

        // Data source #1: static SkyBlock item data from the NEU-REPO (cached).
        NeuRepoService.get().load();

        // Features are registered here as the mod grows (none in the baseplate).
        features.initEnabledState();

        KeyBinding openConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nyxel.config", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.MISC));

        EventHooks.register(features, state, hud, openConfig);

        Nyxel.LOGGER.info("[{}] client init (baseplate): {} features",
                Nyxel.MOD_NAME, features.all().size());
    }
}
