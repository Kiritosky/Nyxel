package plugin.nyxel.feature.garden;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;

import java.util.List;

/** Garden features: the flagship greenhouse Mutation Helper. */
public final class GardenModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new MutationHelperFeature(ctx.playerData)
        );
    }
}
