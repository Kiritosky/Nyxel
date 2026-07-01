package plugin.nyxel.feature.fishing;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;

/**
 * Detects a fishing "bite" from the local player's bobber motion: when a fish
 * bites, the bobber dips with a sharp downward velocity. Returns {@code true}
 * exactly once per bite. Each consumer (catch counter, AutoFish) owns its own
 * instance so their state stays independent.
 */
public final class BiteDetector {

    private static final double DIP_VELOCITY = -0.12;

    private boolean fired = false;

    /** Call once per client tick. True on the tick a bite is first detected. */
    public boolean tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        FishingBobberEntity bobber = player == null ? null : player.fishHook;
        if (bobber == null) {
            fired = false; // line is out; arm for the next cast
            return false;
        }
        double vy = bobber.getVelocity().y;
        if (!fired && vy < DIP_VELOCITY) {
            fired = true;
            return true;
        }
        return false;
    }

    /** Whether a bobber is currently cast. */
    public static boolean hasBobber(MinecraftClient mc) {
        return mc.player != null && mc.player.fishHook != null;
    }
}
