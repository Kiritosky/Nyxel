package plugin.nyxel.feature.economy.data;

/**
 * NPC vendor value for one item, deserialized from {@code npc_prices.json}.
 * {@code id} matches the SkyBlock item id in NBT. {@code npcBuy} is boxed so an
 * absent purchase price stays {@code null} rather than defaulting to zero.
 */
public class NpcPrice {

    public String id;
    public String name;
    public long npcSell;
    public Long npcBuy;
}
