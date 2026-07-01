package plugin.nyxel.feature.crafting;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;

import java.util.List;

/** Crafting & recipes features: the recursive crafting-tree planner. */
public final class CraftingModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new CraftingPlannerFeature()
        );
    }
}
