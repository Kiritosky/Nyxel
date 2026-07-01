package plugin.nyxel.feature.fishing;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;

import java.util.List;

/** Fishing features: sea-creature alerts, catch timer, trophy tracking, gear checks. */
public final class FishingModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new SeaCreatureAlertsFeature(),
                new CatchTimerFeature(),
                new TrophyFishTracker(),
                new HotspotHelper(),
                new GearChecker(ctx.state)
        );
    }
}
