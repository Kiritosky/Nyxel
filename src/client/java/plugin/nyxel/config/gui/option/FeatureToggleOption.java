package plugin.nyxel.config.gui.option;

import plugin.nyxel.core.Feature;
import plugin.nyxel.core.FeatureManager;

/** A {@link ToggleOption} backed by a {@link Feature}'s enabled state. */
public final class FeatureToggleOption extends ToggleOption {

    public FeatureToggleOption(FeatureManager manager, Feature feature) {
        super(feature.displayName(), feature.description(),
                () -> manager.isEnabled(feature.id()),
                v -> manager.setEnabled(feature.id(), v));
    }
}
