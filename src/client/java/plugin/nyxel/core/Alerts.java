package plugin.nyxel.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

/**
 * Lightweight transient on-screen alert (a centered title + subtitle that fades
 * after a short time) plus an optional sound. Shared by any feature that needs
 * to grab the player's attention, e.g. sea-creature spawns. Rendered from
 * {@link EventHooks} once per frame.
 */
public final class Alerts {

    private static String title = "";
    private static String subtitle = "";
    private static long expiresAt = 0L;

    private Alerts() {
    }

    /** Show an alert for {@code durationMs} and optionally play a ping sound. */
    public static void show(String title, String subtitle, long durationMs, boolean sound) {
        Alerts.title = title == null ? "" : title;
        Alerts.subtitle = subtitle == null ? "" : subtitle;
        Alerts.expiresAt = System.currentTimeMillis() + durationMs;
        if (sound) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) {
                mc.getSoundManager().play(PositionedSoundInstance.ui(
                        SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.4f));
            }
        }
    }

    public static void render(DrawContext ctx) {
        if (System.currentTimeMillis() >= expiresAt) {
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return;
        }
        int cx = ctx.getScaledWindowWidth() / 2;
        int y = ctx.getScaledWindowHeight() / 4;
        if (!title.isEmpty()) {
            ctx.drawCenteredTextWithShadow(mc.textRenderer,
                    net.minecraft.text.Text.literal(title), cx, y, 0xFFFFFFFF);
        }
        if (!subtitle.isEmpty()) {
            ctx.drawCenteredTextWithShadow(mc.textRenderer,
                    net.minecraft.text.Text.literal(subtitle), cx, y + 12, 0xFFFFFFFF);
        }
    }
}
