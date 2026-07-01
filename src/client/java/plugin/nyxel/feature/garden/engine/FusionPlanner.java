package plugin.nyxel.feature.garden.engine;

import plugin.nyxel.feature.garden.data.FusionGraph;
import plugin.nyxel.feature.garden.data.Mutation;
import plugin.nyxel.feature.garden.data.MutationRepository;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Expands a target mutation into the full fusion dependency tree: how many of
 * each intermediate mutation and base material are needed, plus a parents-first
 * build order. Handles the special targets Godseed and Rose Dragon.
 */
public final class FusionPlanner {

    private final MutationRepository repo;
    private final FusionGraph graph;

    public FusionPlanner(MutationRepository repo) {
        this.repo = repo;
        this.graph = new FusionGraph(repo);
    }

    public PlanResult plan(String targetId, int quantity) {
        PlanResult res = new PlanResult();
        Mutation target = repo.byId(targetId);
        if (target == null) {
            res.warnings.add("Unknown target: " + targetId);
            return res;
        }
        int qty = Math.max(1, quantity);

        if ("ALL_MUTATIONS_EXCEPT_GODSEED".equals(target.special)) {
            res.notes.add("Rose Dragon requires every mutation except Godseed.");
            for (Mutation m : repo.all()) {
                if (m.id.equals("rose_dragon") || m.id.equals("godseed")) {
                    continue;
                }
                expand(m, qty, res, new ArrayDeque<>());
            }
            return res;
        }
        expand(target, qty, res, new ArrayDeque<>());
        return res;
    }

    private void expand(Mutation m, int qty, PlanResult res, Deque<String> stack) {
        res.mutationCounts.merge(m.id, qty, Integer::sum);
        if (stack.contains(m.id)) {
            res.warnings.add("Cyclic requirement at " + m.name);
            return;
        }
        if (m.special != null && m.requirements.isEmpty()) {
            res.notes.add(m.name + ": " + m.special);
        }
        stack.push(m.id);
        for (Mutation.Requirement r : m.requirements) {
            Mutation parent = graph.asMutation(r.item);
            if (parent != null) {
                expand(parent, qty * r.count, res, stack);
            } else {
                res.materialCounts.merge(r.item, qty * r.count, Integer::sum);
            }
        }
        stack.pop();
        res.buildOrder.add(m.id); // post-order => parents before children
    }

    /** Aggregated plan: counts per mutation/material, build order, notes. */
    public static final class PlanResult {
        public final Map<String, Integer> mutationCounts = new LinkedHashMap<>();
        public final Map<String, Integer> materialCounts = new LinkedHashMap<>();
        public final LinkedHashSet<String> buildOrder = new LinkedHashSet<>();
        public final List<String> notes = new ArrayList<>();
        public final List<String> warnings = new ArrayList<>();

        public List<String> buildOrderList() {
            return new ArrayList<>(buildOrder);
        }
    }
}
