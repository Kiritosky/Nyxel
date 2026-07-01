package plugin.nyxel.core;

import plugin.nyxel.api.PlayerDataService;

/**
 * Shared services handed to each {@link FeatureModule} so it can construct its
 * features. Keeps feature wiring out of the client entrypoint.
 */
public final class FeatureContext {

    public final SkyblockState state;
    public final PlayerDataService playerData;

    public FeatureContext(SkyblockState state, PlayerDataService playerData) {
        this.state = state;
        this.playerData = playerData;
    }
}
