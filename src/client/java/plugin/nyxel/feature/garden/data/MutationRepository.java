package plugin.nyxel.feature.garden.data;

import com.google.gson.Gson;
import plugin.nyxel.Nyxel;
import plugin.nyxel.api.HttpJson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the mutation dataset. The bundled {@code mutations.json} is the baseline;
 * an optional remote URL can override it (so game updates don't require a mod
 * update) with the bundled data as fallback. Lookups by id and by display name
 * (name lookup is how requirement items resolve to fusion edges).
 */
public final class MutationRepository {

    private static final Gson GSON = new Gson();

    private final Map<String, Mutation> byId = new LinkedHashMap<>();
    private final Map<String, Mutation> byName = new HashMap<>();

    public void loadBundled() {
        try (InputStream in = MutationRepository.class
                .getResourceAsStream("/assets/nyxel/data/mutations.json")) {
            if (in == null) {
                Nyxel.LOGGER.error("mutations.json not found on classpath");
                return;
            }
            Root root = GSON.fromJson(
                    new InputStreamReader(in, StandardCharsets.UTF_8), Root.class);
            apply(root);
            Nyxel.LOGGER.info("Loaded {} mutations (bundled)", byId.size());
        } catch (Exception e) {
            Nyxel.LOGGER.error("Failed to load bundled mutations.json", e);
        }
    }

    /** Load directly from a JSON string (used by tests and remote loading). */
    public void loadFromJson(String json) {
        apply(GSON.fromJson(json, Root.class));
    }

    /** Best-effort remote override; keeps bundled data if it fails. */
    public void tryLoadRemote(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            String json = HttpJson.get(url, null).toString();
            Root root = GSON.fromJson(json, Root.class);
            if (root != null && root.mutations != null && !root.mutations.isEmpty()) {
                byId.clear();
                byName.clear();
                apply(root);
                Nyxel.LOGGER.info("Loaded {} mutations (remote)", byId.size());
            }
        } catch (Exception e) {
            Nyxel.LOGGER.warn("Remote mutation dataset failed, keeping bundled: {}",
                    e.getMessage());
        }
    }

    private void apply(Root root) {
        if (root == null || root.mutations == null) {
            return;
        }
        for (Mutation m : root.mutations) {
            if (m.id == null || m.name == null) {
                continue;
            }
            byId.put(m.id, m);
            byName.put(m.name.toLowerCase(Locale.ROOT), m);
        }
    }

    public Collection<Mutation> all() {
        return byId.values();
    }

    public Mutation byId(String id) {
        return id == null ? null : byId.get(id);
    }

    public Mutation byName(String name) {
        return name == null ? null : byName.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean isEmpty() {
        return byId.isEmpty();
    }

    private static final class Root {
        int version;
        String source;
        List<Mutation> mutations;
    }
}
