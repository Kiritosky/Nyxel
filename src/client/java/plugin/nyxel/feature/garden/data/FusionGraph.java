package plugin.nyxel.feature.garden.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Interprets mutation requirements as a fusion graph: a requirement whose item
 * name matches another mutation is a fusion edge (a parent mutation); any other
 * requirement is a base material (a leaf in the tree).
 */
public final class FusionGraph {

    private final MutationRepository repo;

    public FusionGraph(MutationRepository repo) {
        this.repo = repo;
    }

    public boolean isMutation(String item) {
        return repo.byName(item) != null;
    }

    public Mutation asMutation(String item) {
        return repo.byName(item);
    }

    /** Direct parent mutations of {@code m} (excludes base materials). */
    public List<Mutation> parents(Mutation m) {
        List<Mutation> out = new ArrayList<>();
        for (Mutation.Requirement r : m.requirements) {
            Mutation parent = repo.byName(r.item);
            if (parent != null) {
                out.add(parent);
            }
        }
        return out;
    }
}
