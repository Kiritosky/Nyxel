package plugin.nyxel.data.neu;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One SkyBlock item distilled from the NEU-REPO, in Nyxel's own compact shape.
 * {@code recipe} maps an ingredient internal-name to its total count (aggregated
 * across the NEU 3×3 grid); empty when the item has no craft grid. Features build
 * their own logic (recipe resolver, price tables, name lookups) on top of this.
 */
public class NeuItem {

    public String internalName;
    public String displayName;
    public Long npcSell;
    public Map<String, Integer> recipe = new LinkedHashMap<>();

    public boolean hasRecipe() {
        return recipe != null && !recipe.isEmpty();
    }
}
