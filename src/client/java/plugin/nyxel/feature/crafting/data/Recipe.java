package plugin.nyxel.feature.crafting.data;

import java.util.ArrayList;
import java.util.List;

/**
 * One craft, deserialized from {@code recipes.json}. An ingredient whose
 * {@code item} matches another recipe's id is an intermediate craft (a graph
 * edge); any other {@code item} is a base material (a leaf in the tree).
 */
public class Recipe {

    public String id;
    public String name;
    public List<Ingredient> ingredients = new ArrayList<>();

    public static class Ingredient {
        public String item;
        public int count;
    }
}
