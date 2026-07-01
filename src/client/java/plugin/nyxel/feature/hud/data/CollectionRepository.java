package plugin.nyxel.feature.hud.data;

import com.google.gson.annotations.SerializedName;
import plugin.nyxel.feature.common.data.DataRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SkyBlock collection definitions. The live source is Hypixel's public resources
 * endpoint ({@code /v2/resources/skyblock/collections}, no key required), which
 * lists every collection's tiers and unlocks. The bundled {@code collections.json}
 * is only an offline fallback. Entries are keyed by the collected item's display
 * name so action-bar gains resolve to a collection.
 */
public final class CollectionRepository extends DataRepository<CollectionEntry> {

    private static final String COLLECTIONS_URL =
            "https://api.hypixel.net/v2/resources/skyblock/collections";

    @Override
    protected String resourcePath() {
        return "/assets/nyxel/data/collections.json";
    }

    @Override
    protected String remoteUrl() {
        return COLLECTIONS_URL;
    }

    @Override
    protected List<CollectionEntry> parse(String json) {
        Root root = GSON.fromJson(json, Root.class);
        return root == null ? null : root.collections;
    }

    /** Flatten the Hypixel resources/collections shape into our per-item records. */
    @Override
    protected List<CollectionEntry> parseRemote(String json) {
        ApiRoot root = GSON.fromJson(json, ApiRoot.class);
        if (root == null || root.collections == null) {
            return null;
        }
        List<CollectionEntry> out = new ArrayList<>();
        for (ApiGroup group : root.collections.values()) {
            if (group == null || group.items == null) {
                continue;
            }
            for (ApiItem item : group.items.values()) {
                if (item == null || item.name == null) {
                    continue;
                }
                CollectionEntry e = new CollectionEntry();
                e.id = item.name;
                e.name = item.name;
                if (item.tiers != null) {
                    for (ApiTier t : item.tiers) {
                        CollectionEntry.Tier tier = new CollectionEntry.Tier();
                        tier.amount = t.amountRequired;
                        tier.unlocks = (t.unlocks == null || t.unlocks.isEmpty())
                                ? "Tier " + t.tier : t.unlocks.get(0);
                        e.tiers.add(tier);
                    }
                }
                out.add(e);
            }
        }
        return out;
    }

    @Override
    protected String idOf(CollectionEntry item) {
        return item.id;
    }

    @Override
    protected String nameOf(CollectionEntry item) {
        return item.name;
    }

    @Override
    protected String label() {
        return "collections";
    }

    private static final class Root {
        int version;
        String source;
        List<CollectionEntry> collections;
    }

    // --- Hypixel resources/collections shape ---

    private static final class ApiRoot {
        Map<String, ApiGroup> collections;
    }

    private static final class ApiGroup {
        Map<String, ApiItem> items;
    }

    private static final class ApiItem {
        String name;
        int maxTiers;
        List<ApiTier> tiers;
    }

    private static final class ApiTier {
        int tier;
        @SerializedName("amountRequired")
        long amountRequired;
        List<String> unlocks;
    }
}
