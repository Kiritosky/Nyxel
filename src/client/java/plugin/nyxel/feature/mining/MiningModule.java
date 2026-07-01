package plugin.nyxel.feature.mining;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;
import plugin.nyxel.core.StubFeature;

import java.util.List;

/** Mining & farming features: commission HUD, plus WIP pest/visitor helpers. */
public final class MiningModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new CommissionHudFeature(ctx.state),
                new StubFeature("mining-pest-helper", "Pest Helper",
                        Feature.Category.MINING),
                new StubFeature("farming-visitor-helper", "Garden Visitor Helper",
                        Feature.Category.MINING)
        );
    }
}
