package plugin.nyxel.core;

import java.util.List;

/**
 * A self-contained group of features for one area of the mod (a config category).
 * The client entrypoint registers a fixed array of modules and asks each for its
 * features, so adding a feature means editing one module — never the entrypoint.
 */
public interface FeatureModule {

    /** Construct this module's features using the shared services in {@code ctx}. */
    List<Feature> create(FeatureContext ctx);
}
