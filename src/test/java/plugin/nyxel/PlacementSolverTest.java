package plugin.nyxel;

import org.junit.jupiter.api.Test;
import plugin.nyxel.feature.garden.data.GreenhouseModel;
import plugin.nyxel.feature.garden.data.MutationRepository;
import plugin.nyxel.feature.garden.engine.PlacementSolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlacementSolverTest {

    private static final String DATA = """
        {"version":1,"source":"test","mutations":[
          {"id":"choconut","name":"Choconut","rarity":"COMMON","surface":"Farmland",
           "requirements":[{"item":"Cocoa Beans","count":2}]}
        ]}""";

    private MutationRepository repo() {
        MutationRepository repo = new MutationRepository();
        repo.loadFromJson(DATA);
        return repo;
    }

    @Test
    void placesCenterPlusRequiredCrops() {
        var m = repo().byId("choconut");
        var solver = new PlacementSolver();
        var res = solver.solve(new GreenhouseModel(5, 5),
                List.of(new PlacementSolver.Request(m, 1)));

        int filled = 0;
        boolean hasCenter = false;
        for (int r = 0; r < res.rows; r++) {
            for (int c = 0; c < res.cols; c++) {
                String cell = res.at(c, r);
                if (cell != null) {
                    filled++;
                    if (cell.startsWith("★")) {
                        hasCenter = true;
                    }
                }
            }
        }
        assertTrue(hasCenter, "should mark a center cell");
        assertEquals(3, filled, "1 center + 2 cocoa beans");
        assertTrue(res.warnings.isEmpty());
    }

    @Test
    void warnsWhenNoSpace() {
        var m = repo().byId("choconut");
        var res = new PlacementSolver().solve(new GreenhouseModel(1, 1),
                List.of(new PlacementSolver.Request(m, 1)));
        assertTrue(!res.warnings.isEmpty(), "1 cell can't fit a 3-cell mutation");
    }
}
