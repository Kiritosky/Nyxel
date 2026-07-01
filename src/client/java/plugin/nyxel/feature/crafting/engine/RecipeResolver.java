package plugin.nyxel.feature.crafting.engine;

import plugin.nyxel.feature.crafting.data.Recipe;
import plugin.nyxel.feature.crafting.data.RecipeRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Expands a target craft into the full material tree: how many of each base
 * material and each intermediate craft are needed, plus a children-first build
 * order. Ingredients that are themselves recipe ids recurse; anything else is a
 * base material (a leaf). Mirrors the garden {@code FusionPlanner} DFS, including
 * cycle detection. No Minecraft dependencies, so it is unit-testable.
 */
public final class RecipeResolver {

    private final RecipeRepository repo;

    public RecipeResolver(RecipeRepository repo) {
        this.repo = repo;
    }

    public Result resolve(String targetId, int quantity) {
        Result res = new Result();
        Recipe target = repo.byId(targetId);
        if (target == null) {
            res.warnings.add("Unknown recipe: " + targetId);
            return res;
        }
        expand(target, Math.max(1, quantity), res, new ArrayDeque<>());
        return res;
    }

    private void expand(Recipe r, int qty, Result res, Deque<String> stack) {
        res.craftCounts.merge(r.id, qty, Integer::sum);
        if (stack.contains(r.id)) {
            res.warnings.add("Cyclic recipe at " + r.name);
            return;
        }
        stack.push(r.id);
        for (Recipe.Ingredient ing : r.ingredients) {
            Recipe sub = repo.byId(ing.item);
            if (sub != null) {
                expand(sub, qty * ing.count, res, stack);
            } else {
                res.materialCounts.merge(ing.item, qty * ing.count, Integer::sum);
            }
        }
        stack.pop();
        res.buildOrder.add(r.id); // post-order => make intermediates before the target
    }

    /** Aggregated plan: counts per craft/material, a build order, and warnings. */
    public static final class Result {
        public final Map<String, Integer> craftCounts = new LinkedHashMap<>();
        public final Map<String, Integer> materialCounts = new LinkedHashMap<>();
        public final LinkedHashSet<String> buildOrder = new LinkedHashSet<>();
        public final List<String> warnings = new ArrayList<>();

        public List<String> buildOrderList() {
            return new ArrayList<>(buildOrder);
        }
    }
}
