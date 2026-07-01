package plugin.nyxel.feature.crafting.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import plugin.nyxel.feature.crafting.CraftingPlannerFeature;
import plugin.nyxel.feature.crafting.data.Recipe;
import plugin.nyxel.feature.crafting.engine.RecipeResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Crafting planner: pick a target recipe and quantity, and read off the resolved
 * base-material shopping list plus the order to craft intermediates. The recursion
 * lives in {@link RecipeResolver}; this screen just drives it and renders results.
 */
public final class CraftingPlannerScreen extends Screen {

    private final CraftingPlannerFeature feature;
    private final Screen parent;
    private final List<Recipe> recipes;

    private int recipeIndex = 0;
    private int quantity = 1;

    private ButtonWidget recipeButton;
    private ButtonWidget qtyButton;

    public CraftingPlannerScreen(CraftingPlannerFeature feature, Screen parent) {
        super(Text.literal("Crafting Planner"));
        this.feature = feature;
        this.parent = parent;
        this.recipes = new ArrayList<>(feature.repo().all());
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = 40;
        int w = 240;
        int h = 20;

        recipeButton = ButtonWidget.builder(Text.empty(), b -> cycleRecipe(1))
                .dimensions(cx - w / 2, y, w, h).build();
        addDrawableChild(recipeButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("- Qty"), b -> changeQty(-1))
                .dimensions(cx - w / 2, y + 24, 70, h).build());
        qtyButton = ButtonWidget.builder(Text.empty(), b -> {})
                .dimensions(cx - 45, y + 24, 90, h).build();
        qtyButton.active = false;
        addDrawableChild(qtyButton);
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Qty"), b -> changeQty(1))
                .dimensions(cx + w / 2 - 70, y + 24, 70, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - 50, height - 30, 100, h).build());

        updateLabels();
    }

    private void cycleRecipe(int dir) {
        if (recipes.isEmpty()) {
            return;
        }
        recipeIndex = Math.floorMod(recipeIndex + dir, recipes.size());
        updateLabels();
    }

    private void changeQty(int dir) {
        quantity = Math.max(1, Math.min(64, quantity + dir));
        updateLabels();
    }

    private Recipe current() {
        return recipes.get(recipeIndex);
    }

    private void updateLabels() {
        if (recipes.isEmpty()) {
            recipeButton.setMessage(Text.literal("§cNo recipe data"));
            return;
        }
        recipeButton.setMessage(Text.literal("§e" + current().name + " §7(click to change)"));
        qtyButton.setMessage(Text.literal("§fx" + quantity));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§d§lCrafting Planner"),
                width / 2, 20, 0xFFFFFFFF);
        if (recipes.isEmpty()) {
            return;
        }

        RecipeResolver.Result r = feature.resolver().resolve(current().id, quantity);
        int left = width / 2 - 150;
        int right = width / 2 + 20;
        int y = 100;

        ctx.drawTextWithShadow(textRenderer, Text.literal("§b§lBase materials"), left, y, 0xFFFFFFFF);
        int ly = y + 12;
        for (Map.Entry<String, Integer> e : r.materialCounts.entrySet()) {
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§7• §f" + e.getKey() + " §7x" + e.getValue()), left, ly, 0xFFFFFFFF);
            ly += 10;
        }

        ctx.drawTextWithShadow(textRenderer, Text.literal("§a§lCraft order"), right, y, 0xFFFFFFFF);
        int ry = y + 12;
        for (String craftId : r.buildOrderList()) {
            Recipe c = feature.repo().byId(craftId);
            String name = c != null ? c.name : craftId;
            Integer count = r.craftCounts.get(craftId);
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§7• §f" + name + (count != null ? " §7x" + count : "")),
                    right, ry, 0xFFFFFFFF);
            ry += 10;
        }

        if (!r.warnings.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§c" + r.warnings.get(0)), width / 2, height - 50, 0xFFFFFFFF);
        }
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
