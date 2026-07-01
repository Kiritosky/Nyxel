package plugin.nyxel.feature.garden.data;

import plugin.nyxel.feature.common.data.DataRepository;

import java.util.List;

/**
 * Loads the mutation dataset. The bundled {@code mutations.json} is the baseline;
 * an optional remote URL can override it (so game updates don't require a mod
 * update) with the bundled data as fallback. Lookups by id and by display name
 * (name lookup is how requirement items resolve to fusion edges).
 *
 * <p>IO and caching live in {@link DataRepository}; this class only describes the
 * mutation schema.
 */
public final class MutationRepository extends DataRepository<Mutation> {

    @Override
    protected String resourcePath() {
        return "/assets/nyxel/data/mutations.json";
    }

    @Override
    protected List<Mutation> parse(String json) {
        Root root = GSON.fromJson(json, Root.class);
        return root == null ? null : root.mutations;
    }

    @Override
    protected String idOf(Mutation item) {
        return item.id;
    }

    @Override
    protected String nameOf(Mutation item) {
        return item.name;
    }

    @Override
    protected String label() {
        return "mutations";
    }

    private static final class Root {
        int version;
        String source;
        List<Mutation> mutations;
    }
}
