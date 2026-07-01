package plugin.nyxel.feature.general;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import plugin.nyxel.core.Feature;
import plugin.nyxel.feature.general.data.MinionRepository;
import plugin.nyxel.feature.general.gui.MinionPlannerScreen;

/**
 * Minion output estimator: opens a planner that computes items/hour and
 * time-to-full for any minion tier + fuel. Ironman income is minion-driven, but
 * the numbers help everyone plan setups. Owns the minion dataset; data loads at
 * construction so the planner works regardless of the toggle.
 */
public final class MinionPlannerFeature implements Feature {

    public static final String ID = "general-minion-planner";

    private final MinionRepository repo = new MinionRepository();

    public MinionPlannerFeature() {
        repo.refresh();
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Minion Planner";
    }

    @Override
    public String description() {
        return "Estimate minion items/hour and time-to-full by tier and fuel";
    }

    @Override
    public Category category() {
        return Category.GENERAL;
    }

    public MinionRepository repo() {
        return repo;
    }

    /** Open the planner screen, warning if the dataset failed to load. */
    public void openPlanner(Screen parent) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (repo.isEmpty()) {
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("§c[Nyxel] §7Minion dataset failed to load."), false);
            }
            return;
        }
        mc.setScreen(new MinionPlannerScreen(this, parent));
    }
}
