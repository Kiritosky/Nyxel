package plugin.nyxel.feature.mining;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.SkyblockState;
import plugin.nyxel.hud.HudElement;
import plugin.nyxel.hud.HudText;

import java.util.ArrayList;
import java.util.List;

/**
 * Anchor feature for the mining area: a HUD that surfaces commission / powder
 * lines from the sidebar in the mining islands. Pest and visitor helpers live in
 * the mining/farming areas as WIP stubs.
 */
public final class CommissionHudFeature implements Feature, HudElement {

    private final SkyblockState state;
    private boolean active = false;

    public CommissionHudFeature(SkyblockState state) {
        this.state = state;
    }

    @Override
    public String id() {
        return "mining-commission-hud";
    }

    @Override
    public String displayName() {
        return "Commission / Powder HUD";
    }

    @Override
    public String description() {
        return "Shows commissions and powder in mining zones";
    }

    @Override
    public Category category() {
        return Category.MINING;
    }

    @Override
    public void onEnable() {
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
    }

    private List<String> miningLines() {
        List<String> out = new ArrayList<>();
        for (String line : state.sidebarLines()) {
            String l = line.toLowerCase();
            if (l.contains("commission") || l.contains("powder")
                    || l.contains("mithril") || l.contains("gemstone")) {
                out.add(line);
            }
        }
        return out;
    }

    // --- HudElement ---

    @Override
    public boolean isVisible() {
        return active && state.onSkyblock() && !miningLines().isEmpty();
    }

    @Override
    public int width() {
        return 150;
    }

    @Override
    public int height() {
        return 12 + miningLines().size() * 10;
    }

    @Override
    public void render(DrawContext ctx) {
        HudText.draw(ctx, "§b§lMining", 0, 0);
        int y = 12;
        for (String line : miningLines()) {
            HudText.draw(ctx, "§f" + line, 0, y);
            y += 10;
        }
    }
}
