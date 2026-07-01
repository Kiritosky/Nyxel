package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.feature.crafting.data.RecipeRepository;
import plugin.nyxel.feature.crafting.engine.RecipeResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeResolverTest {

    private static final String DATA = """
        {"version":1,"source":"test","recipes":[
          {"id":"enchanted_iron","name":"Enchanted Iron",
           "ingredients":[{"item":"Iron Ingot","count":160}]},
          {"id":"enchanted_iron_block","name":"Enchanted Iron Block",
           "ingredients":[{"item":"enchanted_iron","count":160}]},
          {"id":"cyclic_a","name":"Cyclic A","ingredients":[{"item":"cyclic_b","count":1}]},
          {"id":"cyclic_b","name":"Cyclic B","ingredients":[{"item":"cyclic_a","count":1}]}
        ]}""";

    private RecipeResolver resolver() {
        RecipeRepository repo = new RecipeRepository();
        repo.loadFromJson(DATA);
        return new RecipeResolver(repo);
    }

    @Test
    void expandsRecursivelyIntoBaseMaterials() {
        RecipeResolver.Result r = resolver().resolve("enchanted_iron_block", 1);
        // block needs 160 enchanted_iron; each needs 160 Iron Ingot -> 25600
        assertEquals(160, r.craftCounts.get("enchanted_iron"));
        assertEquals(1, r.craftCounts.get("enchanted_iron_block"));
        assertEquals(25600, r.materialCounts.get("Iron Ingot"));
    }

    @Test
    void quantityMultiplies() {
        RecipeResolver.Result r = resolver().resolve("enchanted_iron_block", 2);
        assertEquals(320, r.craftCounts.get("enchanted_iron"));
        assertEquals(51200, r.materialCounts.get("Iron Ingot"));
    }

    @Test
    void buildOrderCraftsIntermediatesFirst() {
        var order = resolver().resolve("enchanted_iron_block", 1).buildOrderList();
        assertTrue(order.indexOf("enchanted_iron") < order.indexOf("enchanted_iron_block"));
    }

    @Test
    void cyclesAreReportedNotInfinite() {
        RecipeResolver.Result r = resolver().resolve("cyclic_a", 1);
        assertFalse(r.warnings.isEmpty());
    }

    @Test
    void unknownTargetWarns() {
        RecipeResolver.Result r = resolver().resolve("does_not_exist", 1);
        assertFalse(r.warnings.isEmpty());
        assertTrue(r.materialCounts.isEmpty());
    }
}
