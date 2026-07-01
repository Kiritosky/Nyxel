package plugin.nyxel.feature.economy;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import plugin.nyxel.config.ConfigManager;
import plugin.nyxel.core.Feature;

import java.util.Optional;

/**
 * Adds Bazaar buy/sell price lines to item tooltips. Anchor feature for the
 * economy area; reads the SkyBlock item id from the stack's custom data and looks
 * it up in {@link BazaarClient}. All NBT access is defensive.
 */
public final class PriceTooltipFeature implements Feature {

    private boolean registered = false;
    private volatile boolean active = false;

    @Override
    public String id() {
        return "economy-price-tooltip";
    }

    @Override
    public String displayName() {
        return "Bazaar Price Tooltips";
    }

    @Override
    public String description() {
        return "Adds Bazaar buy/sell prices to item tooltips";
    }

    @Override
    public Category category() {
        return Category.ECONOMY;
    }

    @Override
    public void onEnable() {
        active = true;
        if (!registered) {
            ItemTooltipCallback.EVENT.register((stack, ctx, type, lines) -> {
                if (!active || !ConfigManager.get().economy.showTooltipPrices) {
                    return;
                }
                addPriceLines(stack, lines);
            });
            registered = true;
        }
    }

    @Override
    public void onDisable() {
        active = false;
    }

    private void addPriceLines(ItemStack stack, java.util.List<Text> lines) {
        String id = skyblockId(stack);
        if (id == null) {
            return;
        }
        BazaarClient bz = BazaarClient.get();
        Optional<Double> buy = bz.buyPrice(id);
        Optional<Double> sell = bz.sellPrice(id);
        if (buy.isEmpty() && sell.isEmpty()) {
            return;
        }
        buy.ifPresent(v -> lines.add(Text.literal("§7Bazaar buy: §6" + fmt(v))));
        sell.ifPresent(v -> lines.add(Text.literal("§7Bazaar sell: §6" + fmt(v))));
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

    private static String fmt(double v) {
        return String.format("%,.0f", v);
    }
}
