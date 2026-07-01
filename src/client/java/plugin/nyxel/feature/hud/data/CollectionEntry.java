package plugin.nyxel.feature.hud.data;

import java.util.ArrayList;
import java.util.List;

/**
 * One SkyBlock collection, deserialized from {@code collections.json}. Tiers are
 * cumulative amounts; each names what reaching it unlocks. {@code id} matches the
 * collected item's display name so action-bar gains resolve to a collection.
 */
public class CollectionEntry {

    public String id;
    public String name;
    public List<Tier> tiers = new ArrayList<>();

    public static class Tier {
        public long amount;
        public String unlocks;
    }

    /** The first tier whose amount is still above {@code current}, or null if maxed. */
    public Tier nextTier(long current) {
        for (Tier t : tiers) {
            if (current < t.amount) {
                return t;
            }
        }
        return null;
    }
}
