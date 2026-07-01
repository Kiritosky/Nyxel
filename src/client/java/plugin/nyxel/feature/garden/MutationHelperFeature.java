package plugin.nyxel.feature.garden;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import plugin.nyxel.api.PlayerDataService;
import plugin.nyxel.api.model.PlayerInfo;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.NyxelExecutor;
import plugin.nyxel.core.PremiumGate;
import plugin.nyxel.feature.garden.data.GreenhouseModel;
import plugin.nyxel.feature.garden.data.MutationRepository;
import plugin.nyxel.feature.garden.engine.FusionPlanner;
import plugin.nyxel.feature.garden.engine.PlacementSolver;
import plugin.nyxel.feature.garden.gui.MutationPlannerScreen;

/**
 * The Greenhouse Mutation Helper — Nyxel's flagship Ironman feature. Owns the
 * mutation dataset and the fusion/placement engines and opens the planner GUI.
 * Premium-gated via {@link PremiumGate} (currently open). Data loads at
 * construction so the planner works regardless of the toggle.
 */
public final class MutationHelperFeature implements Feature {

    public static final String ID = "garden-mutation-helper";

    private final MutationRepository repo = new MutationRepository();
    private final PlacementSolver solver = new PlacementSolver();
    private final PlayerDataService player;
    private final FusionPlanner planner;

    public MutationHelperFeature(PlayerDataService player) {
        this.player = player;
        repo.loadBundled();
        String remote = ConfigManager.get().garden.datasetUrl;
        if (remote != null && !remote.isBlank()) {
            NyxelExecutor.run("mutations-remote", () -> repo.tryLoadRemote(remote));
        }
        this.planner = new FusionPlanner(repo);
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Mutation Helper";
    }

    @Override
    public String description() {
        return "Greenhouse planner: placement + full Rose Dragon fusion guide";
    }

    @Override
    public Category category() {
        return Category.GARDEN;
    }

    public MutationRepository repo() {
        return repo;
    }

    public FusionPlanner planner() {
        return planner;
    }

    public PlacementSolver solver() {
        return solver;
    }

    public PlayerDataService player() {
        return player;
    }

    /** Greenhouse grid sized from the live garden when available, else default. */
    public GreenhouseModel greenhouseForPlayer() {
        PlayerInfo info = player.get();
        if (ConfigManager.get().garden.autoFillFromApi && info.available
                && info.garden.unlockedPlotCount() > 0) {
            return GreenhouseModel.forUnlockedPlots(info.garden.unlockedPlotCount());
        }
        return GreenhouseModel.defaultGrid();
    }

    /** Open the planner, respecting the premium gate. */
    public void openPlanner(Screen parent) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!PremiumGate.isUnlocked(ID)) {
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("§d[Nyxel] §7Mutation Helper is a premium feature."), false);
            }
            return;
        }
        if (repo.isEmpty()) {
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("§c[Nyxel] §7Mutation dataset failed to load."), false);
            }
            return;
        }
        mc.setScreen(new MutationPlannerScreen(this, parent));
    }
}
