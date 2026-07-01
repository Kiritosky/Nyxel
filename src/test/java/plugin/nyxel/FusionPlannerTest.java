package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.feature.garden.data.MutationRepository;
import plugin.nyxel.feature.garden.engine.FusionPlanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FusionPlannerTest {

    private static final String DATA = """
        {"version":1,"source":"test","mutations":[
          {"id":"choconut","name":"Choconut","rarity":"COMMON","surface":"Farmland",
           "requirements":[{"item":"Cocoa Beans","count":2}]},
          {"id":"chocoberry","name":"Chocoberry","rarity":"UNCOMMON","surface":"Farmland",
           "requirements":[{"item":"Choconut","count":6},{"item":"Gloomgourd","count":2}]},
          {"id":"godseed","name":"Godseed","rarity":"LEGENDARY","surface":"Farmland",
           "special":"ALL_POSITIVE_EFFECTS","requirements":[]},
          {"id":"rose_dragon","name":"Rose Dragon","rarity":"SPECIAL",
           "special":"ALL_MUTATIONS_EXCEPT_GODSEED","requirements":[]}
        ]}""";

    private FusionPlanner planner() {
        MutationRepository repo = new MutationRepository();
        repo.loadFromJson(DATA);
        return new FusionPlanner(repo);
    }

    @Test
    void expandsFusionTreeWithCounts() {
        FusionPlanner.PlanResult r = planner().plan("chocoberry", 1);
        // chocoberry needs 6 choconut; each choconut needs 2 cocoa beans -> 12
        assertEquals(6, r.mutationCounts.get("choconut"));
        assertEquals(12, r.materialCounts.get("Cocoa Beans"));
        assertEquals(2, r.materialCounts.get("Gloomgourd"));
    }

    @Test
    void quantityMultiplies() {
        FusionPlanner.PlanResult r = planner().plan("chocoberry", 2);
        assertEquals(12, r.mutationCounts.get("choconut"));
        assertEquals(24, r.materialCounts.get("Cocoa Beans"));
    }

    @Test
    void buildOrderHasParentsBeforeChildren() {
        FusionPlanner.PlanResult r = planner().plan("chocoberry", 1);
        var order = r.buildOrderList();
        assertTrue(order.indexOf("choconut") < order.indexOf("chocoberry"));
    }

    @Test
    void roseDragonRequiresAllMutationsExceptGodseed() {
        FusionPlanner.PlanResult r = planner().plan("rose_dragon", 1);
        assertTrue(r.mutationCounts.containsKey("chocoberry"));
        assertTrue(r.mutationCounts.containsKey("choconut"));
        assertTrue(!r.mutationCounts.containsKey("godseed"));
    }
}
