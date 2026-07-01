package plugin.nyxel.feature.economy;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureContext;
import plugin.nyxel.core.FeatureModule;
import plugin.nyxel.core.StubFeature;

import java.util.List;

/** Economy features: item price tooltips, plus the WIP auction-flip helper. */
public final class EconomyModule implements FeatureModule {

    @Override
    public List<Feature> create(FeatureContext ctx) {
        return List.of(
                new PriceTooltipFeature(),
                new NpcPriceTooltipFeature(),
                new StubFeature("economy-auction-flips", "Auction Flip Helper",
                        Feature.Category.ECONOMY)
        );
    }
}
