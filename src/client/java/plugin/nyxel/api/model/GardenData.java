package plugin.nyxel.api.model;

import java.util.Set;

/**
 * Subset of the SkyBlock garden endpoint relevant to the mutation planner:
 * accumulated garden experience and which plots are unlocked (used to size the
 * greenhouse grid).
 */
public final class GardenData {

    public final long experience;
    public final Set<String> unlockedPlots;

    public GardenData(long experience, Set<String> unlockedPlots) {
        this.experience = experience;
        this.unlockedPlots = unlockedPlots;
    }

    public int unlockedPlotCount() {
        return unlockedPlots.size();
    }

    public static GardenData empty() {
        return new GardenData(0, Set.of());
    }
}
