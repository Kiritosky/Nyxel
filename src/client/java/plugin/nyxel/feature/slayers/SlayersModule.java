package plugin.nyxel.feature.slayers;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;
import plugin.nyxel.core.StubFeature;

import java.util.List;

/** Dungeons & Slayers features: slayer timer, plus WIP dungeon helpers. */
public final class SlayersModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new SlayerTimerFeature(),
                new StubFeature("dungeons-map", "Dungeon Map",
                        Feature.Category.DUNGEONS),
                new StubFeature("dungeons-party-finder", "Party Finder",
                        Feature.Category.DUNGEONS)
        );
    }
}
