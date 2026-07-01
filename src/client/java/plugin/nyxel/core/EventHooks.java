package plugin.nyxel.core;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import plugin.nyxel.config.gui.NyxelConfigScreen;
import plugin.nyxel.feature.crafting.CraftingPlannerFeature;
import plugin.nyxel.feature.garden.MutationHelperFeature;
import plugin.nyxel.feature.general.MinionPlannerFeature;
import plugin.nyxel.hud.HudManager;

/**
 * Wires all Fabric client callbacks to Nyxel's core systems in one place so
 * features never touch the event API directly. Registered once at client init.
 */
public final class EventHooks {

    private EventHooks() {
    }

    public static void register(FeatureManager features,
                                SkyblockState state,
                                HudManager hud,
                                KeyBinding openConfigKey) {

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            state.update(mc);
            while (openConfigKey.wasPressed()) {
                mc.setScreen(new NyxelConfigScreen(features, hud, null));
            }
            features.onClientTick(mc);
        });

        // Real chat goes to the router; action-bar (overlay) text is broadcast
        // separately so high-frequency overlay updates don't spam chat handlers.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String plain = message.getString();
            if (overlay) {
                features.onActionBar(plain);
            } else {
                ChatRouter.get().handle(plain);
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            hud.render(drawContext);
            Alerts.render(drawContext);
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
            dispatcher.register(ClientCommandManager.literal("nyxel").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> mc.setScreen(new NyxelConfigScreen(features, hud, null)));
                return 1;
            }));
            dispatcher.register(ClientCommandManager.literal("mutations").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> {
                    if (features.byId(MutationHelperFeature.ID)
                            instanceof MutationHelperFeature mh) {
                        mh.openPlanner(null);
                    }
                });
                return 1;
            }));
            dispatcher.register(ClientCommandManager.literal("minions").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> {
                    if (features.byId(MinionPlannerFeature.ID)
                            instanceof MinionPlannerFeature mp) {
                        mp.openPlanner(null);
                    }
                });
                return 1;
            }));
            dispatcher.register(ClientCommandManager.literal("recipes").executes(ctx -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                mc.execute(() -> {
                    if (features.byId(CraftingPlannerFeature.ID)
                            instanceof CraftingPlannerFeature cp) {
                        cp.openPlanner(null);
                    }
                });
                return 1;
            }));
        });
    }
}
