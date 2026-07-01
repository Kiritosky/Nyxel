package plugin.nyxel.feature.garden.data;

import java.util.ArrayList;
import java.util.List;

/**
 * One greenhouse mutation, deserialized from {@code mutations.json}. A
 * requirement item that matches another mutation's name is a fusion edge;
 * otherwise it's a base material (a leaf in the planning tree). {@code special}
 * marks non-count conditions (e.g. Godseed / Rose Dragon).
 */
public class Mutation {

    public String id;
    public String name;
    public String rarity;
    public String size;
    public String surface;
    public boolean watered;
    public String special;
    public List<Requirement> requirements = new ArrayList<>();
    public List<String> drops = new ArrayList<>();
    public List<String> effects = new ArrayList<>();

    public static class Requirement {
        public String item;
        public int count;
    }

    /** Total crops needed around the empty plot to spawn this mutation. */
    public int totalRequiredCrops() {
        int n = 0;
        for (Requirement r : requirements) {
            n += r.count;
        }
        return n;
    }
}
