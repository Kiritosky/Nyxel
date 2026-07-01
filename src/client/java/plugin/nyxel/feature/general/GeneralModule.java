package plugin.nyxel.feature.general;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;

import java.util.List;

/** General-purpose features that don't belong to a single gameplay area. */
public final class GeneralModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new MinionPlannerFeature()
        );
    }
}
