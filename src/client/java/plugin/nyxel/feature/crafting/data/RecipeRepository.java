package plugin.nyxel.feature.crafting.data;

import plugin.nyxel.feature.common.data.DataRepository;

import java.util.List;

/** Loads the bundled crafting-recipe dataset. */
public final class RecipeRepository extends DataRepository<Recipe> {

    @Override
    protected String resourcePath() {
        return "/assets/nyxel/data/recipes.json";
    }

    @Override
    protected List<Recipe> parse(String json) {
        Root root = GSON.fromJson(json, Root.class);
        return root == null ? null : root.recipes;
    }

    @Override
    protected String idOf(Recipe item) {
        return item.id;
    }

    @Override
    protected String nameOf(Recipe item) {
        return item.name;
    }

    @Override
    protected String label() {
        return "recipes";
    }

    private static final class Root {
        int version;
        String source;
        List<Recipe> recipes;
    }
}
