package plugin.nyxel.feature.economy;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import plugin.nyxel.config.FeatureOptions;
import plugin.nyxel.config.gui.option.OptionSpec;
import plugin.nyxel.core.Feature;
import plugin.nyxel.feature.economy.data.NpcPrice;
import plugin.nyxel.feature.economy.data.NpcPriceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adds NPC vendor sell (and optionally buy) prices to item tooltips. The
 * Ironman-friendly complement to {@link PriceTooltipFeature}: when you can't touch
 * the Bazaar, the NPC value is the number that matters. Prices come from Hypixel's
 * public resources API (bundled fallback) via {@link NpcPriceRepository}.
 */
public final class NpcPriceTooltipFeature implements Feature {

    static final String ID = "economy-npc-tooltip";
    private static final String OPT_SHOW_BUY = "show-buy";

    private final NpcPriceRepository prices = new NpcPriceRepository();

    private boolean registered = false;
    private volatile boolean active = false;

    public NpcPriceTooltipFeature() {
        prices.refresh();
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "NPC Price Tooltips";
    }

    @Override
    public String description() {
        return "Adds NPC vendor sell/buy value to item tooltips";
    }

    @Override
    public Category category() {
        return Category.ECONOMY;
    }

    @Override
    public List<OptionSpec> configOptions() {
        return List.of(OptionSpec.toggle(OPT_SHOW_BUY, "Show NPC buy price",
                "Also show what the NPC charges to buy the item", false));
    }

    @Override
    public void onEnable() {
        active = true;
        if (!registered) {
            ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
                if (active) {
                    addPriceLines(stack, lines);
                }
            });
            registered = true;
        }
    }

    @Override
    public void onDisable() {
        active = false;
    }

    private void addPriceLines(ItemStack stack, List<Text> lines) {
        String id = skyblockId(stack);
        if (id == null) {
            return;
        }
        NpcPrice p = prices.byId(id);
        if (p == null) {
            return;
        }
        lines.add(Text.literal("§7NPC sell: §6" + fmt(p.npcSell)));
        if (p.npcBuy != null && FeatureOptions.getBool(ID, OPT_SHOW_BUY, false)) {
            lines.add(Text.literal("§7NPC buy: §6" + fmt(p.npcBuy)));
        }
    }

    /** Read the SkyBlock item id from ExtraAttributes in the stack's custom data. */
    private static String skyblockId(ItemStack stack) {
        try {
            NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (comp == null) {
                return null;
            }
            NbtCompound nbt = comp.copyNbt();
            Optional<NbtCompound> extra = nbt.getCompound("ExtraAttributes");
            return extra.flatMap(c -> c.getString("id")).orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String fmt(long v) {
        return String.format("%,d", v);
    }
}
