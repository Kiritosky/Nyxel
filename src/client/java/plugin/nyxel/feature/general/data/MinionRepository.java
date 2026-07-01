package plugin.nyxel.feature.general.data;

import plugin.nyxel.feature.common.data.DataRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the bundled minion dataset. Minion tier/fuel data is not exposed by the
 * Hypixel API, so this is bundled (community-sourced) with no live source. In
 * addition to the minions indexed by the {@link DataRepository} base, the dataset
 * carries a shared fuels table, captured during {@link #parse} and exposed here.
 */
public final class MinionRepository extends DataRepository<Minion> {

    private final Map<String, Fuel> fuelsById = new LinkedHashMap<>();

    @Override
    protected String resourcePath() {
        return "/assets/nyxel/data/minions.json";
    }

    @Override
    protected List<Minion> parse(String json) {
        Root root = GSON.fromJson(json, Root.class);
        if (root == null) {
            return null;
        }
        fuelsById.clear();
        if (root.fuels != null) {
            for (Fuel f : root.fuels) {
                if (f != null && f.id != null) {
                    fuelsById.put(f.id.toLowerCase(Locale.ROOT), f);
                }
            }
        }
        return root.minions;
    }

    @Override
    protected String idOf(Minion item) {
        return item.id;
    }

    @Override
    protected String nameOf(Minion item) {
        return item.name;
    }

    @Override
    protected String label() {
        return "minions";
    }

    /** All fuels in declared order. */
    public List<Fuel> fuels() {
        return new ArrayList<>(fuelsById.values());
    }

    public Fuel fuelById(String id) {
        return id == null ? null : fuelsById.get(id.toLowerCase(Locale.ROOT));
    }

    private static final class Root {
        int version;
        String source;
        List<Fuel> fuels;
        List<Minion> minions;
    }
}
