package plugin.nyxel.feature.crafting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import plugin.nyxel.core.Feature;
import plugin.nyxel.core.NyxelExecutor;
import plugin.nyxel.feature.crafting.data.NeuRepoManager;
import plugin.nyxel.feature.crafting.data.RecipeRepository;
import plugin.nyxel.feature.crafting.engine.RecipeResolver;
import plugin.nyxel.feature.crafting.gui.CraftingPlannerScreen;

/**
 * Crafting-tree planner — the Ironman flagship for the crafting area. Given a
 * target item it resolves the full base-material shopping list and the order to
 * craft intermediates. Useful to anyone, essential when you can't buy the parts.
 * Owns the recipe dataset and resolver; data loads at construction.
 */
public final class CraftingPlannerFeature implements Feature {

    public static final String ID = "crafting-planner";

    private final RecipeRepository repo = new RecipeRepository();
    private final RecipeResolver resolver;

    public CraftingPlannerFeature() {
        // Bundled seed loads immediately so the planner always works offline; the
        // full NEU-REPO dataset (all items) downloads/updates in the background and
        // replaces it when ready.
        repo.loadBundled();
        this.resolver = new RecipeResolver(repo);
        NyxelExecutor.run("neu-recipes", () -> NeuRepoManager.get().updateRecipes(repo));
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Crafting Planner";
    }

    @Override
    public String description() {
        return "Resolve an item into its full base-material tree and build order";
    }

    @Override
    public Category category() {
        return Category.CRAFTING;
    }

    public RecipeRepository repo() {
        return repo;
    }

    public RecipeResolver resolver() {
        return resolver;
    }

    /** Open the planner screen, warning if the dataset failed to load. */
    public void openPlanner(Screen parent) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (repo.isEmpty()) {
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("§c[Nyxel] §7Recipe dataset failed to load."), false);
            }
            return;
        }
        mc.setScreen(new CraftingPlannerScreen(this, parent));
    }
}
