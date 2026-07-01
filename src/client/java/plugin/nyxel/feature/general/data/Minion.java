package plugin.nyxel.feature.general.data;

import java.util.ArrayList;
import java.util.List;

/**
 * One minion type, deserialized from {@code minions.json}. {@code actionSeconds}
 * and {@code storageSlots} are per-tier (index 0 = tier I). {@code drops} are the
 * items produced per collect.
 */
public class Minion {

    public String id;
    public String name;
    public List<Double> actionSeconds = new ArrayList<>();
    public List<Drop> drops = new ArrayList<>();
    public List<Integer> storageSlots = new ArrayList<>();

    public static class Drop {
        public String item;
        public int count;
    }

    /** Highest tier this minion has data for (1-based). */
    public int maxTier() {
        return actionSeconds.size();
    }
}
