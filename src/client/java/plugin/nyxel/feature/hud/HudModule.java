package plugin.nyxel.feature.hud;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;

import java.util.List;

/** HUD & overlay features that aren't owned by a gameplay area. */
public final class HudModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new SkillTrackerFeature(ctx.state),
                new CollectionTrackerFeature()
        );
    }
}
