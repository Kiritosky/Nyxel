package plugin.nyxel.feature.economy.data;

import com.google.gson.annotations.SerializedName;
import plugin.nyxel.feature.common.data.DataRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * NPC vendor prices. The live source is Hypixel's public resources endpoint
 * ({@code /v2/resources/skyblock/items}, no key required), which carries the
 * {@code npc_sell_price} for every sellable item. The bundled {@code npc_prices.json}
 * is only an offline fallback.
 */
public final class NpcPriceRepository extends DataRepository<NpcPrice> {

    private static final String ITEMS_URL =
            "https://api.hypixel.net/v2/resources/skyblock/items";

    @Override
    protected String resourcePath() {
        return "/assets/nyxel/data/npc_prices.json";
    }

    @Override
    protected String remoteUrl() {
        return ITEMS_URL;
    }

    @Override
    protected List<NpcPrice> parse(String json) {
        Root root = GSON.fromJson(json, Root.class);
        return root == null ? null : root.prices;
    }

    /** Map the Hypixel resources/items shape to our records, keeping only sellables. */
    @Override
    protected List<NpcPrice> parseRemote(String json) {
        ItemsRoot root = GSON.fromJson(json, ItemsRoot.class);
        if (root == null || root.items == null) {
            return null;
        }
        List<NpcPrice> out = new ArrayList<>(root.items.size());
        for (ApiItem it : root.items) {
            if (it.id == null || it.npcSellPrice == null) {
                continue;
            }
            NpcPrice p = new NpcPrice();
            p.id = it.id;
            p.name = it.name != null ? it.name : it.id;
            p.npcSell = Math.round(it.npcSellPrice);
            out.add(p);
        }
        return out;
    }

    @Override
    protected String idOf(NpcPrice item) {
        return item.id;
    }

    @Override
    protected String nameOf(NpcPrice item) {
        return item.name;
    }

    @Override
    protected String label() {
        return "NPC prices";
    }

    private static final class Root {
        int version;
        String source;
        List<NpcPrice> prices;
    }

    /** Subset of the Hypixel resources/items response we care about. */
    private static final class ItemsRoot {
        List<ApiItem> items;
    }

    private static final class ApiItem {
        String id;
        String name;
        @SerializedName("npc_sell_price")
        Double npcSellPrice;
    }
}
