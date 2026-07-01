package plugin.nyxel.feature.garden.engine;

import plugin.nyxel.feature.garden.data.GreenhouseModel;
import plugin.nyxel.feature.garden.data.Mutation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Assigns greenhouse cells to grow the requested mutations: for each mutation
 * instance it reserves a center cell (where the mutation forms) plus a cell per
 * required parent crop, filled greedily row-major. Reports the cell→crop grid,
 * a legend, and warnings when space runs out.
 *
 * <p>Note: this models capacity and crop assignment; the exact in-game adjacency
 * geometry (plus/circle shapes) is an approximation pending precise data.
 */
public final class PlacementSolver {

    /** A request to grow {@code count} of {@code mutation}. */
    public record Request(Mutation mutation, int count) {
    }

    public PlacementResult solve(GreenhouseModel gh, List<Request> requests) {
        String[][] cells = new String[gh.rows][gh.cols];
        PlacementResult res = new PlacementResult(cells, gh.cols, gh.rows);

        for (Request req : requests) {
            Mutation m = req.mutation();
            int need = 1 + m.totalRequiredCrops();
            for (int i = 0; i < req.count(); i++) {
                List<int[]> block = findFreeCells(cells, gh, need);
                if (block.size() < need) {
                    res.warnings.add("Not enough space for " + m.name
                            + " (instance " + (i + 1) + "/" + req.count() + ")");
                    break;
                }
                int[] center = block.get(0);
                cells[center[1]][center[0]] = "★ " + m.name;
                res.legend.add(m.name);
                int idx = 1;
                for (Mutation.Requirement r : m.requirements) {
                    for (int k = 0; k < r.count && idx < block.size(); k++) {
                        int[] cell = block.get(idx++);
                        cells[cell[1]][cell[0]] = r.item;
                        res.legend.add(r.item);
                    }
                }
            }
        }
        return res;
    }

    /** First {@code n} free cells in row-major order as {col,row} pairs. */
    private static List<int[]> findFreeCells(String[][] cells, GreenhouseModel gh, int n) {
        List<int[]> out = new ArrayList<>();
        for (int r = 0; r < gh.rows && out.size() < n; r++) {
            for (int c = 0; c < gh.cols && out.size() < n; c++) {
                if (cells[r][c] == null) {
                    out.add(new int[]{c, r});
                }
            }
        }
        return out;
    }

    /** Result grid: {@code cell(col,row)} → crop/center label or null if empty. */
    public static final class PlacementResult {
        public final String[][] cells;
        public final int cols;
        public final int rows;
        public final Set<String> legend = new LinkedHashSet<>();
        public final List<String> warnings = new ArrayList<>();

        public PlacementResult(String[][] cells, int cols, int rows) {
            this.cells = cells;
            this.cols = cols;
            this.rows = rows;
        }

        public String at(int col, int row) {
            return cells[row][col];
        }
    }
}
