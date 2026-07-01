package plugin.nyxel.core;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import plugin.nyxel.hud.HudManager;

/**
 * Wires the Fabric client callbacks to Nyxel's core systems in one place so
 * features never touch the event API directly. Registered once at client init.
 * The baseplate wires per-tick state + feature dispatch, chat routing, and HUD
 * rendering; screen/keybind/command wiring returns with the GUI phase.
 */
public final class EventHooks {

    private EventHooks() {
    }

    public static void register(FeatureManager features, SkyblockState state, HudManager hud) {

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            state.update(mc);
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
    }
}
